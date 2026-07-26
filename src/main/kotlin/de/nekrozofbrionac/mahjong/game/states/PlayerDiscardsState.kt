package de.nekrozofbrionac.mahjong.game.states

import GameState
import de.nekrozofbrionac.mahjong.hands.determineAverageWin
import de.nekrozofbrionac.mahjong.game.Game
import de.nekrozofbrionac.mahjong.game.Turn
import de.nekrozofbrionac.mahjong.hands.evaluateHand

class PlayerDiscardsState : GameState {
  override fun onStateEntry(game: Game) {
    val player = game.getCurrentPlayer();
    val discardedTile = player.playerControl.chooseDiscard(game.compilePlayerPov(game.turnPlayer));
    game.board.addToDiscard(player.discard(discardedTile));
    player.sortHand();

    // Priority: Win, Kong, Pong, Chow

    for (winIndex in 0..3) {
      val susWinner = game.getPlayerByIndex(winIndex)

      // find out if winning hand?
      val eval = evaluateHand(
        Turn.getByIndex(game.wind),
        Turn.getByIndex(winIndex),
        susWinner.hand,
        susWinner.melds,
        susWinner.flowers,
        discardedTile
      )

      if (eval.first >= game.minFaan) {
        if (susWinner.playerControl.win(game.compilePlayerPov(winIndex))) {
          susWinner.addToHand(game.board.popLastDiscard());
          game.turnPlayer = winIndex;
          game.gameState = WinState();
          game.handleStateChange();
          return;
        }
      }
    }


    for (kongIndex in 0..3) {
      val kongPlayer = game.getPlayerByIndex(kongIndex);
      if (kongPlayer.isKongAvailableInHand(discardedTile)) {
        if (kongPlayer.playerControl.kongFromDiscard(game.compilePlayerPov(kongIndex))) {
          val poppedDiscard = game.board.popLastDiscard()
          kongPlayer.addToHand(poppedDiscard)
          kongPlayer.formKongMeld(poppedDiscard);
          game.turnPlayer = kongIndex;
          game.turn++;
          game.gameState = PlayerDrawsState()
          game.handleStateChange();
          return;
        }
      }
    }

    for (pongIndex in 0..3) {
      val pongPlayer = game.getPlayerByIndex(pongIndex);
      if (pongPlayer.isPongAvailable(discardedTile)) {
        if (pongPlayer.playerControl.pongFromDiscard(game.compilePlayerPov(pongIndex))) {
          val poppedDiscard = game.board.popLastDiscard()
          pongPlayer.addToHand(poppedDiscard);
          pongPlayer.formPongMeld(poppedDiscard);
          game.turnPlayer = pongIndex;
          game.turn++;
          game.gameState = PlayerDiscardsState(); //... do i really need to do that.
          game.handleStateChange();
          return;
        }
      }
    }

    val maybeChowPlayer = game.getPlayerByIndex(game.turnPlayer + 1)
    if (maybeChowPlayer.isChowAvailable(discardedTile)) {
      val choice = maybeChowPlayer.playerControl.chowFromDiscard(game.compilePlayerPov(game.turnPlayer + 1))
      if (null != choice) {
        val poppedDiscard = game.board.popLastDiscard()
        maybeChowPlayer.addToHand(poppedDiscard);
        maybeChowPlayer.formChowMeld(choice);
        game.turnPlayer++;
        game.turn++;
        game.gameState = PlayerDiscardsState(); //... do i really need to do that.
        game.handleStateChange();
        return;
      }
    }

    game.turnPlayer++
    game.turn++;
    game.gameState = PlayerDrawsState()
    game.handleStateChange();
  }
}