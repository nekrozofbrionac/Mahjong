package de.nekrozofbrionac.game.states

import GameState
import de.nekrozofbrionac.game.Game

class PlayerTurnStartState: GameState{

  override fun onStateEntry(game: Game) {
    game.turn++;

    val player = game.getCurrentPlayer();
    player.sortHand()

    val drawnTile = game.board.drawFromWall();

    if (null == drawnTile) {
      game.gameState = DrawnState();
      game.handleStateChange();
      return;
    }

    player.addToHand(drawnTile);
    game.gameState = PlayerDiscardsState();
    game.handleStateChange();
  }
}