package de.nekrozofbrionac.player

import Player
import de.nekrozofbrionac.mahjong.board.TileType
import de.nekrozofbrionac.player.control.PlayerControl

class VoidPlayer : Player {
  override val playerControl: PlayerControl = object : PlayerControl {
    override fun act() = Unit
    override fun chooseDiscard(p: Player): TileType {
      return p.hand.first();
    }
  }
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
}