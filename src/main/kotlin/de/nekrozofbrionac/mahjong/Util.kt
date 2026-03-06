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

      val tileEngraving = if (it.suit != null) {
        it.shortHand.substring(1, 2).lowercase().padStart(maxLength)
      } else {
        it.shortHand.uppercase().padEnd(maxLength)
      }

      val resetColor = "\u001b[0m";

      return@map "$color $tileEngraving $resetColor"
    }
    .chunked(perLine)
    .joinToString("\n") { padLeft.plus(it.joinToString(" ")) }
}

