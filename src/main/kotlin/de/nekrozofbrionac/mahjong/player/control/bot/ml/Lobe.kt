package de.nekrozofbrionac.mahjong.player.control.bot.ml

import org.jetbrains.kotlinx.multik.api.d1array
import org.jetbrains.kotlinx.multik.api.d2array
import org.jetbrains.kotlinx.multik.api.linalg.dot
import org.jetbrains.kotlinx.multik.api.mk
import org.jetbrains.kotlinx.multik.api.ndarray
import org.jetbrains.kotlinx.multik.api.zeros
import org.jetbrains.kotlinx.multik.ndarray.data.D1Array
import org.jetbrains.kotlinx.multik.ndarray.data.D2Array
import org.jetbrains.kotlinx.multik.ndarray.data.get
import org.jetbrains.kotlinx.multik.ndarray.data.set
import org.jetbrains.kotlinx.multik.ndarray.operations.div
import org.jetbrains.kotlinx.multik.ndarray.operations.forEach
import org.jetbrains.kotlinx.multik.ndarray.operations.map
import org.jetbrains.kotlinx.multik.ndarray.operations.minus
import org.jetbrains.kotlinx.multik.ndarray.operations.plus
import org.jetbrains.kotlinx.multik.ndarray.operations.sum
import org.jetbrains.kotlinx.multik.ndarray.operations.times
import org.jetbrains.kotlinx.multik.ndarray.operations.toFloatArray
import java.io.*
import kotlin.math.exp
import kotlin.math.max
import kotlin.math.sqrt

data class TrainingSample(
  val input: FloatArray,
  val expectedOutput: FloatArray,
  val mask: FloatArray
) {
  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (javaClass != other?.javaClass) return false

    other as TrainingSample

    if (!input.contentEquals(other.input)) return false
    if (!expectedOutput.contentEquals(other.expectedOutput)) return false
    if (!mask.contentEquals(other.mask)) return false

    return true
  }

  override fun hashCode(): Int {
    var result = input.contentHashCode()
    result = 31 * result + expectedOutput.contentHashCode()
    result = 31 * result + mask.contentHashCode()
    return result
  }
}

enum class PredictionType(val errorName: String) {
  LINEAR("MSE"),
  LOGISTIC("LogLoss"),
  SOFTMAX("SoftmaxLoss"), ;
}

const val EPS = 1e-7f

class Lobe {
  val inputNodesSize: Int
  val outputLayerSize: Int
  val maxNorm = 5f
  val predictionType: PredictionType
  val temperature: Float = 1.0f

  val rng = java.util.Random(System.currentTimeMillis())


  var amountOfTraining: Long = 0
    private set
  val layers: MutableList<Pair<D2Array<Float>, D1Array<Float>>>

  // HE init
  fun randomWeight(prevLayerSize: Int): Float {
    val std = sqrt(2f / prevLayerSize.toFloat())
    return (rng.nextGaussian() * std).toFloat()
  }

  constructor(
    inputNodesSize: Int,
    outputLayerSize: Int,
    intermediateLayerSizes: List<Int>,
    predictionType: PredictionType
  ) {
    this.inputNodesSize = inputNodesSize
    this.outputLayerSize = outputLayerSize
    this.predictionType = predictionType

    var previousLayerSize = this.inputNodesSize

    val layersToUse = mutableListOf<Pair<D2Array<Float>, D1Array<Float>>>()
    for (currentLayerSize in intermediateLayerSizes) {
      val a = mk.d2array(currentLayerSize, previousLayerSize) { randomWeight(previousLayerSize) }
      val b = mk.d1array(currentLayerSize) { 0.0f }
      previousLayerSize = currentLayerSize
      layersToUse.add(Pair(a, b))
    }
    val lastLayerA = mk.d2array(outputLayerSize, previousLayerSize) { randomWeight(previousLayerSize) }
    val lastLayerB = mk.d1array(outputLayerSize) { 0.0f }
    layersToUse.add(Pair(lastLayerA, lastLayerB))

    layers = layersToUse
  }

  constructor(loadFromFile: String) {
    DataInputStream(BufferedInputStream(FileInputStream(loadFromFile))).use { inputStream ->
      try {
        amountOfTraining = inputStream.readLong()
        val amountOfLayers = inputStream.readInt()
        this.inputNodesSize = inputStream.readInt()
        this.outputLayerSize = inputStream.readInt()
        this.predictionType = inputStream.readInt().let { PredictionType.entries[it] }

        val layerSizes = List(amountOfLayers) { inputStream.readInt() }
        val layersToUse = mutableListOf<Pair<D2Array<Float>, D1Array<Float>>>()

        var previousLayerSize = inputNodesSize

        layerSizes.forEachIndexed { index, currentLayerSize ->
          val a = mk.d2array(currentLayerSize, previousLayerSize) { inputStream.readFloat() }
          val b = mk.d1array(currentLayerSize) { inputStream.readFloat() }
          previousLayerSize = currentLayerSize

          layersToUse.add(Pair(a, b))
        }

        layers = layersToUse
      } catch (_: EOFException) {
        throw AssertionError("Unexpected end of file while loading brain from $loadFromFile")
      }
    }
  }

