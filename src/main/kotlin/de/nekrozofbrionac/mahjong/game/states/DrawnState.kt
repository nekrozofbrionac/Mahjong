package de.nekrozofbrionac.mahjong.game.states

import GameState
import de.nekrozofbrionac.mahjong.game.Game

class DrawnState: GameState {
  override fun onStateEntry(game: Game) {
    if (game.verbose) {
      println("Drawn game.")
    }
    game.terminate();
  }
}