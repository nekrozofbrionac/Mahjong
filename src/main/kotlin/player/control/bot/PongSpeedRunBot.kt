package de.nekrozofbrionac.player.control.bot

import Player
import de.nekrozofbrionac.mahjong.board.TileType
import de.nekrozofbrionac.player.control.PlayerControl

object PongSpeedRunBot: PlayerControl{
  override fun act() = Unit
  override fun chooseDiscard(p: Player): TileType {
    return p.hand.find { handTile -> p.hand.count { it == handTile } == 1 }
      ?: p.hand.random();
  }

  override fun kongFromDiscard(player: Player, tile: TileType): Boolean {
    println("PONG SPEED RUN BOT SAYS: LETS KONG! $tile")
    return true;
  }

  override fun pongFromDiscard(player: Player, tile: TileType): Boolean {
    println("PONG SPEED RUN BOT SAYS: LETS PONG! $tile")
    return true;
  }

  override fun chowFromDiscard(player: Player, tile: TileType): Boolean {
    println("PONG SPEED RUN BOT SAYS: LETS CHOW! $tile")
    return true;
  }
}