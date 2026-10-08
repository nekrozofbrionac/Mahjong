package de.nekrozofbrionac.mahjong.player.control.bot.ml

import de.nekrozofbrionac.mahjong.board.TileType
import de.nekrozofbrionac.mahjong.game.PlayerPov
import de.nekrozofbrionac.mahjong.player.control.PlayerControl
import de.nekrozofbrionac.mahjong.player.control.PlayerControlAction
import de.nekrozofbrionac.mahjong.printPov

class NeuralNetBot(
  val verbose: Boolean = false,
  val brain: Brain
): PlayerControl {


  override fun win(pov: PlayerPov): Boolean {
    val thought = brain.thinkAbout(PlayerControlAction.WIN, pov)
    if (verbose) {
      printPov(pov)
      println("NeuralNetBot action: win")
      println("NeuralNetBot (${pov.playerSeat}) says: ${thought}")
    }
    return thought[0] > 0.5f
  }

  override fun chooseDiscard(pov: PlayerPov): TileType {
    val thought = brain.thinkAbout(PlayerControlAction.CHOOSE_DISCARD, pov)
    if (verbose) {
      printPov(pov)
      println("NeuralNetBot action: chooseDiscard")
      println("NeuralNetBot (${pov.playerSeat}) says: ${thought}")
    }

    val hand = pov.hand
    val bestDiscardIndex = hand.indices.maxByOrNull { index ->
      if (index < thought.size) thought[index] else Float.NEGATIVE_INFINITY
    } ?: 0

    return hand[bestDiscardIndex]
  }

  override fun kongFromDraw(pov: PlayerPov): Boolean {
    val thought = brain.thinkAbout(PlayerControlAction.KONG_FROM_DRAW, pov)
    if (verbose) {
      printPov(pov)
      println("NeuralNetBot action: kongFromDraw")
      println("NeuralNetBot (${pov.playerSeat}) says: ${thought}")
    }
    return thought[0] > 0.5f
  }

  override fun kongFromDiscard(pov: PlayerPov): Boolean {
    val thought = brain.thinkAbout(PlayerControlAction.KONG_FROM_DISCARD, pov)
    if (verbose) {
      printPov(pov)
      println("NeuralNetBot action: kongFromDiscard")
      println("NeuralNetBot (${pov.playerSeat}) says: ${thought}")
    }
    return thought[0] > 0.5f
  }

  override fun pongFromDiscard(pov: PlayerPov): Boolean {
    val thought = brain.thinkAbout(PlayerControlAction.PONG_FROM_DISCARD, pov)
    if (verbose) {
      printPov(pov)
      println("NeuralNetBot action: pongFromDiscard")
      println("NeuralNetBot (${pov.playerSeat}) says: ${thought}")
    }
    return thought[0] > 0.5f
  }

  override fun chowFromDiscard(pov: PlayerPov): TileType? {
    val thought = brain.thinkAbout(PlayerControlAction.CHOW_FROM_DISCARD, pov)
    if (verbose) {
      printPov(pov)
      println("NeuralNetBot action: chowFromDiscard")
      println("NeuralNetBot (${pov.playerSeat}) says: ${thought}")
    }

    if (thought[0] < 0.5f) {
      return null
    }

    val possibleChows = determineChowPossibilities(pov.hand, pov.discarded.lastOrNull() ?: return null)
    if (possibleChows.isEmpty()) {
      return null
    }

    val chowScores = listOf(
      thought[1], // score for chow starting with the tile two below the discarded tile
      thought[2], // score for chow starting with the tile one below the discarded tile
      thought[3]  // score for chow starting with the discarded tile itself
    )

    val bestChowIndex = possibleChows.indices.maxByOrNull { index ->
      if (index < chowScores.size) chowScores[index] else Float.NEGATIVE_INFINITY
    } ?: 0

    return possibleChows[bestChowIndex].firstOrNull()
  }
}