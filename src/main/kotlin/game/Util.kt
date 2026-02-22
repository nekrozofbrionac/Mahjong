package de.nekrozofbrionac.game

import Player
import de.nekrozofbrionac.mahjong.board.Board

fun draw(board: Board, player: Player): Boolean {

  val drawnTile = board.drawFromWall()
    ?: return false;
  player.addToHand(drawnTile);

  if (drawnTile.isFlower()) {
    return draw(board, player);
  }
  return true
}