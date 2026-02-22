package de.nekrozofbrionac.game.states

import GameState
import de.nekrozofbrionac.game.Game

class InitialState : GameState {
  override fun onStateEntry(game: Game) {
    repeat(12 * 4) {
      val drawnTile = game.board.drawFromWall()
        ?: throw UnsupportedOperationException("The wall is too empty to start the game, how");

      game.getCurrentPlayer().addToHand(drawnTile);
      game.turnPlayer++
    }



    game.gameState = PlayerTurnStartState();
    game.handleStateChange();
  }
}