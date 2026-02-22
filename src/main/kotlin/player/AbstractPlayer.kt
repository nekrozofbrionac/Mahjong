package de.nekrozofbrionac.player

import Player
import de.nekrozofbrionac.mahjong.board.TileType

abstract class AbstractPlayer : Player {
  override val hand: MutableList<TileType> = mutableListOf()
  override val melds: MutableList<MutableList<TileType>> = mutableListOf();
  override val flowers: MutableList<TileType> = mutableListOf();

  override fun sortHand() {
    hand.sort();
  }

  override fun addToHand(tile: TileType) {
    if (tile.isFlower()) {
      flowers.add(tile)
      return
    }

    hand.add(tile);
  }

  override fun discard(tile: TileType): TileType? {
    val done = hand.remove(tile);
    return if (done) tile else null;
  }

  override fun formKongMeld(tile: TileType) {
    if (hand.count { it == tile } < 4) {
      return;
    }

    hand.removeAll { it == tile };
    melds.add(mutableListOf(tile, tile, tile, tile))
  }

  override fun formPongMeld(tile: TileType) {
    if (hand.count { it == tile } < 3) {
      return;
    }

    repeat(3) { hand.remove(tile) }
    melds.add(mutableListOf(tile, tile, tile))
  }
}