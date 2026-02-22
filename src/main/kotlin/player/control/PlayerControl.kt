package de.nekrozofbrionac.player.control

import Player
import de.nekrozofbrionac.mahjong.board.TileType

interface PlayerControl {
  fun act()
  fun chooseDiscard(player: Player): TileType
  fun kongFromDiscard(player: Player, tile: TileType): Boolean
  fun pongFromDiscard(player: Player, tile: TileType): Boolean
  fun chowFromDiscard(player: Player, tile: TileType): Boolean
}