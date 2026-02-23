package de.nekrozofbrionac.player.control.bot

import de.nekrozofbrionac.game.PlayerPov
import de.nekrozofbrionac.mahjong.board.TileType
import de.nekrozofbrionac.player.control.PlayerControl

object PongSpeedRunBot: PlayerControl{
  override fun win(pov: PlayerPov): Boolean {
    return true;
  }

  override fun chooseDiscard(pov: PlayerPov): TileType {
    return pov.hand.find { handTile -> pov.hand.count { it == handTile } == 1 }
      ?: pov.hand.random();
  }

  override fun kongFromDiscard(pov: PlayerPov): Boolean {
    if (pov.discarded.isEmpty()) {
      return false
    }

    println("PONG SPEED RUN BOT SAYS: LETS KONG! ${pov.discarded.last()}")
    return true;
  }

  override fun pongFromDiscard(pov: PlayerPov): Boolean {
    if (pov.discarded.isEmpty()) {
      return false
    }

    println("PONG SPEED RUN BOT SAYS: LETS PONG! ${pov.discarded.last()}")
    return true;
  }

  override fun chowFromDiscard(pov: PlayerPov): TileType?{
    println("PONG SPEED RUN BOT SAYS: I WILL NEVER CHOW!!!")
    return null;
  }
}