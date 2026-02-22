package de.nekrozofbrionac.player.control

import de.nekrozofbrionac.game.PlayerPov
import de.nekrozofbrionac.mahjong.board.TileType

interface PlayerControl {
  fun chooseDiscard(pov: PlayerPov): TileType
  fun kongFromDiscard(pov: PlayerPov): Boolean
  fun pongFromDiscard(pov: PlayerPov): Boolean
  fun chowFromDiscard(pov: PlayerPov): Boolean
}