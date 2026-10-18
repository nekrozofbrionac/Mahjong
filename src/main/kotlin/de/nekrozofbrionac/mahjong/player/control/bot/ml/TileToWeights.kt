package de.nekrozofbrionac.mahjong.player.control.bot.ml

import de.nekrozofbrionac.mahjong.board.NUMBER_OF_TILES_WITHOUT_FLOWERS
import de.nekrozofbrionac.mahjong.board.TileType

/*
 * different approach: HOE all the way.
 */
val tileEncodingSize = TileType.entries.size
fun tileTypeToWeights (tile: TileType): FloatArray {
  return FloatArray(TileType.entries.size) { if (it == tile.ordinal) 1.0f else 0.0f }
}

fun tileTypeToWeightsWithoutFlowers (tile: TileType): FloatArray {
  if (tile.ordinal >= NUMBER_OF_TILES_WITHOUT_FLOWERS) {
    throw IllegalArgumentException("tile must not be a flower, but was $tile")
  }
  return FloatArray(NUMBER_OF_TILES_WITHOUT_FLOWERS) { if (it == tile.ordinal) 1.0f else 0.0f }
}

fun weightsToTileType(weights: FloatArray): TileType? {
  if (weights.size != tileEncodingSize && weights.size != NUMBER_OF_TILES_WITHOUT_FLOWERS) {
    throw IllegalArgumentException("weights size must be $tileEncodingSize or ${NUMBER_OF_TILES_WITHOUT_FLOWERS}, but was ${weights.size}")
  }
  val maxIndex = weights.indices.maxByOrNull { weights[it] }
  return maxIndex?.let { TileType.entries[it] }
}
