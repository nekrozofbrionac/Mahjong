package de.nekrozofbrionac.mahjong.player.control

import de.nekrozofbrionac.mahjong.game.PlayerPov
import de.nekrozofbrionac.mahjong.board.TileType

interface PlayerControl {
  fun win(pov: PlayerPov): Boolean
  fun chooseDiscard(pov: PlayerPov): TileType
  fun kongFromDraw(pov: PlayerPov): Boolean
  fun kongFromDiscard(pov: PlayerPov): Boolean
  fun pongFromDiscard(pov: PlayerPov): Boolean
  /** @return Starting tile for a chow, if null, it means no */
  fun chowFromDiscard(pov: PlayerPov): TileType?

  fun determineChowPossibilities(hand: List<TileType>, extraTile: TileType): List<List<TileType>> {
    if (extraTile.numeral == null || extraTile.suit == null) {
      return listOf()
    }

    val chowPossibilities: MutableList<List<TileType>> = mutableListOf();

    val sameSuited = hand.filter { it.suit == extraTile.suit };
    val twoBelow = sameSuited.find { it.numeral == extraTile.numeral - 2 }
    val oneBelow = sameSuited.find { it.numeral == extraTile.numeral - 1 }
    val oneAbove = sameSuited.find { it.numeral == extraTile.numeral + 1 }
    val twoAbove = sameSuited.find { it.numeral == extraTile.numeral + 2 }

    if (null != twoBelow && null != oneBelow) {
      chowPossibilities.add(listOf(twoBelow, oneBelow, extraTile))
    }
    if (null != oneBelow && null != oneAbove) {
      chowPossibilities.add(listOf(oneBelow, extraTile, oneAbove))
    }
    if (null != oneAbove && null != twoAbove) {
      chowPossibilities.add(listOf(extraTile, oneAbove, twoAbove))
    }

    return chowPossibilities;
  }
}