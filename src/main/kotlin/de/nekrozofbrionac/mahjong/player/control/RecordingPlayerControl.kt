package de.nekrozofbrionac.mahjong.player.control

import de.nekrozofbrionac.mahjong.board.TileType
import de.nekrozofbrionac.mahjong.game.PlayerPov
import de.nekrozofbrionac.mahjong.player.control.bot.ml.Thought

class RecordingPlayerControl(
  val actualPlayerControl: PlayerControl
) : PlayerControl {
  private val recordedActions: MutableList<Triple<PlayerControlAction, PlayerPov, Thought>> = mutableListOf()
  var winRecorded: Boolean = false

  fun recording(): List<Triple<PlayerControlAction, PlayerPov, Thought>> {
    return recordedActions.toList()
  }

  fun reset() {
    recordedActions.clear()
    winRecorded = false
  }

  override fun win(pov: PlayerPov): Boolean {
    val result = actualPlayerControl.win(pov)
    winRecorded = result
    recordedActions.add(Triple(PlayerControlAction.WIN, pov, Thought(
      winning = if (result) 1.0f else 0.0f,
      discard01 = 0.0f,
      discard02 = 0.0f,
      discard03 = 0.0f,
      discard04 = 0.0f,
      discard05 = 0.0f,
      discard06 = 0.0f,
      discard07 = 0.0f,
      discard08 = 0.0f,
      discard09 = 0.0f,
      discard10 = 0.0f,
      discard11 = 0.0f,
      discard12 = 0.0f,
      discard13 = 0.0f,
      discard14 = 0.0f,
      kongFromDraw = 0.0f,
      kongFromDiscard = 0.0f,
      pongFromDiscard = 0.0f,
      chowFromDiscardLowest = 0.0f,
      chowFromDiscardMiddle = 0.0f,
      chowFromDiscardHighest = 0.0f,
      chowAtAll = 0.0f
    )));
    return result
  }

  override fun chooseDiscard(pov: PlayerPov): TileType {
    val result = actualPlayerControl.chooseDiscard(pov)
    recordedActions.add(Triple(PlayerControlAction.CHOOSE_DISCARD, pov, Thought(
      winning = 0.0f,
      discard01 = if (result == pov.hand.getOrNull(0)) 1.0f else 0.0f,
      discard02 = if (result == pov.hand.getOrNull(1)) 1.0f else 0.0f,
      discard03 = if (result == pov.hand.getOrNull(2)) 1.0f else 0.0f,
      discard04 = if (result == pov.hand.getOrNull(3)) 1.0f else 0.0f,
      discard05 = if (result == pov.hand.getOrNull(4)) 1.0f else 0.0f,
      discard06 = if (result == pov.hand.getOrNull(5)) 1.0f else 0.0f,
      discard07 = if (result == pov.hand.getOrNull(6)) 1.0f else 0.0f,
      discard08 = if (result == pov.hand.getOrNull(7)) 1.0f else 0.0f,
      discard09 = if (result == pov.hand.getOrNull(8)) 1.0f else 0.0f,
      discard10 = if (result == pov.hand.getOrNull(9)) 1.0f else 0.0f,
      discard11 = if (result == pov.hand.getOrNull(10)) 1.0f else 0.0f,
      discard12 = if (result == pov.hand.getOrNull(11)) 1.0f else 0.0f,
      discard13 = if (result == pov.hand.getOrNull(12)) 1.0f else 0.0f,
      discard14 = if (result == pov.hand.getOrNull(13)) 1.0f else 0.0f,
      kongFromDraw = 0.0f,
      kongFromDiscard = 0.0f,
      pongFromDiscard = 0.0f,
      chowFromDiscardLowest = 0.0f,
      chowFromDiscardMiddle = 0.0f,
      chowFromDiscardHighest = 0.0f,
      chowAtAll = 0.0f
    )));
    return result
  }

  override fun kongFromDraw(pov: PlayerPov): Boolean {
    val result = actualPlayerControl.kongFromDraw(pov)
    recordedActions.add(Triple(PlayerControlAction.KONG_FROM_DRAW, pov, Thought(
      winning = 0.0f,
      discard01 = 0.0f,
      discard02 = 0.0f,
      discard03 = 0.0f,
      discard04 = 0.0f,
      discard05 = 0.0f,
      discard06 = 0.0f,
      discard07 = 0.0f,
      discard08 = 0.0f,
      discard09 = 0.0f,
      discard10 = 0.0f,
      discard11 = 0.0f,
      discard12 = 0.0f,
      discard13 = 0.0f,
      discard14 = 0.0f,
      kongFromDraw = if (result) 1.0f else 0.0f,
      kongFromDiscard = 0.0f,
      pongFromDiscard = 0.0f,
      chowFromDiscardLowest = 0.0f,
      chowFromDiscardMiddle = 0.0f,
      chowFromDiscardHighest = 0.0f,
      chowAtAll = 0.0f
    )));
    return result
  }

  override fun kongFromDiscard(pov: PlayerPov): Boolean {
    val result = actualPlayerControl.kongFromDiscard(pov)
    recordedActions.add(Triple(PlayerControlAction.KONG_FROM_DISCARD, pov, Thought(
      winning = 0.0f,
      discard01 = 0.0f,
      discard02 = 0.0f,
      discard03 = 0.0f,
      discard04 = 0.0f,
      discard05 = 0.0f,
      discard06 = 0.0f,
      discard07 = 0.0f,
      discard08 = 0.0f,
      discard09 = 0.0f,
      discard10 = 0.0f,
      discard11 = 0.0f,
      discard12 = 0.0f,
      discard13 = 0.0f,
      discard14 = 0.0f,
      kongFromDraw = 0.0f,
      kongFromDiscard = if (result) 1.0f else 0.0f,
      pongFromDiscard = 0.0f,
      chowFromDiscardLowest = 0.0f,
      chowFromDiscardMiddle = 0.0f,
      chowFromDiscardHighest = 0.0f,
      chowAtAll = 0.0f
    )));
    return result
  }

  override fun pongFromDiscard(pov: PlayerPov): Boolean {
    val result = actualPlayerControl.pongFromDiscard(pov)
    recordedActions.add(Triple(PlayerControlAction.PONG_FROM_DISCARD, pov, Thought(
      winning = 0.0f,
      discard01 = 0.0f,
      discard02 = 0.0f,
      discard03 = 0.0f,
      discard04 = 0.0f,
      discard05 = 0.0f,
      discard06 = 0.0f,
      discard07 = 0.0f,
      discard08 = 0.0f,
      discard09 = 0.0f,
      discard10 = 0.0f,
      discard11 = 0.0f,
      discard12 = 0.0f,
      discard13 = 0.0f,
      discard14 = 0.0f,
      kongFromDraw = 0.0f,
      kongFromDiscard = 0.0f,
      pongFromDiscard = if (result) 1.0f else 0.0f,
      chowFromDiscardLowest = 0.0f,
      chowFromDiscardMiddle = 0.0f,
      chowFromDiscardHighest = 0.0f,
      chowAtAll = 0.0f
    )));
    return result
  }

  override fun chowFromDiscard(pov: PlayerPov): TileType? {
    val result = actualPlayerControl.chowFromDiscard(pov)
    recordedActions.add(Triple(PlayerControlAction.CHOW_FROM_DISCARD, pov, Thought(
      winning = 0.0f,
      discard01 = 0.0f,
      discard02 = 0.0f,
      discard03 = 0.0f,
      discard04 = 0.0f,
      discard05 = 0.0f,
      discard06 = 0.0f,
      discard07 = 0.0f,
      discard08 = 0.0f,
      discard09 = 0.0f,
      discard10 = 0.0f,
      discard11 = 0.0f,
      discard12 = 0.0f,
      discard13 = 0.0f,
      discard14 = 0.0f,
      kongFromDraw = 0.0f,
      kongFromDiscard = 0.0f,
      pongFromDiscard = 0.0f,
      chowFromDiscardLowest = if (result != null && result.numeral == pov.discarded.lastOrNull()?.numeral) 1.0f else 0.0f,
      chowFromDiscardMiddle = if (result != null && result.numeral == pov.discarded.lastOrNull()?.numeral?.minus(1)) 1.0f else 0.0f,
      chowFromDiscardHighest = if (result != null && result.numeral == pov.discarded.lastOrNull()?.numeral?.minus(2)) 1.0f else 0.0f,
      chowAtAll = if (result != null) 1.0f else 0.0f
    )));
    return result
  }
}