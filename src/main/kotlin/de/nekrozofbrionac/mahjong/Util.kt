package de.nekrozofbrionac.mahjong

import Player
import de.nekrozofbrionac.mahjong.game.PlayerPov
import de.nekrozofbrionac.mahjong.board.Board
import de.nekrozofbrionac.mahjong.board.TileType

fun draw(board: Board, player: Player): Boolean {
  val drawnTile = board.drawFromWall()
    ?: return false;
  player.addToHand(drawnTile);

  if (drawnTile.isFlower()) {
    return draw(board, player);
  }
  return true
}

fun printPov(pov: PlayerPov) {
  println()
  println("------------ PLAYER POV ${pov.playerSeat.name} ------------")
  println()
  println("Current Turn: ${pov.turn}")
  println("Current Turn Player: ${pov.currentTurnPlayer}")
  println("You : ${pov.playerSeat.name}")
  println("Previous player: ${pov.prevSeat.name}")
  println("    Melds:   ${pov.prevMelds.joinToString("  ") { formatTiles(it, 16, "") }}")
  println("    Flowers: ${formatTiles(pov.prevFlowers, 8, "")}")
  println("Next player: ${pov.nextSeat.name}")
  println("    Melds:   ${pov.nextMelds.joinToString("  ") { formatTiles(it, 16, "") }}")
  println("    Flowers: ${formatTiles(pov.nextFlowers, 8, "")}")
  println("Opposite player: ${pov.oppoSeat.name}")
  println("    Melds:   ${pov.oppoMelds.joinToString("  ") { formatTiles(it, 16, "") }}")
  println("    Flowers: ${formatTiles(pov.oppoFlowers, 8, "")}")
  println("Board:")
  println("    Wall remaining: ${pov.wallRemaining}")
  println("    Discard pile:")
  println(formatTiles(pov.discarded, 18))
  println("You (${pov.playerSeat.name}):")
  println("    Hand:    ${formatTiles(pov.hand, 14, "")}")
  println("    Melds:   ${pov.melds.joinToString("  ") { formatTiles(it, 16, "") }}")
  println("    Flowers: ${formatTiles(pov.flowers, 8, "")}")
  println()
  println("---------- PLAYER POV ${pov.playerSeat.name} END ----------")
  println()
}

fun formatTiles(tiles: List<TileType>, perLine: Int, padLeft: String = "        "): String {
  val maxLength = TileType.entries.maxOf { it.shortHand.length }
  return tiles
    .map {
      val color = when (it.suit) {
        "B" -> "\u001b[1;97;42m"
        "S" -> "\u001b[1;97;44m"
        "C" -> "\u001b[1;97;41m"
        else -> {
          when (it) {
            TileType.WIND_EAST,
            TileType.WIND_SOUTH,
            TileType.WIND_WEST,
            TileType.WIND_NORTH -> "\u001b[1;36;107m"

            TileType.DRAGON_RED -> "\u001b[1;31;107m"
            TileType.DRAGON_GREEN -> "\u001b[1;32;107m"
            TileType.DRAGON_WHITE -> "\u001b[1;30;107m"
            else -> ""
          }
        }
      }

      val tileEngraving = if (it.suit != null)
        it.shortHand.lowercase().padEnd(maxLength)
      else {
        it.shortHand.uppercase().padEnd(maxLength)
      }

      val resetColor = "\u001b[0m";

      return@map "$color $tileEngraving $resetColor"
    }

    .chunked(perLine)
    .joinToString("\n") { padLeft.plus(it.joinToString(" ")) }
}

fun determineAverageWin(
  hand: List<TileType>,
  melds: List<List<TileType>>,
  extraTileToConsider: TileType? = null
): List<List<TileType>>? {
  val handCopy = hand.toMutableList()
  if (null != extraTileToConsider) {
    handCopy.add(extraTileToConsider)
  }
  handCopy.sort()
  val sortedHand = handCopy.toList();

  val possibleEyes = sortedHand
    .filter { st -> sortedHand.count { it == st } >= 2 }
    .distinct()

  possibleEyes.forEach { eyeTile ->
    // remove the eye first
    val mutableRestHand = sortedHand.toMutableList()
    val mutableRestMelds: MutableList<List<TileType>> = mutableListOf()
    val chowOrphansToIgnore: MutableList<TileType> = mutableListOf()
    repeat(2) { mutableRestHand.remove(eyeTile) }
    mutableRestMelds.add(listOf(eyeTile, eyeTile))


    // try to find concealed kongs that are a pong and part of a chow
    while (true) {
      val firstPossibleKong = mutableRestHand.firstOrNull() { st -> mutableRestHand.count { st == it } >= 4 }
        ?: break;

      repeat(3) { mutableRestHand.remove(firstPossibleKong) }
      mutableRestMelds.add(listOf(firstPossibleKong, firstPossibleKong, firstPossibleKong))
    }

    // try to find all chows
    while (true) {
      val firstPossibleChow =
        mutableRestHand.firstOrNull() { st -> mutableRestHand.count { st == it } < 3 && st.numeral != null && st.suit != null }
          ?: break;

      val susSecond =
        mutableRestHand.find { it.numeral == firstPossibleChow.numeral?.plus(1) && it.suit == firstPossibleChow.suit }
      val susThird =
        mutableRestHand.find { it.numeral == firstPossibleChow.numeral?.plus(2) && it.suit == firstPossibleChow.suit }

      if (null != susSecond && null != susThird) {
        mutableRestHand.remove(firstPossibleChow)
        mutableRestHand.remove(susSecond)
        mutableRestHand.remove(susThird)
        mutableRestMelds.add(listOf(firstPossibleChow, susSecond, susThird))
      } else {
        mutableRestHand.remove(firstPossibleChow)
        chowOrphansToIgnore.add(firstPossibleChow)
      }
    }

    mutableRestHand.addAll(chowOrphansToIgnore)
    chowOrphansToIgnore.removeAll { true };
    mutableRestHand.sort()

    // try to find all pongs
    while (true) {
      val firstPossiblePong =
        mutableRestHand.firstOrNull() { st -> mutableRestHand.count { st == it } >= 3 }
          ?: break;
      repeat(3) { mutableRestHand.remove(firstPossiblePong) }
      mutableRestMelds.add(listOf(firstPossiblePong, firstPossiblePong, firstPossiblePong))
    }

    if (mutableRestHand.isEmpty()) {
      mutableRestMelds.addAll(melds)
      return mutableRestMelds;
    }
  }

  return null;
}

