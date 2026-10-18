package de.nekrozofbrionac.mahjong.player.control.bot.ml

import de.nekrozofbrionac.mahjong.board.TileType
import de.nekrozofbrionac.mahjong.determineChowPossibilities
import de.nekrozofbrionac.mahjong.formatTile
import de.nekrozofbrionac.mahjong.formatTiles
import de.nekrozofbrionac.mahjong.game.PlayerPov
import de.nekrozofbrionac.mahjong.player.control.PlayerControl
import de.nekrozofbrionac.mahjong.printPov
import kotlin.random.Random

class GamblingNeuralNetBot(
  val verbose: Boolean = false,
  val brain: Brain,
  val gamblingFactors: Map<PlayerControlAction, Float>
) : PlayerControl {
  private fun printPovIfVerbose(pov: PlayerPov) {
    if (verbose) {
      printPov(pov)
    }
  }

  private fun printlnIfVerbose(string: String) {
    if (verbose) {
      println(string)
    }
  }

  override fun win(pov: PlayerPov): Boolean {
    val thought = brain.thinkAbout(PlayerControlAction.WIN, pov)
    printPovIfVerbose(pov)
    printlnIfVerbose("GamblingNeuralNetBot action: win")
    printlnIfVerbose("GamblingNeuralNetBot (${pov.playerSeat}) says: ${thought.contentToString()}")
    printlnIfVerbose("Winning anyway, anything else is boring.")
    return true
  }

  fun formatTileAndThought(tile: TileType, thought: FloatArray): String {
    return "${formatTile(tile)} ${"%.3f".format(thought[tile.ordinal])}"
  }

  override fun chooseDiscard(pov: PlayerPov): TileType {
    val gamblingFactor = gamblingFactors[PlayerControlAction.CHOOSE_DISCARD] ?: 0.0f
    val thought = brain.thinkAbout(PlayerControlAction.CHOOSE_DISCARD, pov)
    val rawOdds = pov.hand.distinct().map { tile -> Pair(tile, thought[tile.ordinal] + gamblingFactor) }
    printPovIfVerbose(pov)
    printlnIfVerbose("GamblingNeuralNetBot action: chooseDiscard")
    printlnIfVerbose("GamblingNeuralNetBot says: ${thought.contentToString()}")
    printlnIfVerbose("                           The nearest existing selection is  ${rawOdds.map { formatTileAndThought(it.first, thought) }}")

    val totalWeights = rawOdds.sumOf{ it.second.toDouble() }.toFloat()
    var random = Random.nextFloat() * totalWeights
    for ((tile, tileOdds) in rawOdds) {
      random -= tileOdds
      if (random <= 0) {
        printlnIfVerbose("GamblingNeuralNetBot randomly selected tile: ${formatTileAndThought(tile, thought)}")
        return tile
      }
    }
    printlnIfVerbose("GamblingNeuralNetBot defaulted to best tile.")
    return pov.hand.distinct().sortedByDescending { tile -> thought[tile.ordinal] }[0]
  }

  override fun kongFromDraw(pov: PlayerPov): Boolean {
    val gamblingFactor = gamblingFactors[PlayerControlAction.KONG_FROM_DRAW] ?: 0.0f
    val thought = brain.thinkAbout(PlayerControlAction.KONG_FROM_DRAW, pov)
    printPovIfVerbose(pov)
    printlnIfVerbose("GamblingNeuralNetBot action: kongFromDraw")
    printlnIfVerbose("GamblingNeuralNetBot (${pov.playerSeat}) says: ${thought.contentToString()}")
    val roll = Random.nextFloat()
    printlnIfVerbose("GamblingNeuralNetBot rolled: $roll, threshold: ${thought[0]}")
    return thought[0].coerceIn(gamblingFactor, 1.0f - gamblingFactor) >= roll
  }

  override fun kongFromDiscard(pov: PlayerPov): Boolean {
    val gamblingFactor = gamblingFactors[PlayerControlAction.KONG_FROM_DISCARD] ?: 0.0f
    val thought = brain.thinkAbout(PlayerControlAction.KONG_FROM_DISCARD, pov)
    printPovIfVerbose(pov)
    printlnIfVerbose("GamblingNeuralNetBot action: kongFromDiscard")
    printlnIfVerbose("GamblingNeuralNetBot says: ${thought.contentToString()}")
    val roll = Random.nextFloat()
    printlnIfVerbose("GamblingNeuralNetBot rolled: $roll, threshold: ${thought[0]}")
    return thought[0].coerceIn(gamblingFactor, 1.0f - gamblingFactor) >= roll
  }

  override fun pongFromDiscard(pov: PlayerPov): Boolean {
    val gamblingFactor = gamblingFactors[PlayerControlAction.PONG_FROM_DISCARD] ?: 0.0f
    val thought = brain.thinkAbout(PlayerControlAction.PONG_FROM_DISCARD, pov)
    printPovIfVerbose(pov)
    printlnIfVerbose("GamblingNeuralNetBot action: pongFromDiscard")
    printlnIfVerbose("GamblingNeuralNetBot says: ${thought.contentToString()}")
    val roll = Random.nextFloat()
    printlnIfVerbose("GamblingNeuralNetBot rolled: $roll, threshold: ${thought[0]}")
    return thought[0].coerceIn(gamblingFactor, 1.0f - gamblingFactor) >= roll
  }

  override fun chowFromDiscard(pov: PlayerPov): TileType? {
    val gamblingFactor = gamblingFactors[PlayerControlAction.CHOW_FROM_DISCARD] ?: 0.0f
    val thought = brain.thinkAbout(PlayerControlAction.CHOW_FROM_DISCARD, pov)
    printPovIfVerbose(pov)
    printlnIfVerbose("GamblingNeuralNetBot action: chowFromDiscard")
    printlnIfVerbose("GamblingNeuralNetBot says: ${thought.contentToString()}")

    val discardedTile = pov.discarded.lastOrNull() ?: return null
    val possibleChows = determineChowPossibilities(pov.hand, discardedTile)
    if (possibleChows.isEmpty()) {
      throw IllegalStateException("No possible chows found, but game suggested to chow.")
    }

    printlnIfVerbose("NeuralNetBot not chowing at all score: ${thought[0]}")
    val chowScoresForPossibleChows: MutableList<Pair<Float, TileType?>> = possibleChows.map{ chow ->
      if (chow[0].numeral == discardedTile.numeral?.minus(2)) {
        printlnIfVerbose("NeuralNetBot chow score: ${thought[1]} ${ formatTiles(chow, 3, "")}")
        Pair(thought[1] + gamblingFactor, chow[0]) // score for chow starting with the tile two below the discarded tile
      } else if (chow[0].numeral == discardedTile.numeral?.minus(1)) {
        printlnIfVerbose("NeuralNetBot chow score: ${thought[2]} ${ formatTiles(chow, 3, "")}")
        Pair(thought[2] + gamblingFactor, chow[0]) // score for chow starting with the tile one below the discarded tile
      } else if (chow[0].numeral == discardedTile.numeral) {
        printlnIfVerbose("NeuralNetBot chow score: ${thought[3]} ${ formatTiles(chow, 3, "")}")
        Pair(thought[3] + gamblingFactor, chow[0]) // score for chow starting with the discarded tile itself
      } else {
        Pair (Float.NEGATIVE_INFINITY, chow[0])
      }
    }.toMutableList()
    chowScoresForPossibleChows.add(Pair(thought[0] + gamblingFactor, null)) // score for not chowing at all

    val totalWeights = chowScoresForPossibleChows.sumOf{ it.first.toDouble() }.toFloat()
    var random = Random.nextFloat() * totalWeights
    for ((tileOdds, tile) in chowScoresForPossibleChows) {
      random -= tileOdds
      if (random <= 0) {
        printlnIfVerbose("GamblingNeuralNetBot randomly selected: ${formatTile(tile)} with score: ${"0.3f".format(tileOdds)}")
        return tile
      }
    }
    val chosenChow = chowScoresForPossibleChows.maxBy { it.first }
    if (thought[0] >= chosenChow.first) {
      return null
    }
    return chosenChow.second
  }
}