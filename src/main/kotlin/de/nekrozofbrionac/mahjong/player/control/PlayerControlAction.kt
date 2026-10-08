package de.nekrozofbrionac.mahjong.player.control

enum class PlayerControlAction(val outputSize: Int) {
  WIN(1),
  CHOOSE_DISCARD(14),
  KONG_FROM_DRAW(1),
  KONG_FROM_DISCARD(1),
  PONG_FROM_DISCARD(1),
  CHOW_FROM_DISCARD(4),
}