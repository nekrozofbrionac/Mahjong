package de.nekrozofbrionac.game.states

import GameState
import de.nekrozofbrionac.game.Game

class PlayerDiscardsState: GameState {
  override fun onStateEntry(game: Game) {
    val player = game.getCurrentPlayer();
    val playerPov = player.playerControl.chooseDiscard(game.compilePlayerPov(game.turnPlayer));
    game.board.addToDiscard(player.discard(playerPov));
    player.sortHand();

    // Priority: Win, Kong, Pong, Chow

    // TODO: Win

    for (kongIndex in 0..3) {
      val kongPlayer = game.getPlayerByIndex(kongIndex);
      if (kongPlayer.isKongAvailableInHand(playerPov)) {
        if (kongPlayer.playerControl.kongFromDiscard(game.compilePlayerPov(kongIndex))) {
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

    for (pongIndex in 0..3) {
      val pongPlayer = game.getPlayerByIndex(pongIndex);
      if (pongPlayer.isPongAvailable(playerPov)) {
        if (pongPlayer.playerControl.pongFromDiscard(game.compilePlayerPov(pongIndex))) {
          val poppedDiscard = game.board.popLastDiscard()
          pongPlayer.addToHand(poppedDiscard);
          pongPlayer.formPongMeld(poppedDiscard);
          game.turnPlayer = pongIndex;
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