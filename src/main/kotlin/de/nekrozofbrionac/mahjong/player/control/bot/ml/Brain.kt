package de.nekrozofbrionac.mahjong.player.control.bot.ml

import de.nekrozofbrionac.mahjong.board.TileType
import de.nekrozofbrionac.mahjong.game.PlayerPov
import de.nekrozofbrionac.mahjong.game.Turn
import de.nekrozofbrionac.mahjong.player.control.PlayerControlAction
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

class Brain {
  val randomWeightFactor = 0.001f
  val learningRate = 0.1f

  var amountOfTraining: Long = 0
    private set
  val layers: MutableList<Pair<D2Array<Float>, D1Array<Float>>>

  fun randomWeight(): Float {
    return Random.nextFloat() * randomWeightFactor
  }

  constructor(intermediateLayerSizes: List<Int>) {
    var previousLayerSize = INPUT_LAYER_SIZE
    val layersToUse = mutableListOf<Pair<D2Array<Float>, D1Array<Float>>>()
    for (currentLayerSize in intermediateLayerSizes) {
      val a = mk.d2array(currentLayerSize, previousLayerSize) { randomWeight() }
      val b = mk.d1array(currentLayerSize) { randomWeight() }
      previousLayerSize = currentLayerSize
      layersToUse.add(Pair(a, b))
    }
    val lastLayerA = mk.d2array(OUTPUT_LAYER_SIZE, previousLayerSize) { randomWeight() }
    val lastLayerB = mk.d1array(OUTPUT_LAYER_SIZE) { randomWeight() }
    layersToUse.add(Pair(lastLayerA, lastLayerB))

    layers = layersToUse
  }

