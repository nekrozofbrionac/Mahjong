package de.nekrozofbrionac.mahjong.game.states

import GameState
import de.nekrozofbrionac.mahjong.game.Game
import de.nekrozofbrionac.mahjong.draw
import de.nekrozofbrionac.mahjong.game.Turn
import de.nekrozofbrionac.mahjong.hands.evaluateHand

class PlayerDrawsState : GameState {

  override fun onStateEntry(game: Game) {
    val player = game.getCurrentPlayer();
    player.sortHand()

    if (!draw(game.board, player)) {
      game.gameState = DrawnState();
      game.handleStateChange();
      return;
    }

    val eval = evaluateHand(
      Turn.getByIndex(game.wind),
      Turn.getByIndex(game.turnPlayer),
      player.hand,
      player.melds,
      player.flowers,
      null
    )
    if (eval.first >= game.minFaan) {
      if (player.playerControl.win(game.compilePlayerPov(game.turnPlayer))) {
        game.gameState = WinState();
        game.handleStateChange();
        return;
      }
    }

    // TODO: add kong draw to existing meld
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