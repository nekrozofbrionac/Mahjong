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
        this.turnPlayer %= 4; return this.getCurrentPlayer()
      }
    }
  }

  fun getDiscardedTile(): TileType {
    return board.discarded.last()
  }

  fun handleStateChange() {
    this.gameState.onStateEntry(this);
  }


  fun terminate() {
    println("This game is over.");
  }
}