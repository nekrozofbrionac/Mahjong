package de.nekrozofbrionac.game.states

import GameState
import de.nekrozofbrionac.determineAverageWin
import de.nekrozofbrionac.game.Game
import de.nekrozofbrionac.draw

class PlayerTurnStartState : GameState {

  override fun onStateEntry(game: Game) {
    val player = game.getCurrentPlayer();
    player.sortHand()


    // TODO: Handle drawing into a kong
    if (!draw(game.board, player)) {
      game.gameState = DrawnState();
      game.handleStateChange();
      return;
    }

    if (determineAverageWin(player.hand, player.melds) != null) {
      if (player.playerControl.win(game.compilePlayerPov(game.turnPlayer))) {
        game.gameState = WinState();
        game.handleStateChange();
        return;
      }
    }

    game.gameState = PlayerDiscardsState();
    game.handleStateChange();
  }
}