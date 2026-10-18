package de.nekrozofbrionac.mahjong.player.control.bot

import de.nekrozofbrionac.mahjong.game.PlayerPov
import de.nekrozofbrionac.mahjong.board.TileType
import de.nekrozofbrionac.mahjong.determineChowPossibilities
import de.nekrozofbrionac.mahjong.player.control.PlayerControl
import de.nekrozofbrionac.mahjong.printPov

class LobotomizedBot(
  val verbose: Boolean = false
) : PlayerControl {

  override fun win(pov: PlayerPov): Boolean {
    return true;
  }

  override fun chooseDiscard(pov: PlayerPov): TileType {
    return pov.hand.random();
  }

  override fun kongFromDraw(pov: PlayerPov): Boolean {
    if (verbose) {
      printPov(pov)
      println("Lobotomized Bot (${pov.playerSeat}) says: LETS KONG FROM DRAW! ${pov.hand.last()}")
    }
    return true;
  }

  override fun kongFromDiscard(pov: PlayerPov): Boolean {
    if (pov.discarded.isEmpty()) {
      return false
    }

    if (verbose) {
      printPov(pov)
      println("Lobotomized Bot (${pov.playerSeat}) says: LETS KONG FROM DISCARD! ${pov.discarded.last()}")
    }
    return true;
  }

  override fun pongFromDiscard(pov: PlayerPov): Boolean {
    if (pov.discarded.isEmpty()) {
      return false
    }

    if (verbose) {
      printPov(pov)
      println("Lobotomized Bot (${pov.playerSeat}) says: LETS PONG! ${pov.discarded.last()}")
    }
    return true;
  }

  override fun chowFromDiscard(pov: PlayerPov): TileType? {
    if (pov.discarded.isEmpty()) {
      return null
    }

    val possiblities = determineChowPossibilities(pov.hand, pov.discarded.last());
    if (possiblities.isEmpty()) {
      println("************************ POV in chowFromDiscard")
      printPov(pov)
      throw UnsupportedOperationException("HOW AM I ALLOWED TO CHOW WITH NOTHING?!")
    }

    val choice = possiblities.random()

    if (verbose) {
      printPov(pov)
      println("Lobotomized Bot (${pov.playerSeat}) says: LETS CHOW! ${choice.first()}")
    }
    return choice.first();
  }
}