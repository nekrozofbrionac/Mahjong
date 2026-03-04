package de.nekrozofbrionac.mahjong.game.states

import GameState
import de.nekrozofbrionac.mahjong.hands.determineAverageWin
import de.nekrozofbrionac.mahjong.game.Game
import de.nekrozofbrionac.mahjong.draw

class PlayerDrawsState : GameState {

  override fun onStateEntry(game: Game) {
    val player = game.getCurrentPlayer();
    player.sortHand()

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

    val lastDrawnTile = player.hand.last() // exists because of checking earlier
    if (player.isKongAvailableFromDraw(lastDrawnTile)) {
      if (player.playerControl.kongFromDraw(game.compilePlayerPov(game.turnPlayer))) {
        player.formKongMeld(lastDrawnTile)
        game.gameState = PlayerDrawsState()
        game.handleStateChange();
        return;
      }
    }

    game.gameState = PlayerDiscardsState();
    game.handleStateChange();
  }
}