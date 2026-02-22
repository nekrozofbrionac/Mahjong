package de.nekrozofbrionac.player

import Player
import de.nekrozofbrionac.mahjong.board.TileType
import de.nekrozofbrionac.player.control.PlayerControl

class TerminalPlayer : Player {
  override val playerControl: PlayerControl = object : PlayerControl {
    override fun act() = Unit
    override fun chooseDiscard(p: Player): TileType {
      println("--- PLAYER INTERACTION REQUIRED ---")
      println("Name a tile to discard, e.g. \"b1\" ")
      var prospect: TileType? = null;
      while (null == prospect) {
        val line = readlnOrNull() ?: continue;
        prospect = p.hand.find { line.uppercase().trim() == it.shortHand} ?: continue;
      }
      println("Thank you, discarding ${prospect.name.lowercase()}!")
      println("--- PLAYER INTERACTION END ---")
      return prospect;
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