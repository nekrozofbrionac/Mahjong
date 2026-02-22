package de.nekrozofbrionac.game.states

import GameState
import de.nekrozofbrionac.game.Game
import de.nekrozofbrionac.game.draw

class PlayerTurnStartState : GameState {

  override fun onStateEntry(game: Game) {
    game.turn++;

    val player = game.getCurrentPlayer();
    player.sortHand()


    // TODO: Handle drawing into a kong
    if (!draw(game.board, player)) {
      game.gameState = DrawnState();
      game.handleStateChange();
      return;
    }

    game.gameState = PlayerDiscardsState();
    game.handleStateChange();
  }
}