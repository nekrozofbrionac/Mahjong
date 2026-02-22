package de.nekrozofbrionac.game.states

import GameState
import de.nekrozofbrionac.game.Game

class PlayerDiscardsState: GameState {
  override fun onStateEntry(game: Game) {
    val player = game.getCurrentPlayer();
    val tile = player.playerControl.chooseDiscard(player);
    game.board.addToDiscard(player.discard(tile));
    player.sortHand();

    game.turnPlayer++
    game.gameState = PlayerTurnStartState()
    game.handleStateChange();
  }
}