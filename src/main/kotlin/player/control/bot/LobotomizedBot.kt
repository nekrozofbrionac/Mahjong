package de.nekrozofbrionac.player.control.bot

import Player
import de.nekrozofbrionac.mahjong.board.TileType
import de.nekrozofbrionac.player.control.PlayerControl

object LobotomizedBot: PlayerControl {
  override fun act() = Unit
  override fun chooseDiscard(p: Player): TileType {
    return p.hand.random();
  }

  override fun kongFromDiscard(player: Player, tile: TileType): Boolean {
    println("LOBOTOMIZED BOT SAYS: LETS KONG! $tile")
    return true;
  }

  override fun pongFromDiscard(player: Player, tile: TileType): Boolean {
    println("LOBOTOMIZED BOT SAYS: LETS PONG! $tile")
    return true;
  }

  override fun chowFromDiscard(player: Player, tile: TileType): Boolean {
    println("LOBOTOMIZED BOT SAYS: LETS CHOW! $tile")
    return true;
  }
}