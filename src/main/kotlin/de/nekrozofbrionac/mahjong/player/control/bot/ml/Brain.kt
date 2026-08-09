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
import org.jetbrains.kotlinx.multik.ndarray.data.D1Array
import org.jetbrains.kotlinx.multik.ndarray.data.D2Array
import org.jetbrains.kotlinx.multik.ndarray.operations.forEach
import org.jetbrains.kotlinx.multik.ndarray.operations.map
import org.jetbrains.kotlinx.multik.ndarray.operations.plus
import org.jetbrains.kotlinx.multik.ndarray.operations.toFloatArray
import java.io.*
import kotlin.math.max
import kotlin.random.Random

class Brain {
  val inputLayerSize = 6629
  val outputLayerSize = 22


  val layers: List<Pair<D2Array<Float>, D1Array<Float>>>

  constructor(intermediateLayerSizes: List<Int>) {

    var previousLayerSize = inputLayerSize
    val layersToUse = mutableListOf<Pair<D2Array<Float>, D1Array<Float>>>()
    for (currentLayerSize in intermediateLayerSizes) {
      val a = mk.d2array( currentLayerSize, previousLayerSize) { Random.nextFloat() }
      val b = mk.d1array(currentLayerSize) { Random.nextFloat() }
      previousLayerSize = currentLayerSize
      layersToUse.add(Pair(a, b))
    }
    val lastLayerA = mk.d2array(outputLayerSize, previousLayerSize) { 2*Random.nextFloat() - 1 }
    val lastLayerB = mk.d1array(outputLayerSize) { 2*Random.nextFloat() - 1 }
    layersToUse.add(Pair(lastLayerA, lastLayerB))

    layers = layersToUse
  }

  constructor(loadFromFile: String) {
    DataInputStream(BufferedInputStream(FileInputStream(loadFromFile))).use { inputStream ->
      try {
        val amountOfLayers = inputStream.readInt()
        val layerSizes = List(amountOfLayers) { inputStream.readInt() }
        assert(layerSizes.last() == outputLayerSize) { "The last layer size must be $outputLayerSize, but was ${layerSizes.last()}" }

        val layersToUse = mutableListOf<Pair<D2Array<Float>, D1Array<Float>>>()
        var previousLayerSize = inputLayerSize
        layerSizes.forEachIndexed { index, currentLayerSize ->
          val a = mk.d2array(currentLayerSize, previousLayerSize) { inputStream.readFloat() }
          val b = mk.d1array(currentLayerSize) { inputStream.readFloat() }
          previousLayerSize = currentLayerSize

          layersToUse.add(Pair(a, b))
        }

        assert(layersToUse.last().second.size == outputLayerSize) { "The last layer's size must be $outputLayerSize, but was ${layersToUse.last().second.size}" }

        layers = layersToUse
      } catch (_: EOFException) {
        throw AssertionError("Unexpected end of file while loading brain from $loadFromFile")
      }
    }
  }

