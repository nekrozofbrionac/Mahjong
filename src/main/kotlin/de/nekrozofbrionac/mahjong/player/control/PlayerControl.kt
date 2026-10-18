package de.nekrozofbrionac.mahjong.player.control

import de.nekrozofbrionac.mahjong.game.PlayerPov
import de.nekrozofbrionac.mahjong.board.TileType
import de.nekrozofbrionac.mahjong.game.GameReport

interface PlayerControl {
  fun win(pov: PlayerPov): Boolean
  fun chooseDiscard(pov: PlayerPov): TileType
  fun kongFromDraw(pov: PlayerPov): Boolean
  fun kongFromDiscard(pov: PlayerPov): Boolean
  fun pongFromDiscard(pov: PlayerPov): Boolean

  /** @return Starting tile for a chow, if null, it means no */
  fun chowFromDiscard(pov: PlayerPov): TileType?

  fun endGame() = Unit
}