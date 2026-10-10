package de.nekrozofbrionac.mahjong.player.control.bot.ml

import de.nekrozofbrionac.mahjong.board.TileType
import de.nekrozofbrionac.mahjong.board.NUMBER_OF_FLOWERS

enum class PlayerControlAction(val outputSize: Int, val predictionType: PredictionType) {
  WIN(1, PredictionType.LOGISTIC),
  CHOOSE_DISCARD(TileType.entries.size - NUMBER_OF_FLOWERS, PredictionType.SOFTMAX), //
  KONG_FROM_DRAW(1, PredictionType.LOGISTIC),
  KONG_FROM_DISCARD(1, PredictionType.LOGISTIC),
  PONG_FROM_DISCARD(1, PredictionType.LOGISTIC),
  CHOW_FROM_DISCARD(4, PredictionType.SOFTMAX),
}