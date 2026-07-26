package de.nekrozofbrionac.mahjong.player

import de.nekrozofbrionac.mahjong.player.control.PlayerControl

class BotPlayer(
  override val name: String,
  override val playerControl: PlayerControl,
) : AbstractPlayer() {
}