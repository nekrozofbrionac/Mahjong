package de.nekrozofbrionac.board

import de.nekrozofbrionac.mahjong.board.Board
import de.nekrozofbrionac.mahjong.board.TileType

class SimpleBoard : Board {
  override val wall: MutableList<TileType> = mutableListOf();
  override val discarded: MutableList<TileType> = mutableListOf();

  override fun drawFromWall(): TileType? {
    if (wall.isEmpty()) {
      return null;
    }

    val tile = wall.random();
    wall.remove(tile);
    return tile;
  }

  override fun addToDiscard(tile: TileType?) {
    if (null == tile) {
      return;
    }
    discarded.add(tile)
  }

  override fun popLastDiscard(): TileType {
    return discarded.removeLast();
  }

  init {
    TileType.entries.forEach { tile ->
      repeat(tile.count) { this.wall.add(tile); }
      this.wall.shuffle();
    }
  }
}