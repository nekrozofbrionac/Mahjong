package de.nekrozofbrionac.mahjong.player.control.bot.ml

import de.nekrozofbrionac.mahjong.board.TileType
import de.nekrozofbrionac.mahjong.determineChowPossibilities
import de.nekrozofbrionac.mahjong.formatTile
import de.nekrozofbrionac.mahjong.game.PlayerPov
import de.nekrozofbrionac.mahjong.player.control.PlayerControl
import de.nekrozofbrionac.mahjong.printPov

data class RecordedGame(
  val actions: List<RecordedDecision>,
  val win: Boolean,
)

data class RecordedDecision(
  val action: PlayerControlAction,
  val pov: PlayerPov,
  val pred: FloatArray,
  val takenDecision: FloatArray,
) {
  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (javaClass != other?.javaClass) return false

    other as RecordedDecision

    if (action != other.action) return false
    if (pov != other.pov) return false
    if (!pred.contentEquals(other.pred)) return false
    if (!takenDecision.contentEquals(other.takenDecision)) return false

    return true
  }

  override fun hashCode(): Int {
    var result = action.hashCode()
    result = 31 * result + pov.hashCode()
    result = 31 * result + pred.contentHashCode()
    result = 31 * result + takenDecision.contentHashCode()
    return result
  }
}

