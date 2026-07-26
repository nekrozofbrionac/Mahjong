package de.nekrozofbrionac.mahjong.player.control.bot

import de.nekrozofbrionac.mahjong.board.TileType
import de.nekrozofbrionac.mahjong.combinations
import de.nekrozofbrionac.mahjong.determineHiddenTiles
import de.nekrozofbrionac.mahjong.formatTiles
import de.nekrozofbrionac.mahjong.game.PlayerPov
import de.nekrozofbrionac.mahjong.game.Turn
import de.nekrozofbrionac.mahjong.player.control.PlayerControl
import de.nekrozofbrionac.mahjong.printPov

class MeldBuilder(
  val minFaan: Int = 3,
) : PlayerControl {
  override fun win(pov: PlayerPov): Boolean {
    TODO("Not yet implemented")
  }

  override fun chooseDiscard(pov: PlayerPov): TileType {
    printPov(pov)
    println("chooseDiscard")

    val remainingTiles = determineHiddenTiles(pov)


    val possibleHands = proposeHands(pov.hand, pov.melds, remainingTiles, minFaan, pov.prevailingWind, pov.playerSeat)
    val assessedHands = assessHands(possibleHands, pov.hand, pov.melds)

    println("Assessed Hands:")
    assessedHands.subList(0, 50).forEach {
      println("Score: " + it.second.toString().padStart(2) + "Hand: " + formatTiles(it.first, 14, "") + "")
    }

    val line = readlnOrNull()
    println(line)

    return pov.hand.first()
  }

  override fun kongFromDraw(pov: PlayerPov): Boolean {
    TODO("Not yet implemented")
  }

  override fun kongFromDiscard(pov: PlayerPov): Boolean {
    TODO("Not yet implemented")
  }

  override fun pongFromDiscard(pov: PlayerPov): Boolean {
    TODO("Not yet implemented")
  }

  override fun chowFromDiscard(pov: PlayerPov): TileType? {
    TODO("Not yet implemented")
  }
}

fun getPossibleStandardMelds(hand: List<TileType>): Set<List<TileType>> {
  val output = mutableSetOf<List<TileType>>()
  // maybe special hands like 13 orphans, 7 pairs, 9 gates, etc. can be added here later
  hand.forEach { tile ->
    // chows
    if (null != tile.numeral) {
      val tile2Before = TileType.entries.find { it.suit == tile.suit && it.numeral == tile.numeral - 2 }
      val tile1Before = TileType.entries.find { it.suit == tile.suit && it.numeral == tile.numeral - 1 }
      val tile1After = TileType.entries.find { it.suit == tile.suit && it.numeral == tile.numeral + 1 }
      val tile2After = TileType.entries.find { it.suit == tile.suit && it.numeral == tile.numeral + 2 }

      if (tile2Before != null && tile1Before != null) {
        output.add(listOf(tile2Before, tile1Before, tile))
      }
      if (tile1Before != null && tile1After != null) {
        output.add(listOf(tile1Before, tile, tile1After))
      }
      if (tile1After != null && tile2After != null) {
        output.add(listOf(tile, tile1After, tile2After))
      }
    }

    // pongs
    output.add(listOf(tile, tile, tile))

    // kongs?
    output.add(listOf(tile, tile, tile, tile))
  }

  return output
}

// disqualify melds that are not possible with the given tiles
fun disqualifyMelds(melds: Set<List<TileType>>, tiles: List<TileType>): Set<List<TileType>> {

  return melds.filter { meld ->
    val tilesCopy = tiles.toMutableList()
    return@filter meld.all { tilesCopy.remove(it) }
  }.toSet()
}

fun proposeHands(
  hand: List<TileType>,
  fixedMelds: List<List<TileType>>,
  remainingTiles: List<TileType>,
  minFaan: Int,
  prevWind: Turn,
  playerWind: Turn
): Set<List<TileType>> {
  val output = mutableSetOf<List<TileType>>()

  val tilesToWorkWith = (hand + remainingTiles).sorted()
  println("yoo tilesToWorkWith " + formatTiles(tilesToWorkWith, 36, ""))

  val possibleMelds = disqualifyMelds(getPossibleStandardMelds(hand), tilesToWorkWith)
  val huh = hand.distinct().map { listOf(it, it) }.toSet()


  val possibleEyes = disqualifyMelds(huh, tilesToWorkWith)
  //println("yoo huh " + huh.joinToString(" ") { formatTiles(it, 2, "") })
  println("yoo possibleEyes " + possibleEyes.joinToString(" ") { formatTiles(it, 2, "") })

  possibleEyes.forEach { eye ->
    val pickedMelds = possibleMelds.toList().combinations(4 - fixedMelds.size)
    println("yoo pickedMelds" +  pickedMelds.size)
    pickedMelds.forEach { pMelds ->
      val melds = pMelds + fixedMelds
      val proposedHand = eye + melds.flatten()
      val availableTiles = (hand + remainingTiles).toMutableList()

      if (proposedHand.all { tile -> availableTiles.remove(tile) }) {
        val eval = fastEvaluateStdHand(listOf(eye) + melds, prevWind, playerWind)
        if (eval >= minFaan) {
          output.add(proposedHand)
        }
      }
    }
  }
  println("yoo " +  output.size)
  return output
}

fun assessHands(
  possibleHands: Set<List<TileType>>,
  hand: List<TileType>,
  fixedMelds: List<List<TileType>>
): List<Pair<List<TileType>, Int>> {
  return possibleHands.map { ph ->
    val currentHand = (hand + fixedMelds.flatten()).toMutableList()
    ph.forEach { currentHand.remove(it) }
    return@map Pair(ph, currentHand.size)
  }.sortedBy { it.second }
}
