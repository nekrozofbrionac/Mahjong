package de.nekrozofbrionac.mahjong.player.control.bot.ml

import de.nekrozofbrionac.mahjong.board.TileType

/*
 * different approach: HOE all the way.
 */
val tileEncodingSize = TileType.entries.size
fun tileTypeToWeights (tile: TileType): FloatArray {
  return FloatArray(TileType.entries.size) { if (it == tile.ordinal) 1.0f else 0.0f }
}