  constructor(loadFromFile: String) {
    DataInputStream(BufferedInputStream(FileInputStream(loadFromFile))).use { inputStream ->
      try {
        amountOfTraining = inputStream.readLong()
        val amountOfLayers = inputStream.readInt()
        val layerSizes = List(amountOfLayers) { inputStream.readInt() }
        assert(layerSizes.last() == OUTPUT_LAYER_SIZE) { "The last layer size must be $OUTPUT_LAYER_SIZE, but was ${layerSizes.last()}" }

        val layersToUse = mutableListOf<Pair<D2Array<Float>, D1Array<Float>>>()
        var previousLayerSize = INPUT_LAYER_SIZE
        layerSizes.forEachIndexed { index, currentLayerSize ->
          val a = mk.d2array(currentLayerSize, previousLayerSize) { inputStream.readFloat() }
          val b = mk.d1array(currentLayerSize) { inputStream.readFloat() }
          previousLayerSize = currentLayerSize

          layersToUse.add(Pair(a, b))
        }

        assert(layersToUse.last().second.size == OUTPUT_LAYER_SIZE) { "The last layer's size must be $OUTPUT_LAYER_SIZE, but was ${layersToUse.last().second.size}" }

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

  fun think(action: PlayerControlAction, pov: PlayerPov): Thought {
    val inputLayer = povToInputLayer(action, pov)
    return think(inputLayer)
  }

  fun think(inputLayer: FloatArray): Thought {
    assert(inputLayer.size == INPUT_LAYER_SIZE) { "Input layer size must be $INPUT_LAYER_SIZE, but was ${inputLayer.size}" }

    var currentActivationValues: D1Array<Float> = mk.ndarray(inputLayer)
    for ((a: D2Array<Float>, b: D1Array<Float>) in layers) {
      currentActivationValues = ((a dot currentActivationValues) + b).map { activationFunction(it) }
    }
    assert(currentActivationValues.size == OUTPUT_LAYER_SIZE) { "Output layer size must be 22, but was ${currentActivationValues.size}" }

    return floatToThought(currentActivationValues.toFloatArray())
  }

  fun weightCoerce(value: Float): Float {
    return value.coerceIn(-1f, 1f) * learningRate
  }

  fun evaluateError(samples: List<Pair<FloatArray, FloatArray>>): Float {
    return samples
      .map {
        var currentActivationValues: D1Array<Float> = mk.ndarray(it.first)
        for ((a: D2Array<Float>, b: D1Array<Float>) in layers) {
          currentActivationValues = ((a dot currentActivationValues) + b).map { activationFunction(it) }
        }
        val error = (currentActivationValues - mk.ndarray(it.second)).map{ it * it }
        return@map error
      }
      .reduce { acc, unit -> acc + unit }
      .map { it / samples.size }
      .toFloatArray()
      .reduce { acc, f -> acc + f }
  }

  fun massTrain(samples: List<Pair<FloatArray, FloatArray>>) {
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
    assert(sample.first.size == INPUT_LAYER_SIZE) { "Input layer size must be $INPUT_LAYER_SIZE, but was ${sample.first.size}" }
    assert(sample.second.size == OUTPUT_LAYER_SIZE) { "Expected output layer size must be $OUTPUT_LAYER_SIZE, but was ${sample.second.size}" }

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

/*
  | Value             | Encoding       |
  |-------------------|----------------|
  | Action            | Action (HOE)   |
  | minFaan           | HOE 0-8        |
  | prevailingWind    | Turn (HOE)     |
  | playerSeat        | Turn (HOE)     |
  | prevSeat          | Turn (HOE)     |
  | nextSeat          | Turn (HOE)     |
  | oppoSeat          | Turn (HOE)     |
  | turn              | Int            |
  | wallReminaing     | Int            |
  | currentTurnPlayer | Turn (HOE)     |
  | prevMelds         | TileType[4][4] |
  | nextMelds         | TileType[4][4] |
  | oppoMelds         | TileType[4][4] |
  | prevFlowers       | TileType[8]    |
  | nextFlowers       | TileType[8]    |
  | oppoFlowers       | TileType[8]    |
  | discarded         | TileType[84]   |
  | hand              | TileType[14]   |
  | melds             | TileType[4][4] |
  | flowers           | TileType[8]    |

  turns are hotoneencoded

 */
const val actionIndex = 0
val minFaanIndex = actionIndex + PlayerControlAction.entries.size
val prevailingWindIndex = minFaanIndex + 9
val playerSeatIndex = prevailingWindIndex + Turn.entries.size
val prevSeatIndex = playerSeatIndex + Turn.entries.size
val nextSeatIndex = prevSeatIndex + Turn.entries.size
val oppoSeatIndex = nextSeatIndex + Turn.entries.size
val turnIndex = oppoSeatIndex + Turn.entries.size
val wallRemainingIndex = turnIndex + 1
val currentTurnPlayerIndex = wallRemainingIndex + 1
val prevMeldsIndex = currentTurnPlayerIndex + Turn.entries.size
val nextMeldsIndex = prevMeldsIndex + 4 * 4 * tileEncodingSize
val oppoMeldsIndex = nextMeldsIndex + 4 * 4 * tileEncodingSize
val prevFlowerIndex = oppoMeldsIndex + 4 * 4 * tileEncodingSize
val nextFlowerIndex = prevFlowerIndex + 8 * tileEncodingSize
val oppoFlowerIndex = nextFlowerIndex + 8 * tileEncodingSize
val discardedIndex = oppoFlowerIndex + 8 * tileEncodingSize
val handIndex = discardedIndex + 84 * tileEncodingSize
val meldsIndex = handIndex + 14 * tileEncodingSize
val flowersIndex = meldsIndex + 4 * 4 * tileEncodingSize
val INPUT_LAYER_SIZE = flowersIndex + 8 * tileEncodingSize // currently 8189

val sizePerMeld = 4 * tileEncodingSize

fun povToInputLayer(action: PlayerControlAction, pov: PlayerPov): FloatArray {
  if (pov.minFaan !in 0..8) {
    throw AssertionError("minFaan must be between 0 and 8, but was ${pov.minFaan}")
  }

  val output = FloatArray(INPUT_LAYER_SIZE)
  output[actionIndex + action.ordinal] = 1.0f
  output[minFaanIndex + pov.minFaan + 1] = pov.minFaan.toFloat()
  output[prevailingWindIndex + pov.prevailingWind.ordinal] = 1.0f
  output[playerSeatIndex + pov.playerSeat.ordinal] = 1.0f
  output[prevSeatIndex + pov.prevSeat.ordinal] = 1.0f
  output[nextSeatIndex + pov.nextSeat.ordinal] = 1.0f
  output[oppoSeatIndex + pov.oppoSeat.ordinal] = 1.0f
  output[turnIndex] = pov.turn.toFloat()
  output[wallRemainingIndex] = pov.wallRemaining.toFloat()
  output[currentTurnPlayerIndex + pov.currentTurnPlayer.ordinal] = 1.0f
  // first tile of prevmelds: 1 + 4*4 + 2 + 4
  if (pov.prevMelds.size > 4) {
    throw AssertionError("why does someone have more than 4 melds")
  }
  pov.prevMelds.forEachIndexed { m_i: Int, tiles: List<TileType> ->
    tiles.forEachIndexed { t_i: Int, tile: TileType ->
      tileTypeToWeights(tile).forEachIndexed { t_ii, fl ->
        output[prevMeldsIndex + m_i * sizePerMeld + t_i * tileEncodingSize + t_ii] = fl
      }
    }
  }
  pov.nextMelds.forEachIndexed { m_i, tiles ->
    tiles.forEachIndexed { t_i, tile ->
      tileTypeToWeights(tile).forEachIndexed { t_ii, fl ->
        output[nextMeldsIndex + m_i * sizePerMeld + t_i * tileEncodingSize + t_ii] = fl
      }
    }
  }
  pov.oppoMelds.forEachIndexed { m_i, tiles ->
    tiles.forEachIndexed { t_i, tile ->
      tileTypeToWeights(tile).forEachIndexed { t_ii, fl ->
        output[oppoMeldsIndex + m_i * sizePerMeld + t_i * tileEncodingSize + t_ii] = fl
      }
    }
  }

  pov.prevFlowers.forEachIndexed { fi, ft ->
    tileTypeToWeights(ft).forEachIndexed { fii, fl ->
      output[prevFlowerIndex + fi * tileEncodingSize + fii] = fl
    }
  }
  pov.nextFlowers.forEachIndexed { fi, ft ->
    tileTypeToWeights(ft).forEachIndexed { fii, fl ->
      output[nextFlowerIndex + fi * tileEncodingSize + fii] = fl
    }
  }
  pov.oppoFlowers.forEachIndexed { fi, ft ->
    tileTypeToWeights(ft).forEachIndexed { fii, fl ->
      output[oppoFlowerIndex + fi * tileEncodingSize + fii] = fl
    }
  }
  pov.discarded.forEachIndexed { di, tile ->
    tileTypeToWeights(tile).forEachIndexed { dii, fl ->
      output[discardedIndex + di * tileEncodingSize + dii] = fl
    }
  }
  pov.hand.forEachIndexed { hi, tile ->
    tileTypeToWeights(tile).forEachIndexed { hii, fl ->
      output[handIndex + hi * tileEncodingSize + hii] = fl
    }
  }
  pov.melds.forEachIndexed { m_i, tiles ->
    tiles.forEachIndexed { t_i, tile ->
      tileTypeToWeights(tile).forEachIndexed { t_ii, fl ->
        output[meldsIndex + m_i * sizePerMeld + t_i * tileEncodingSize + t_ii] = fl
      }
    }
  }
  pov.flowers.forEachIndexed { hi, tile ->
    tileTypeToWeights(tile).forEachIndexed { hii, fl ->
      output[flowersIndex + hi * tileEncodingSize + hii] = fl
    }
  }
  return output
}


val OUTPUT_LAYER_SIZE = 22
data class Thought(
  val winning: Float,
  val discard01: Float,
  val discard02: Float,
  val discard03: Float,
  val discard04: Float,
  val discard05: Float,
  val discard06: Float,
  val discard07: Float,
  val discard08: Float,
  val discard09: Float,
  val discard10: Float,
  val discard11: Float,
  val discard12: Float,
  val discard13: Float,
  val discard14: Float,
  val kongFromDraw: Float,
  val kongFromDiscard: Float,
  val pongFromDiscard: Float,
  val chowFromDiscardLowest: Float,
  val chowFromDiscardMiddle: Float,
  val chowFromDiscardHighest: Float,
  val chowAtAll: Float,
)

fun floatToThought(values: FloatArray): Thought {
  return Thought(
    winning = values[0],
    discard01 = values[1],
    discard02 = values[2],
    discard03 = values[3],
    discard04 = values[4],
    discard05 = values[5],
    discard06 = values[6],
    discard07 = values[7],
    discard08 = values[8],
    discard09 = values[9],
    discard10 = values[10],
    discard11 = values[11],
    discard12 = values[12],
    discard13 = values[13],
    discard14 = values[14],
    kongFromDraw = values[15],
    kongFromDiscard = values[16],
    pongFromDiscard = values[17],
    chowFromDiscardLowest = values[18],
    chowFromDiscardMiddle = values[19],
    chowFromDiscardHighest = values[20],
    chowAtAll = values[21]
  )
}

fun thoughtToFloat(thought: Thought): FloatArray {
  return floatArrayOf(
    thought.winning,
    thought.discard01,
    thought.discard02,
    thought.discard03,
    thought.discard04,
    thought.discard05,
    thought.discard06,
    thought.discard07,
    thought.discard08,
    thought.discard09,
    thought.discard10,
    thought.discard11,
    thought.discard12,
    thought.discard13,
    thought.discard14,
    thought.kongFromDraw,
    thought.kongFromDiscard,
    thought.pongFromDiscard,
    thought.chowFromDiscardLowest,
    thought.chowFromDiscardMiddle,
    thought.chowFromDiscardHighest,
    thought.chowAtAll
  )
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

