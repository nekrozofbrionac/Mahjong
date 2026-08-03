package de.nekrozofbrionac.mahjong.player.control.bot.ml

import de.nekrozofbrionac.mahjong.board.TileType
import de.nekrozofbrionac.mahjong.game.PlayerPov
import de.nekrozofbrionac.mahjong.player.control.PlayerControl
import de.nekrozofbrionac.mahjong.printPov

class NeuralNetBot(
  val verbose: Boolean = false,
  val brain: Brain
): PlayerControl {


  override fun win(pov: PlayerPov): Boolean {
    val thought = brain.think(pov)
    if (verbose) {
      printPov(pov)
      println("NeuralNetBot action: win")
      println("NeuralNetBot (${pov.playerSeat}) says: ${thought}")
    }
    return thought.winning > 0.5f
  }

  override fun chooseDiscard(pov: PlayerPov): TileType {
    val thought = brain.think(pov)
    if (verbose) {
      printPov(pov)
      println("NeuralNetBot action: chooseDiscard")
      println("NeuralNetBot (${pov.playerSeat}) says: ${thought}")
    }
    val discardScores = listOf(
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
      thought.discard14
    )

    val hand = pov.hand
    val bestDiscardIndex = hand.indices.maxByOrNull { index ->
      if (index < discardScores.size) discardScores[index] else Float.NEGATIVE_INFINITY
    } ?: 0

    return hand[bestDiscardIndex]
  }

  override fun kongFromDraw(pov: PlayerPov): Boolean {
    val thought = brain.think(pov)
    if (verbose) {
      printPov(pov)
      println("NeuralNetBot action: kongFromDraw")
      println("NeuralNetBot (${pov.playerSeat}) says: ${thought}")
    }
    return thought.kongFromDraw > 0.5f
  }

  override fun kongFromDiscard(pov: PlayerPov): Boolean {
    val thought = brain.think(pov)
    if (verbose) {
      printPov(pov)
      println("NeuralNetBot action: kongFromDiscard")
      println("NeuralNetBot (${pov.playerSeat}) says: ${thought}")
    }
    return thought.kongFromDiscard> 0.5f
  }

  override fun pongFromDiscard(pov: PlayerPov): Boolean {
    val thought = brain.think(pov)
    if (verbose) {
      printPov(pov)
      println("NeuralNetBot action: pongFromDiscard")
      println("NeuralNetBot (${pov.playerSeat}) says: ${thought}")
    }
    return thought.pongFromDiscard> 0.5f
  }

  override fun chowFromDiscard(pov: PlayerPov): TileType? {
    val thought = brain.think(pov)
    if (verbose) {
      printPov(pov)
      println("NeuralNetBot action: chowFromDiscard")
      println("NeuralNetBot (${pov.playerSeat}) says: ${thought}")
    }

    if (thought.chowAtAll < 0.5f) {
      return null
    }

    val chowScores = listOf(
      thought.chowFromDiscardLowest,
      thought.chowFromDiscardMiddle,
      thought.chowFromDiscardHighest,
    )

    val possibleChows = determineChowPossibilities(pov.hand, pov.discarded.lastOrNull() ?: return null)

    if (possibleChows.isEmpty()) {
      return null
    }

    val bestChowIndex = possibleChows.indices.maxByOrNull { index ->
      if (index < chowScores.size) chowScores[index] else Float.NEGATIVE_INFINITY
    } ?: 0

    return possibleChows[bestChowIndex].firstOrNull()
  }
}