package de.nekrozofbrionac.mahjong.player

import de.nekrozofbrionac.mahjong.player.control.PlayerControl
import de.nekrozofbrionac.mahjong.player.control.RecordingPlayerControl

class BotPlayer(
  override val name: String,
  override val playerControl: RecordingPlayerControl,
) : AbstractPlayer() {
}