class NeuralNetRecordingBot(
  val brain: Brain,
  val verbose: Boolean = false,
) : PlayerControl {
  val gameRecords = mutableListOf<RecordedGame>()
  val currentGameRecording: MutableList<RecordedDecision> = mutableListOf()
  var currentGameWon = false

  fun popRecordedGames(): List<RecordedGame> {
    val output = gameRecords.toList()
    gameRecords.clear()
    return output
  }

  override fun endGame() {
    gameRecords.add(RecordedGame(currentGameRecording.toList(), currentGameWon))
    currentGameRecording.clear()
    currentGameWon = false
  }

  override fun win(pov: PlayerPov): Boolean {
    val pred = brain.thinkAbout(PlayerControlAction.WIN, pov)

    printPovIfVerbose(pov)
    printlnIfVerbose("Action    : Win")
    printlnIfVerbose("Prediction: ${pred[0]}")



    currentGameWon = pred[0] >= 0.5

    val takenDecision = FloatArray(1) { if (currentGameWon) 1.0f else 0.0f }
    currentGameRecording.add(RecordedDecision(PlayerControlAction.WIN, deepPovCopy(pov), pred, takenDecision))
    return currentGameWon
  }

  override fun chooseDiscard(pov: PlayerPov): TileType {
    val pred = brain.thinkAbout(PlayerControlAction.CHOOSE_DISCARD, pov)
    val bestTile = pov.hand.distinct().sortedByDescending { tile -> pred[tile.ordinal] }

    printPovIfVerbose(pov)
    printlnIfVerbose("Action    : Choose Discard")
    printlnIfVerbose("Prediction: ${bestTile.joinToString("  ") { formatTileAndThought(it, pred) }}")

    val takenDecision = tileTypeToWeightsWithoutFlowers(bestTile[0])
    currentGameRecording.add(RecordedDecision(PlayerControlAction.CHOOSE_DISCARD, deepPovCopy(pov), pred, takenDecision))
    return bestTile[0]
  }

  override fun kongFromDraw(pov: PlayerPov): Boolean {
    val pred = brain.thinkAbout(PlayerControlAction.KONG_FROM_DRAW, pov)

    printPovIfVerbose(pov)
    printlnIfVerbose("Action    : Kong from Draw")
    printlnIfVerbose("Prediction: ${pred[0]}")

    val takenDecision = FloatArray(1) { if (currentGameWon) 1.0f else 0.0f }
    currentGameRecording.add(RecordedDecision(PlayerControlAction.KONG_FROM_DRAW, deepPovCopy(pov), pred, takenDecision))

    return pred[0] >= 0.5
  }

  override fun kongFromDiscard(pov: PlayerPov): Boolean {
    val pred = brain.thinkAbout(PlayerControlAction.KONG_FROM_DISCARD, pov)

    printPovIfVerbose(pov)
    printlnIfVerbose("Action    : Kong from Discard")
    printlnIfVerbose("Prediction: ${pred[0]}")

    val takenDecision = FloatArray(1) { if (currentGameWon) 1.0f else 0.0f }
    currentGameRecording.add(RecordedDecision(PlayerControlAction.KONG_FROM_DISCARD, deepPovCopy(pov), pred, takenDecision))
    return pred[0] >= 0.5
  }

  override fun pongFromDiscard(pov: PlayerPov): Boolean {
    val pred = brain.thinkAbout(PlayerControlAction.PONG_FROM_DISCARD, pov)

    printPovIfVerbose(pov)
    printlnIfVerbose("Action    : Pong from Discard")
    printlnIfVerbose("Prediction: ${pred[0]}")

    val takenDecision = FloatArray(1) { if (currentGameWon) 1.0f else 0.0f }
    currentGameRecording.add(RecordedDecision(PlayerControlAction.PONG_FROM_DISCARD, deepPovCopy(pov), pred, takenDecision))
    return pred[0] >= 0.5
  }

  override fun chowFromDiscard(pov: PlayerPov): TileType? {
    val pred = brain.thinkAbout(PlayerControlAction.CHOW_FROM_DISCARD, pov)
    val discardedTile = pov.discarded.lastOrNull() ?: return null

    val possibleChows = determineChowPossibilities(pov.hand, discardedTile)
    if (possibleChows.isEmpty()) {
      throw IllegalStateException("No possible chows found, but game suggested to chow.")
    }

    printlnIfVerbose("NeuralNetBot not chowing at all score: ${pred[0]}")


    val chowScores = mutableListOf<Pair<Float, TileType?>>()
    chowScores.add(Pair(pred[0], null))

    possibleChows.forEach { chow ->
      when (chow[0].numeral) {
        discardedTile.numeral?.minus(2) -> chowScores.add(
          Pair(
            pred[1],
            chow[0]
          )
        ) // score for chow starting with the tile two below the discarded tile
        discardedTile.numeral?.minus(1) -> chowScores.add(
          Pair(
            pred[2],
            chow[0]
          )
        ) // score for chow starting with the tile one below the discarded tile
        discardedTile.numeral -> chowScores.add(
          Pair(
            pred[3],
            chow[0]
          )
        )// score for chow starting with the discarded tile itself
      }
    }

    chowScores.sortBy { it.first }

    printPovIfVerbose(pov)
    printlnIfVerbose("Action    : Chow")
    printlnIfVerbose("Prediction: ${chowScores.joinToString("  ") { "${formatTile(it.second)} ${"%.2f".format(it.first)}" }}")
    val result = chowScores[0].second

    val takenDecision =  floatArrayOf(
      if (result == null) 1.0f else 0.0f,
      if (result != null && result.numeral == pov.discarded.lastOrNull()?.numeral?.minus(2)) 1.0f else 0.0f,
      if (result != null && result.numeral == pov.discarded.lastOrNull()?.numeral?.minus(1)) 1.0f else 0.0f,
      if (result != null && result.numeral == pov.discarded.lastOrNull()?.numeral) 1.0f else 0.0f,
    )
    currentGameRecording.add(RecordedDecision(PlayerControlAction.CHOW_FROM_DISCARD, deepPovCopy(pov), pred, takenDecision))
    return result
  }


  private fun formatTileAndThought(tile: TileType, thought: FloatArray): String {
    return "${formatTile(tile)} ${"%.2f".format(thought[tile.ordinal])}"
  }

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

  fun deepPovCopy(pov: PlayerPov): PlayerPov {
    return pov.copy(
      prevMelds = pov.prevMelds.map { it.toList() }.toList(),
      nextMelds = pov.nextMelds.map { it.toList() }.toList(),
      oppoMelds = pov.oppoMelds.map { it.toList() }.toList(),
      prevFlowers = pov.prevFlowers.toList(),
      nextFlowers = pov.nextFlowers.toList(),
      oppoFlowers = pov.oppoFlowers.toList(),

      discarded = pov.discarded.toList(),
      hand = pov.hand.toList(),
      melds = pov.melds.map { it.toList() }.toList(),
      flowers = pov.flowers.toList(),
    )
  }
}