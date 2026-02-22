package de.nekrozofbrionac.game

import GameState
import Player
import de.nekrozofbrionac.board.SimpleBoard
import de.nekrozofbrionac.formatTiles
import de.nekrozofbrionac.game.states.InitialState
import de.nekrozofbrionac.mahjong.board.Board
import de.nekrozofbrionac.mahjong.board.TileType

enum class Turn {
  EAST,
  SOUTH,
  WEST,
  NORTH,
}

data class PlayerPov(
  val playerSeat: Turn,

  val prevSeat: Turn,
  val nextSeat: Turn,
  val oppoSeat: Turn,

  val prevMelds: List<List<TileType>>,
  val nextMelds: List<List<TileType>>,
  val oppoMelds: List<List<TileType>>,

  val prevFlowers: List<TileType>,
  val nextFlowers: List<TileType>,
  val oppoFlowers: List<TileType>,

  val discarded: List<TileType>,
  val wallRemaining: Int,

  val hand: List<TileType>,
  val melds: List<List<TileType>>,
  val flowers: List<TileType>,
)

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

  fun compilePlayerPov(index: Int): PlayerPov {
    val prev = getPlayerByIndex(index - 1)
    val pov = getPlayerByIndex(index)
    val next = getPlayerByIndex(index + 1)
    val oppo = getPlayerByIndex(index + 2)

    return PlayerPov(
      playerSeat = Turn.entries[index],
      prevSeat = Turn.entries[index+3 % 4],
      nextSeat = Turn.entries[index+1 % 4],
      oppoSeat = Turn.entries[index+2 % 4],
      prevMelds = prev.melds,
      nextMelds = next.melds,
      oppoMelds = oppo.melds,
      prevFlowers = prev.flowers,
      nextFlowers = next.flowers,
      oppoFlowers = oppo.flowers,
      discarded = board.discarded,
      wallRemaining = board.wall.size,
      hand = pov.hand,
      melds = pov.melds,
      flowers = pov.flowers,
    )
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
    println(formatTiles(board.discarded, 18))
    println("Current player ($turnPlayer):")
    println("    Hand:")
    println(formatTiles(getCurrentPlayer().hand, 18))
    println("    Melds:")
    getCurrentPlayer().melds.forEach { println(formatTiles(it, 16)) }
    println("    Flowers:")
    println(formatTiles(getCurrentPlayer().flowers, 18 ))
  }
}