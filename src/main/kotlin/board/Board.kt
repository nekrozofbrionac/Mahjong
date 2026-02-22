package de.nekrozofbrionac.mahjong.board;

interface Board {
  val wall: List<TileType>;
  val discarded: List<TileType>;

  fun drawFromWall(): TileType?;
  fun addToDiscard(tile: TileType?)
}