  fun save(saveToFile: String) {
    DataOutputStream(BufferedOutputStream(FileOutputStream(saveToFile, false))).use { outputStream ->
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
    assert(inputLayer.size == inputLayerSize) { "Input layer size must be $inputLayerSize, but was ${inputLayer.size}" }

    var currentActivationValues: D1Array<Float> = mk.ndarray(inputLayer)
    for ((a: D2Array<Float>, b: D1Array<Float>) in layers) {
      currentActivationValues = ((a dot currentActivationValues) + b).map { activationFunction(it) }
    }
    assert(currentActivationValues.size == outputLayerSize) { "Output layer size must be 22, but was ${currentActivationValues.size}" }

    return floatToThought(currentActivationValues.toFloatArray())
  }

  fun train(samples: List<Pair<FloatArray, FloatArray>>) {
  }
}

/*
  | Value             | Encoding       |
  |-------------------|----------------|
  | Action            | Action (HOE)   |
  | minFaan           | Int            |
  | prevailingWind    | Turn (HOE)     |
  | playerSeat        | Turn (HOE)     |
  | prevSeat          | Turn (HOE)     |
  | nextSeat          | Turn (HOE)     |
  | oppoSeat          | Turn (HOE)     |
  | turn              | Int            |
  | wallReminaing     | Int            |
  | currentTurnPlayer | Turn (Int 0-3) |
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

  6 + 1 + 5*4 + 1 + 1 + 4 + 34*4*4*3 + 34*8*3 + 34*84 + 34*14 + 34*4*4 + 34*8
  6629
 */
const val tileEncodingSize = 34
const val sizePerMeld = 4 * tileEncodingSize
const val prevMeldFirstIndex = 6 + 1 + 5 * 4 + 2 + 4
const val nextMeldFirstIndex = prevMeldFirstIndex + 4 * sizePerMeld
const val oppoMeldFirstIndex = nextMeldFirstIndex + 4 * sizePerMeld
const val prevFlowerIndex = oppoMeldFirstIndex + 4 * sizePerMeld
const val nextFlowerIndex = prevFlowerIndex + 8 * tileEncodingSize
const val oppoFlowerIndex = nextFlowerIndex + 8 * tileEncodingSize
const val discardedIndex = oppoFlowerIndex + 8 * tileEncodingSize
const val handIndex = discardedIndex + 84 * tileEncodingSize
const val meldsIndex = handIndex + 14 * tileEncodingSize
const val flowersIndex = meldsIndex + 4 * sizePerMeld
const val inputLayerSize = flowersIndex + 8 * tileEncodingSize
fun povToInputLayer(action: PlayerControlAction, pov: PlayerPov): FloatArray {
  /* val output = FloatArray(
    1 + 5 * 4 + 1 + 1 + 4
        + tileEncodingSize * 4 * 4 * 3
        + tileEncodingSize * 8 * 3
        + tileEncodingSize * 84
        + tileEncodingSize * 14
        + tileEncodingSize * 4 * 4
        + tileEncodingSize * 8
  ) */
  val output = FloatArray(inputLayerSize)
  output[0 + action.ordinal] = 1.0f
  output[6] = pov.minFaan.toFloat()
  output[6 + 1 + 4 * 0 + pov.prevailingWind.ordinal] = 1.0f
  output[6 + 1 + 4 * 1 + pov.playerSeat.ordinal] = 1.0f
  output[6 + 1 + 4 * 2 + pov.prevSeat.ordinal] = 1.0f
  output[6 + 1 + 4 * 3 + pov.nextSeat.ordinal] = 1.0f
  output[6 + 1 + 4 * 4 + pov.oppoSeat.ordinal] = 1.0f
  output[6 + 1 + 4 * 5 + 0] = pov.turn.toFloat()
  output[6 + 1 + 4 * 5 + 1] = pov.wallRemaining.toFloat()
  output[6 + 1 + 4 * 5 + 2 + pov.currentTurnPlayer.ordinal] = 1.0f
  // first tile of prevmelds: 1 + 4*4 + 2 + 4
  if (pov.prevMelds.size > 4) {
    throw AssertionError("why does someone have more than 4 melds")
  }
  pov.prevMelds.forEachIndexed { m_i: Int, tiles: List<TileType> ->
    tiles.forEachIndexed { t_i: Int, tile: TileType ->
      tileTypeToWeights(tile).forEachIndexed { t_ii, fl ->
        output[prevMeldFirstIndex + m_i * sizePerMeld + t_i * tileEncodingSize + t_ii] = fl
      }
    }
  }
  pov.nextMelds.forEachIndexed { m_i, tiles ->
    tiles.forEachIndexed { t_i, tile ->
      tileTypeToWeights(tile).forEachIndexed { t_ii, fl ->
        output[nextMeldFirstIndex + m_i * sizePerMeld + t_i * tileEncodingSize + t_ii] = fl
      }
    }
  }
  pov.oppoMelds.forEachIndexed { m_i, tiles ->
    tiles.forEachIndexed { t_i, tile ->
      tileTypeToWeights(tile).forEachIndexed { t_ii, fl ->
        output[oppoMeldFirstIndex + m_i * sizePerMeld + t_i * tileEncodingSize + t_ii] = fl
      }
    }
  }

  pov.prevFlowers.forEachIndexed { fi, ft ->
    tileTypeToWeights(ft).forEachIndexed { fii, fl ->
      output[prevFlowerIndex + fi * tileEncodingSize + fii]
    }
  }
  pov.nextFlowers.forEachIndexed { fi, ft ->
    tileTypeToWeights(ft).forEachIndexed { fii, fl ->
      output[nextFlowerIndex + fi * tileEncodingSize + fii]
    }
  }
  pov.oppoFlowers.forEachIndexed { fi, ft ->
    tileTypeToWeights(ft).forEachIndexed { fii, fl ->
      output[oppoFlowerIndex + fi * tileEncodingSize + fii]
    }
  }
  pov.discarded.forEachIndexed { di, tile ->
    tileTypeToWeights(tile).forEachIndexed { dii, fl ->
      output[discardedIndex + di * tileEncodingSize + dii]
    }
  }
  pov.hand.forEachIndexed { hi, tile ->
    tileTypeToWeights(tile).forEachIndexed { hii, fl ->
      output[handIndex + hi * tileEncodingSize + hii]
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
      output[flowersIndex + hi * tileEncodingSize + hii]
    }
  }
  return output
}


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

