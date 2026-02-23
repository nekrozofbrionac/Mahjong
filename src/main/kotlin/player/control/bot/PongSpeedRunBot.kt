package de.nekrozofbrionac.player.control.bot

import de.nekrozofbrionac.game.PlayerPov
import de.nekrozofbrionac.mahjong.board.TileType
import de.nekrozofbrionac.player.control.PlayerControl
import de.nekrozofbrionac.printPov

class PongSpeedRunBot(
  val verbose: Boolean = false
) : PlayerControl {

  override fun win(pov: PlayerPov): Boolean {
    return true;
  }

  override fun chooseDiscard(pov: PlayerPov): TileType {
    val unusableTiles = mutableListOf<TileType>()
    unusableTiles.addAll(pov.discarded)
    unusableTiles.addAll(pov.melds.flatten())
    unusableTiles.addAll(pov.prevMelds.flatten())
    unusableTiles.addAll(pov.nextMelds.flatten())
    unusableTiles.addAll(pov.oppoMelds.flatten())

    val unpongableCandidates = pov.hand
      .filter { ht -> unusableTiles.count { ht == it } > 1 }

    if (unpongableCandidates.isNotEmpty()) {
      return unpongableCandidates.random();
    }


    val unlikelyPongCandidate = pov.hand
      .filter { ht -> unusableTiles.count { ht == it } == 1 }
      .find { ht -> pov.hand.count { ht == it } < 2 }
    if (null != unlikelyPongCandidate) {
      return unlikelyPongCandidate;
    }


    return pov.hand.find { ht -> pov.hand.count { ht == it } < 2 }
      ?: pov.hand.random();
  }

  override fun kongFromDiscard(pov: PlayerPov): Boolean {
    if (pov.discarded.isEmpty()) {
      return false
    }

    if (verbose) {
      printPov(pov)
      println("Pong Speedrun Bot (${pov.playerSeat}) says: LETS KONG! ${pov.discarded.last()}")
    }
    return true;
  }

  override fun pongFromDiscard(pov: PlayerPov): Boolean {
    if (pov.discarded.isEmpty()) {
      return false
    }

    if (verbose) {
      printPov(pov)
      println("Pong Speedrun Bot (${pov.playerSeat}) says: LETS PONG! ${pov.discarded.last()}")
    }
    return true;
  }

  override fun chowFromDiscard(pov: PlayerPov): TileType? {
    if (verbose) {
      printPov(pov)
      println("Pong Speedrun Bot (${pov.playerSeat}) says: I WILL NEVER CHOW!!!")
    }
    return null;
  }
}