package de.nekrozofbrionac.mahjong.player.control.bot.ml

import de.nekrozofbrionac.mahjong.game.PlayerPov
import de.nekrozofbrionac.mahjong.game.Turn
import org.jetbrains.kotlinx.multik.api.d1array
import org.jetbrains.kotlinx.multik.api.d2array
import org.jetbrains.kotlinx.multik.api.linalg.dot
import org.jetbrains.kotlinx.multik.api.mk
import org.jetbrains.kotlinx.multik.api.ndarray
import org.jetbrains.kotlinx.multik.api.zeros
import org.jetbrains.kotlinx.multik.ndarray.data.D1Array
import org.jetbrains.kotlinx.multik.ndarray.data.D2Array
import org.jetbrains.kotlinx.multik.ndarray.operations.forEach
import org.jetbrains.kotlinx.multik.ndarray.operations.map
import org.jetbrains.kotlinx.multik.ndarray.operations.minus
import org.jetbrains.kotlinx.multik.ndarray.operations.plus
import org.jetbrains.kotlinx.multik.ndarray.operations.times
import org.jetbrains.kotlinx.multik.ndarray.operations.toFloatArray
import java.io.*
import kotlin.math.max
import kotlin.random.Random

class Lobe {
  val inputNodesSize: Int
  val outputLayerSize: Int
  val randomWeightFactor = 0.001f
  val learningRate = 0.1f

  var amountOfTraining: Long = 0
    private set
  val layers: MutableList<Pair<D2Array<Float>, D1Array<Float>>>

  fun randomWeight(): Float {
    return Random.nextFloat() * randomWeightFactor
  }

  constructor(inputNodesSize: Int, outputLayerSize: Int, intermediateLayerSizes: List<Int>) {
    this.inputNodesSize = inputNodesSize
    this.outputLayerSize = outputLayerSize

    var previousLayerSize = this.inputNodesSize

    val layersToUse = mutableListOf<Pair<D2Array<Float>, D1Array<Float>>>()
    for (currentLayerSize in intermediateLayerSizes) {
      val a = mk.d2array(currentLayerSize, previousLayerSize) { randomWeight() }
      val b = mk.d1array(currentLayerSize) { randomWeight() }
      previousLayerSize = currentLayerSize
      layersToUse.add(Pair(a, b))
    }
    val lastLayerA = mk.d2array(outputLayerSize, previousLayerSize) { randomWeight() }
    val lastLayerB = mk.d1array(outputLayerSize) { randomWeight() }
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

  fun activationFunction(value: Float): Float {
    return max(0.1f * value, value)
  }

  fun activationFunctionDerivative(value: Float): Float {
    return if (value > 0) 1.0f else 0.1f
  }

  fun think(inputLayer: FloatArray): FloatArray {
    assert(inputLayer.size == inputNodesSize) { "Input layer size must be $inputNodesSize, but was ${inputLayer.size}" }

    var currentActivationValues: D1Array<Float> = mk.ndarray(inputLayer)
    for ((a: D2Array<Float>, b: D1Array<Float>) in layers) {
      currentActivationValues = ((a dot currentActivationValues) + b).map { activationFunction(it) }
    }

    assert(currentActivationValues.size == outputLayerSize) { "Output layer size must be $outputLayerSize, but was ${currentActivationValues.size}" }

    return currentActivationValues.toFloatArray()
  }

  fun weightCoerce(value: Float): Float {
    return value.coerceIn(-1f, 1f) * learningRate
  }

  fun evaluateError(samples: List<Pair<FloatArray, FloatArray>>): Float {
    if (samples.isEmpty()) {
      return Float.NaN
    }
    return samples
      .map { sample ->
        var currentActivationValues: D1Array<Float> = mk.ndarray(sample.first)
        for ((a: D2Array<Float>, b: D1Array<Float>) in layers) {
          currentActivationValues = ((a dot currentActivationValues) + b).map { activationFunction(it) }
        }
        val error = (currentActivationValues - mk.ndarray(sample.second)).map{ it * it }
        return@map error
      }
      .reduce { acc, unit -> acc + unit }
      .map { it / samples.size }
      .toFloatArray()
      .reduce { acc, f -> acc + f }
  }

  fun massTrain(samples: List<Pair<FloatArray, FloatArray>>) {
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
    acc = acc.map { (a, b) -> Pair(a.map{it / samples.size }, b.map{it / samples.size }) }
    acc = acc.map { (a, b) -> Pair(a.map{weightCoerce(it)}, b.map{weightCoerce(it)}) }


    for (li in layers.indices) {
      layers[li] = Pair(layers[li].first - acc[li].first, layers[li].second - acc[li].second)
    }
    amountOfTraining += samples.size


  }

  fun trainSingle(sample: Pair<FloatArray, FloatArray>): List<Pair<D2Array<Float>, D1Array<Float>>> {
    assert(sample.first.size == inputNodesSize) { "Input layer size must be $inputNodesSize, but was ${sample.first.size}" }
    assert(sample.second.size == outputLayerSize) { "Expected output layer size must be $outputLayerSize, but was ${sample.second.size}" }

    val activations = mutableListOf<D1Array<Float>>();

    activations.add(mk.ndarray(sample.first))
    for ((a: D2Array<Float>, b: D1Array<Float>) in layers) {
      activations.add(((a dot activations.last()) + b).map { activationFunction(it) })
    }
    val error = activations.last() - mk.ndarray(sample.second)

    assert(activations.size == layers.size + 1)

    val gradientWeights: MutableList<Pair<D2Array<Float>, D1Array<Float>>> = mutableListOf<Pair<D2Array<Float>, D1Array<Float>>>()

    var delta: D1Array<Float> = error * activations.last().map { activationFunctionDerivative(it) }
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

val emptyPov = PlayerPov(
  turn = 0,
  currentTurnPlayer = Turn.EAST,
  currentTurnPlayerName = "",
  minFaan = 1,
  playerSeat = Turn.EAST,
  prevailingWind = Turn.EAST,
  name = "",
  prevName = "",
  nextName = "",
  oppoName = "",
  prevSeat = Turn.EAST,
  nextSeat = Turn.EAST,
  oppoSeat = Turn.EAST,
  prevMelds = emptyList(),
  nextMelds = emptyList(),
  oppoMelds = emptyList(),
  prevFlowers = emptyList(),
  nextFlowers = emptyList(),
  oppoFlowers = emptyList(),
  discarded = emptyList(),
  wallRemaining = 0,
  hand = emptyList(),
  melds = emptyList(),
  flowers = emptyList(),
)

