package de.nekrozofbrionac.player.control

import Player
import de.nekrozofbrionac.mahjong.board.TileType

interface PlayerControl {
  fun act()
  fun chooseDiscard(player: Player): TileType
}