  fun save(saveToFile: String) {
    DataOutputStream(BufferedOutputStream(FileOutputStream(saveToFile, false))).use { outputStream ->
      outputStream.writeLong(amountOfTraining)
      outputStream.writeInt(layers.size)
      outputStream.writeInt(inputNodesSize)
      outputStream.writeInt(outputLayerSize)
      outputStream.writeInt(predictionType.ordinal)

      layers.forEach { layer ->
        val currentLayerSize = layer.second.size
        outputStream.writeInt(currentLayerSize) // since the dimensionality of b equals the amount of neurons in its layer
      }
      layers.forEach {
        val (a, b) = it
        a.forEach { value -> outputStream.writeFloat(value) }
        b.forEach { value -> outputStream.writeFloat(value) }
      }
    }
  }

  fun softmax(z: D1Array<Float>): D1Array<Float> {
    var max = Float.NEGATIVE_INFINITY
    for (i in z.indices) if (z[i] > max) max = z[i]

    val e = FloatArray(z.size) { exp(z[it] - max) / temperature }   // subtract max for numerical stability
    val sum = e.sum()
    return mk.ndarray(FloatArray(z.size) { e[it] / sum })
  }

  fun sigmoid(value: Float): Float {
    return 1.0f / (1.0f + exp(-value))
  }

  fun activationFunction(value: Float): Float {
    return max(0.1f * value, value)
  }

  fun activationFunctionDerivative(value: Float): Float {
    return if (value > 0) 1.0f else 0.1f
  }

  fun think(inputLayer: FloatArray, mask: FloatArray): FloatArray {
    assert(inputLayer.size == inputNodesSize) { "Input layer size must be $inputNodesSize, but was ${inputLayer.size}" }

    var currentActivationValues: D1Array<Float> = mk.ndarray(inputLayer)
    for ((a: D2Array<Float>, b: D1Array<Float>) in layers.subList(0, layers.size - 1)) {
      currentActivationValues = ((a dot currentActivationValues) + b).map { activationFunction(it) }
    }
    val lastNodeValuesMasked: D1Array<Float> =
      mk.ndarray(mask) * ((layers.last().first dot currentActivationValues) + layers.last().second)

    if (predictionType == PredictionType.SOFTMAX || predictionType == PredictionType.LOGISTIC) {
      mask.forEachIndexed { index, value -> if (value == 0.0f) lastNodeValuesMasked[index] = Float.NEGATIVE_INFINITY }
    }

    val lastActivationValues: D1Array<Float> = when (predictionType) {
      PredictionType.LINEAR -> lastNodeValuesMasked.map { activationFunction(it) }
      PredictionType.LOGISTIC -> lastNodeValuesMasked.map { sigmoid(it) }
      PredictionType.SOFTMAX -> softmax(lastNodeValuesMasked)
    }

    assert(lastActivationValues.size == outputLayerSize) { "Output layer size must be $outputLayerSize, but was ${currentActivationValues.size}" }

    return lastActivationValues.toFloatArray()
  }

  fun evaluateError(samples: List<TrainingSample>): Float {
    return when (predictionType) {
      PredictionType.LINEAR -> evaluateErrorMSE(samples)
      PredictionType.LOGISTIC -> evaluateErrorLogLoss(samples)
      PredictionType.SOFTMAX -> evaluateErrorSoftMax(samples)
    }
  }

  fun evaluateErrorMSE(samples: List<TrainingSample>): Float {
    if (samples.isEmpty()) {
      return Float.NaN
    }
    return samples
      .map { sample ->
        val error = (mk.ndarray(think(sample.input, sample.mask)) - mk.ndarray(sample.expectedOutput)).map { it * it }
        return@map error
      }
      .reduce { acc, unit -> acc + unit }
      .map { it / samples.size }
      .toFloatArray()
      .reduce { acc, f -> acc + f }
  }

  fun evaluateErrorLogLoss(samples: List<TrainingSample>): Float {
    if (samples.isEmpty()) {
      return Float.NaN
    }
    return samples
      .map { sample ->
        val predicted = think(sample.input, sample.mask)
        val actual = sample.expectedOutput

        val error = mk.d1array(predicted.size) { 0.0f }
        for (i in predicted.indices) {
          val p = predicted[i].coerceIn(EPS, 1.0f - EPS)
          val y = actual[i]
          error[i] = y * kotlin.math.ln(p) + (1 - y) * kotlin.math.ln(1 - p)
        }
        return@map error
      }
      .reduce { acc, unit -> acc + unit }
      .map { it / samples.size }
      .toFloatArray()
      .reduce { acc, f -> acc + f } * -1.0f / samples[0].expectedOutput.size
  }

