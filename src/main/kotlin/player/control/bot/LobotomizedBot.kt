package de.nekrozofbrionac.player.control.bot

import de.nekrozofbrionac.game.PlayerPov
import de.nekrozofbrionac.mahjong.board.TileType
import de.nekrozofbrionac.player.control.PlayerControl

object LobotomizedBot: PlayerControl {
  override fun chooseDiscard(p: PlayerPov): TileType {
    return p.hand.random();
  }

  override fun kongFromDiscard(pov: PlayerPov): Boolean {
    if (pov.discarded.isEmpty()) {
      return false
    }

    println("LOBOTOMIZED BOT SAYS: LETS KONG! ${pov.discarded.last()}")
    return true;
  }

  override fun pongFromDiscard(pov: PlayerPov): Boolean {
    if (pov.discarded.isEmpty()) {
      return false
    }

    println("LOBOTOMIZED BOT SAYS: LETS PONG! ${pov.discarded.last()}")
    return true;
  }

  override fun chowFromDiscard(pov: PlayerPov): Boolean {
    if (pov.discarded.isEmpty()) {
      return false
    }

    println("LOBOTOMIZED BOT SAYS: LETS CHOW! ${pov.discarded.last()}")
    return true;
  }
}