package de.nekrozofbrionac.mahjong.game.states

import GameState
import de.nekrozofbrionac.mahjong.game.Game
import de.nekrozofbrionac.mahjong.game.GameReport
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

    game.winner = game.getCurrentPlayer()
    game.winnerEval = eval

    game.terminate(
      GameReport(
        minFaan = game.minFaan,
        prevailingWind = Turn.getByIndex(game.wind),
        winnerSeat = Turn.getByIndex(game.turnPlayer),
        winnerScore = eval.first,
        winnerScoreDesc = eval.second,
        players = game.getPlayerInfos(),
      )
    )
  }
}