  fun evaluateErrorSoftMax(samples: List<TrainingSample>): Float {
    if (samples.isEmpty()) {
      return Float.NaN
    }
    return samples
      .map { sample ->
        val predicted = think(sample.input, sample.mask)
        val actual = sample.expectedOutput

        var error = 0.0f
        for (i in actual.indices) {
          if (actual[i] > 0) {
            val p = predicted[i].coerceAtLeast(EPS)
            error += -actual[i] * kotlin.math.ln(p)
          }
        }
        return@map error
      }
      .sum()
      .let { it / samples.size }
  }

  fun massTrain(samples: List<TrainingSample>) {
    if (samples.isEmpty()) {
      return
    }
    var acc: List<Pair<D2Array<Float>, D1Array<Float>>> = layers.map { layer ->
      Pair(mk.zeros(layer.first.shape[0], layer.first.shape[1]), mk.zeros(layer.second.size))
    }
    samples.forEach {
      val singleWeightCorrection = trainSingle(it)
      acc = acc.mapIndexed { index, pair ->
        Pair(pair.first + singleWeightCorrection[index].first, pair.second + singleWeightCorrection[index].second)
      }
    }
    acc = acc.map { (a, b) -> Pair(a.map { it / samples.size }, b.map { it / samples.size }) }

    val norm = sqrt(acc.map { (a, b) -> a.map { it * it }.sum() + b.map { it * it }.sum() }.sum())
    if (norm > maxNorm) {
      val scale = maxNorm / norm
      // print("\nNormalizing accumulated gradients with norm $norm")
      acc = acc.map { (a, b) -> Pair(a.map { it * scale }, b.map { it * scale }) }

      //val newNorm = sqrt(acc.map { (a, b) -> a.map { it * it }.sum() + b.map { it * it }.sum() }.sum())
      //print("\nNew norm after normalization: $newNorm")
    }


    for (li in layers.indices) {
      layers[li] = Pair(layers[li].first - acc[li].first, layers[li].second - acc[li].second)
    }
    amountOfTraining += samples.size


  }

  fun trainSingle(sample: TrainingSample): List<Pair<D2Array<Float>, D1Array<Float>>> {
    assert(sample.input.size == inputNodesSize) { "Input layer size must be $inputNodesSize, but was ${sample.input.size}" }
    assert(sample.expectedOutput.size == outputLayerSize) { "Expected output layer size must be $outputLayerSize, but was ${sample.expectedOutput.size}" }
    assert(sample.mask.size == inputNodesSize) { "Mask layer size must be $inputNodesSize, but was ${sample.mask.size}" }
    val activations = mutableListOf<D1Array<Float>>();

    activations.add(mk.ndarray(sample.input))
    for ((a: D2Array<Float>, b: D1Array<Float>) in layers.subList(0, layers.size - 1)) {
      activations.add(((a dot activations.last()) + b).map { activationFunction(it) })
    }
    val lastNodesMasked = mk.ndarray(sample.mask) * ((layers.last().first dot activations.last()) + layers.last().second)
    if (predictionType == PredictionType.SOFTMAX || predictionType == PredictionType.LOGISTIC) {
      sample.mask.forEachIndexed { index, value -> if (value == 0.0f) lastNodesMasked[index] = Float.NEGATIVE_INFINITY }
    }
    activations.add(
      when (predictionType) {
      PredictionType.LINEAR -> lastNodesMasked.map { activationFunction(it) }
      PredictionType.LOGISTIC -> lastNodesMasked.map { sigmoid(it) }
      PredictionType.SOFTMAX -> softmax(lastNodesMasked)
    })

    assert(activations.size == layers.size + 1)

    val gradientWeights: MutableList<Pair<D2Array<Float>, D1Array<Float>>> =
      mutableListOf<Pair<D2Array<Float>, D1Array<Float>>>()

    val error = activations.last() - mk.ndarray(sample.expectedOutput)
    var delta: D1Array<Float> = when (predictionType) {
      PredictionType.LINEAR -> error * activations.last().map { activationFunctionDerivative(it) }
      PredictionType.LOGISTIC -> error
      PredictionType.SOFTMAX -> error / temperature
    }
    for (li in layers.indices.reversed()) {
      val delLinearFunctionDelWeight: D2Array<Float> = activations[li].reshape(1, activations[li].size)

      val dW: D2Array<Float> = delta.reshape(delta.size, 1) dot delLinearFunctionDelWeight
      val dB: D1Array<Float> = delta
      //println("li  :       " + li)
      //println("delta:      " + delta.shape.contentToString())
      //println("dw          " + dW.shape.contentToString())
      //println("db          " + dB.shape.contentToString())
      gradientWeights.add(Pair(dW, dB))

      if (li == 0) {
        continue
      }
      val propagatedD2: D2Array<Float> = (layers[li].first.transpose() dot delta.reshape(delta.size, 1))
      val f2: D1Array<Float> = activations[li].map { activationFunctionDerivative(it) }
      //println("layers[]    " + layers[li].first.transpose().shape.contentToString())
      //println("propagated: " + propagated.shape.contentToString())
      //println("f2:         " + f2.shape.contentToString())
      delta = propagatedD2.reshape(propagatedD2.size) * f2
    }

    gradientWeights.reverse()
    return gradientWeights
  }
}
