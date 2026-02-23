package de.nekrozofbrionac.game.states

import GameState
import de.nekrozofbrionac.determineAverageWin
import de.nekrozofbrionac.formatTiles
import de.nekrozofbrionac.game.Game
import de.nekrozofbrionac.game.Turn

class WinState : GameState {
  override fun onStateEntry(game: Game) {
    val winner = game.getCurrentPlayer()
    val winningMelds = determineAverageWin(winner.hand, winner.melds)
      ?: throw UnsupportedOperationException("Why are we in the winning game state when we didnt win?")

    val prevPlayer = game.getPlayerByIndex(game.turnPlayer - 1)
    val prevSeat = Turn.getByIndex(game.turnPlayer - 1)
    val nextPlayer = game.getPlayerByIndex(game.turnPlayer + 1)
    val nextSeat = Turn.getByIndex(game.turnPlayer + 1)
    val oppoPlayer = game.getPlayerByIndex(game.turnPlayer + 2)
    val oppoSeat = Turn.getByIndex(game.turnPlayer + 2)

    println()
    println("-------------- WINNER: ${Turn.getByIndex(game.turnPlayer)}-------------")
    println()
    println("Winning Melds:")
    println("    ${winningMelds.joinToString(" ") { formatTiles(it, 16, "") }}")
    println()
    println("Previous player: ${prevSeat}")
    println("    Hand:   ${prevPlayer.melds.joinToString(" ") { formatTiles(it, 16, "") }}  ${formatTiles(prevPlayer.hand, 14, "")}")
    println("    Flowers: ${formatTiles(prevPlayer.flowers, 8, "")}")
    println("Previous player: ${nextSeat}")
    println("    Hand:   ${nextPlayer.melds.joinToString(" ") { formatTiles(it, 16, "") }}  ${formatTiles(nextPlayer.hand, 14, "")}")
    println("    Flowers: ${formatTiles(nextPlayer.flowers, 8, "")}")
    println("Previous player: ${oppoSeat}")
    println("    Hand:   ${oppoPlayer.melds.joinToString(" ") { formatTiles(it, 16, "") }}  ${formatTiles(oppoPlayer.hand, 14, "")}")
    println("    Flowers: ${formatTiles(oppoPlayer.flowers, 8, "")}")
    println()
    println("---------------- GAME OVER --------------")

    game.terminate();
  }
}