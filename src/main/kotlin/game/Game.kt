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
    println("Current game state: ${gameState.javaClass.name}")
    println("Board:")
    println("    Wall remaining: ${board.wall.size}")
    println("    Discard pile:")
    println(formatTiles(board.discarded, 9, 12))
    println("Current player: $turnPlayer")
    println("    Hand:")
    println(formatTiles(getCurrentPlayer().hand, 6, 12))
    println("    Melds:")
    println("         (None yet. Todo)")
    println("    Flowers:")
    println(formatTiles(getCurrentPlayer().flowers, 6, 12))
  }

  private fun formatTiles(tiles: List<TileType>, perLine: Int, paddingLeft: Int): String {
    val maxLength = TileType.entries.maxOf { it.shortHand.length }
    return tiles
      .map { it.shortHand.uppercase().padEnd(maxLength) }
      .chunked(perLine)
      .joinToString("\n") { "        ".plus(it.joinToString(" ")) }
  }


}