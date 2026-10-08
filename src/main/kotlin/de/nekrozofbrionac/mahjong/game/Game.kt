package de.nekrozofbrionac.mahjong.game

import GameState
import de.nekrozofbrionac.mahjong.player.Player
import de.nekrozofbrionac.mahjong.board.SimpleBoard
import de.nekrozofbrionac.mahjong.formatTiles
import de.nekrozofbrionac.mahjong.game.states.InitialState
import de.nekrozofbrionac.mahjong.board.Board
import de.nekrozofbrionac.mahjong.board.TileType

enum class Turn {
  EAST,
  SOUTH,
  WEST,
  NORTH,

  ;

  companion object {
    fun getByIndex(index: Int): Turn {
      return entries[(index + 4) % 4]
    }
  }
}

data class PlayerInfo(
  val name: String,
  val fullHand: List<TileType>,
  val flowers: List<TileType>,
)

data class GameReport(
  val minFaan: Int,
  val prevailingWind: Turn,

  val winnerSeat: Turn?,
  val winnerScore: Int?,
  val winnerScoreDesc: String?,

  val players: Map<Turn, PlayerInfo>
)

data class PlayerPov(
  val turn: Int,
  val currentTurnPlayer: Turn,
  val currentTurnPlayerName: String,
  val minFaan: Int,

  val playerSeat: Turn,
  val prevailingWind: Turn,
  val name: String,

  val prevName: String,
  val nextName: String,
  val oppoName: String,

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
  val playerNorth: Player,
  val onTermination: (gameReport: GameReport) -> Unit = { _ -> },
  val verbose: Boolean = false,
  val wind: Int = 0,
  val minFaan: Int = 3,
) {
  val board: Board = SimpleBoard();
  var turn: Int = 0;
  var turnPlayer: Int = 0
    set(value) {
      field = value % 4
    };

  var winner: Player? = null
  var winnerEval: Triple<Int, String, List<List<TileType>>>? = null

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

  fun getPlayerInfos(): Map<Turn, PlayerInfo> {
    val output: MutableMap<Turn, PlayerInfo> = mutableMapOf()
    repeat(4) {
      val player =  getPlayerByIndex(it)
      output[Turn.getByIndex(it)] = PlayerInfo(
        name = player.name,
        fullHand = player.melds.flatten() + player.hand,
        flowers = player.flowers
      )
    }
    return output;
  }

  fun compilePlayerPov(index: Int): PlayerPov {
    val prev = getPlayerByIndex(index - 1)
    val pov = getPlayerByIndex(index)
    val next = getPlayerByIndex(index + 1)
    val oppo = getPlayerByIndex(index + 2)

    return PlayerPov(
      turn = turn,
      currentTurnPlayer = Turn.getByIndex(turnPlayer),
      currentTurnPlayerName = getCurrentPlayer().name,
      minFaan = minFaan,
      playerSeat = Turn.getByIndex(index),
      prevailingWind = Turn.getByIndex(wind),
      prevSeat = Turn.getByIndex(index - 1),
      nextSeat = Turn.getByIndex(index + 1),
      oppoSeat = Turn.getByIndex(index + 2),
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
      name = pov.name,
      prevName = prev.name,
      nextName = next.name,
      oppoName = oppo.name,
    )
  }

  fun handleStateChange() {
    if (verbose) {
      printGame();
    }
    this.gameState.onStateEntry(this);
  }

  fun terminate(gameReport: GameReport) {
    if (winnerEval == null || winner == null) {
      onTermination(gameReport)
    } else {
      onTermination(gameReport)
    }
  }

  private fun printGame() {
    println()
    println()
    println("Current game state: ${gameState.javaClass.name.substringAfterLast(".")}")
    println("Current player: ${Turn.getByIndex(turnPlayer)}")
    println("Board:")
    println("    Wall remaining: ${board.wall.size}")
    println("    Discard pile:")
    println(formatTiles(board.discarded, 18))
    println("Current player: ${Turn.getByIndex(turnPlayer)}")
    println("    Hand:")
    println(formatTiles(getCurrentPlayer().hand, 18))
    println("    Melds:")
    getCurrentPlayer().melds.forEach { println(formatTiles(it, 16)) }
    println("    Flowers:")
    println(formatTiles(getCurrentPlayer().flowers, 18))
  }
}