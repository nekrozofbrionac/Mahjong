package de.nekrozofbrionac.mahjong.game.states

import GameState
import de.nekrozofbrionac.mahjong.formatTiles
import de.nekrozofbrionac.mahjong.game.Game
import de.nekrozofbrionac.mahjong.game.Turn
import de.nekrozofbrionac.mahjong.hands.evaluateHand

class WinState : GameState {
  override fun onStateEntry(game: Game) {
    val winner = game.getCurrentPlayer()

    val eval = evaluateHand(
      Turn.getByIndex(game.wind),
      Turn.getByIndex(game.turnPlayer),
      winner.hand,
      winner.melds,
      winner.flowers,
      null
    )
    val winningMelds = eval.third

    if (!game.verbose) {
      println("\nWinner: ${Turn.getByIndex(game.turnPlayer)}")
      println("Winning Hand: ${eval.first} faan ${eval.second}\n              ${winningMelds.joinToString(" ") { formatTiles(it, 16, "") }}")
      game.terminate();
      return;
    }

    val prevPlayer = game.getPlayerByIndex(game.turnPlayer - 1)
    val prevSeat = Turn.getByIndex(game.turnPlayer - 1)
    val nextPlayer = game.getPlayerByIndex(game.turnPlayer + 1)
    val nextSeat = Turn.getByIndex(game.turnPlayer + 1)
    val oppoPlayer = game.getPlayerByIndex(game.turnPlayer + 2)
    val oppoSeat = Turn.getByIndex(game.turnPlayer + 2)

    println()
    println("-------------- WINNER: ${Turn.getByIndex(game.turnPlayer)}-------------")
    println()
    println("Winning Hand: ${eval.first} faan ${eval.second}")
    println("    ${winningMelds.joinToString(" ") { formatTiles(it, 16, "") }}")
    println()
    println("Previous player: ${prevSeat}")
    println("    Hand:   ${prevPlayer.melds.joinToString(" ") { formatTiles(it, 16, "") }}  ${formatTiles(prevPlayer.hand, 14, "")}")
    println("    Flowers: ${formatTiles(prevPlayer.flowers, 8, "")}")
    println("Next player: ${nextSeat}")
    println("    Hand:   ${nextPlayer.melds.joinToString(" ") { formatTiles(it, 16, "") }}  ${formatTiles(nextPlayer.hand, 14, "")}")
    println("    Flowers: ${formatTiles(nextPlayer.flowers, 8, "")}")
    println("Oppo player: ${oppoSeat}")
    println("    Hand:   ${oppoPlayer.melds.joinToString(" ") { formatTiles(it, 16, "") }}  ${formatTiles(oppoPlayer.hand, 14, "")}")
    println("    Flowers: ${formatTiles(oppoPlayer.flowers, 8, "")}")
    println()
    println("---------------- GAME OVER --------------")

    game.terminate();
  }
}