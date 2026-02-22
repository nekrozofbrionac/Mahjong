package de.nekrozofbrionac.game.states

import GameState
import de.nekrozofbrionac.game.Game

class PlayerDiscardsState: GameState {
  override fun onStateEntry(game: Game) {
    val player = game.getCurrentPlayer();
    val discardedTile = player.playerControl.chooseDiscard(player);
    game.board.addToDiscard(player.discard(discardedTile));
    player.sortHand();

    // Priority: Win, Kong, Pong, Chow

    // TODO: Win

    for (kongIndex in 0..3) {
      val kongPlayer = game.getPlayerByIndex(kongIndex);
      if (kongPlayer.kongAvailableInHand(discardedTile)) {
        if (kongPlayer.playerControl.kongFromDiscard(kongPlayer, discardedTile)) {
          val poppedDiscard = game.board.popLastDiscard()
          kongPlayer.addToHand(poppedDiscard)
          kongPlayer.formKongMeld(poppedDiscard);
          game.turnPlayer = kongIndex;
          game.gameState = PlayerTurnStartState()
          game.handleStateChange();
          return;
        }
      }
    }

    for (i in 0..3) {
      val pongPlayer = game.getPlayerByIndex(i);
      if (pongPlayer.pongAvailable(discardedTile)) {
        if (pongPlayer.playerControl.pongFromDiscard(pongPlayer, discardedTile)) {
          val poppedDiscard = game.board.popLastDiscard()
          pongPlayer.addToHand(poppedDiscard);
          pongPlayer.formPongMeld(poppedDiscard);
          game.turnPlayer = i;
          game.gameState = PlayerDiscardsState(); //... do i really need to do that.
          game.handleStateChange();
          return;
        }
      }
    }

    /* TODO
    val maybeChowPlayer = game.getPlayerByIndex(game.turnPlayer+1)
    if (maybeChowPlayer.chowAvailable(discardedTile)) {

    }
     */

    game.turnPlayer++
    game.gameState = PlayerTurnStartState()
    game.handleStateChange();
  }
}