package de.nekrozofbrionac.mahjong.game.states

import GameState
import de.nekrozofbrionac.mahjong.game.Game
import de.nekrozofbrionac.mahjong.game.GameReport
import de.nekrozofbrionac.mahjong.game.Turn

class DrawnState: GameState {
  override fun onStateEntry(game: Game) {
    game.winner = null;
    game.winnerEval = null;
    game.terminate(GameReport(
      minFaan = game.minFaan,
      prevailingWind = Turn.getByIndex(game.wind),
      winnerSeat = null,
      winnerScore = null,
      winnerScoreDesc = null,
      players = game.getPlayerInfos(),
    ));
  }
}