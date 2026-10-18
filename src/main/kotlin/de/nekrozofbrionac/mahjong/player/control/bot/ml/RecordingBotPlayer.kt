package de.nekrozofbrionac.mahjong.player.control.bot.ml

import de.nekrozofbrionac.mahjong.player.AbstractPlayer

class RecordingBotPlayer (
  override val name: String,
  override val playerControl: RecordingPlayerControl,
) : AbstractPlayer() {
}
