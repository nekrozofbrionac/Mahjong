package de.nekrozofbrionac.game

import GameState
import Player
import de.nekrozofbrionac.board.SimpleBoard
import de.nekrozofbrionac.game.states.InitialState
import de.nekrozofbrionac.mahjong.board.Board
import de.nekrozofbrionac.mahjong.board.TileType

enum class Turn {
  EAST,
  SOUTH,
  WEST,
  NORTH,
}

class Game(
  val playerEast: Player,
  val playerSouth: Player,
  val playerWest: Player,
  val playerNorth: Player
) {
  val board: Board = SimpleBoard();
  val wind: Int = 0;
  var turn: Int = 0;
  var turnPlayer: Int = 0
    set(value) {
      field = value % 4
    };

  var gameState: GameState = InitialState();

  fun start() {
    handleStateChange();
  }

  fun getCurrentPlayer(): Player {
    return when (turnPlayer) {
      0 -> playerEast;
      1 -> playerSouth;
      2 -> playerWest;
      3 -> playerNorth;
      else -> {
        this.turnPlayer %= 4;
        return this.getCurrentPlayer()
      }
    }
  }

  fun getPlayerByIndex(index: Int): Player {
    val normalizedIndex = (index + 4) % 4;

    return when (normalizedIndex) {
      0 -> playerEast;
      1 -> playerSouth;
      2 -> playerWest;
      3 -> playerNorth;
      else -> {
        return this.getPlayerByIndex(index)
      }
    }
  }

  fun getDiscardedTile(): TileType {
    return board.discarded.last()
  }

  fun handleStateChange() {
    printGame();
    this.gameState.onStateEntry(this);
  }


  fun terminate() {
    println("This game is over.");
  }

  private fun printGame() {
    println()
    println()
    println("Current game state: ${gameState.javaClass.name.substringAfterLast(".")}")
    println("Current player: $turnPlayer")
    println("Board:")
    println("    Wall remaining: ${board.wall.size}")
    println("    Discard pile:")
    println(formatTiles(board.discarded, 18, 12))
    println("Current player ($turnPlayer):")
    println("    Hand:")
    println(formatTiles(getCurrentPlayer().hand, 18, 12))
    println("    Melds:")
    getCurrentPlayer().melds.forEach { println(formatTiles(it, 16, 12)) }
    println("    Flowers:")
    println(formatTiles(getCurrentPlayer().flowers, 18, 12))
  }

  private fun formatTiles(tiles: List<TileType>, perLine: Int, paddingLeft: Int): String {
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
            };

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
      .joinToString("\n") { "        ".plus(it.joinToString(" ")) }
  }


}