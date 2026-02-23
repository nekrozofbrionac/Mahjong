package de.nekrozofbrionac.game.states

import GameState
import de.nekrozofbrionac.game.Game
import de.nekrozofbrionac.draw

class InitialState : GameState {
  override fun onStateEntry(game: Game) {
    repeat(13 * 4) {
      if (!draw(game.board, game.getCurrentPlayer())) {
        throw UnsupportedOperationException("The wall is too empty to start the game, how");
      }
      game.turnPlayer++
    }

    game.gameState = PlayerDrawsState();
    game.handleStateChange();
  }
}