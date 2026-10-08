package de.nekrozofbrionac.mahjong.player.control

import de.nekrozofbrionac.mahjong.board.TileType
import de.nekrozofbrionac.mahjong.game.PlayerPov

data class RecordedAction(
  val action: PlayerControlAction,
  val pov: PlayerPov,
  val decision: FloatArray,
) {
  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (javaClass != other?.javaClass) return false

    other as RecordedAction

    if (action != other.action) return false
    if (pov != other.pov) return false
    if (!decision.contentEquals(other.decision)) return false

    return true
  }

  override fun hashCode(): Int {
    var result = action.hashCode()
    result = 31 * result + pov.hashCode()
    result = 31 * result + decision.contentHashCode()
    return result
  }
}

class RecordingPlayerControl(
  val actualPlayerControl: PlayerControl
) : PlayerControl {
  private val recordedActions: MutableList<RecordedAction> = mutableListOf()
  var winRecorded: Boolean = false

  fun recording(): List<RecordedAction> {
    return recordedActions.toList()
  }

  fun reset() {
    recordedActions.clear()
    winRecorded = false
  }

  override fun win(pov: PlayerPov): Boolean {
    val result = actualPlayerControl.win(pov)
    winRecorded = result
    recordedActions.add(RecordedAction(PlayerControlAction.WIN, pov, floatArrayOf((if (result) 1.0f else 0.0f))));
    return result
  }

  override fun chooseDiscard(pov: PlayerPov): TileType {
    val result = actualPlayerControl.chooseDiscard(pov)
    recordedActions.add(RecordedAction(
      PlayerControlAction.CHOOSE_DISCARD,
      pov,
      floatArrayOf(
        if (result == pov.hand.getOrNull(0)) 1.0f else 0.0f,
        if (result == pov.hand.getOrNull(1)) 1.0f else 0.0f,
        if (result == pov.hand.getOrNull(2)) 1.0f else 0.0f,
        if (result == pov.hand.getOrNull(3)) 1.0f else 0.0f,
        if (result == pov.hand.getOrNull(4)) 1.0f else 0.0f,
        if (result == pov.hand.getOrNull(5)) 1.0f else 0.0f,
        if (result == pov.hand.getOrNull(6)) 1.0f else 0.0f,
        if (result == pov.hand.getOrNull(7)) 1.0f else 0.0f,
        if (result == pov.hand.getOrNull(8)) 1.0f else 0.0f,
        if (result == pov.hand.getOrNull(9)) 1.0f else 0.0f,
        if (result == pov.hand.getOrNull(10)) 1.0f else 0.0f,
        if (result == pov.hand.getOrNull(11)) 1.0f else 0.0f,
        if (result == pov.hand.getOrNull(12)) 1.0f else 0.0f,
        if (result == pov.hand.getOrNull(13)) 1.0f else 0.0f,
      )
    ));
    return result
  }

  override fun kongFromDraw(pov: PlayerPov): Boolean {
    val result = actualPlayerControl.kongFromDraw(pov)
    recordedActions.add(RecordedAction(
      PlayerControlAction.KONG_FROM_DRAW,
      pov,
      floatArrayOf(if (result) 1.0f else 0.0f)
    ));
    return result
  }

  override fun kongFromDiscard(pov: PlayerPov): Boolean {
    val result = actualPlayerControl.kongFromDiscard(pov)
    recordedActions.add(
      RecordedAction(
        PlayerControlAction.KONG_FROM_DISCARD, pov, floatArrayOf(
          if (result) 1.0f else 0.0f,
        )
      )
    );
    return result
  }

  override fun pongFromDiscard(pov: PlayerPov): Boolean {
    val result = actualPlayerControl.pongFromDiscard(pov)
    recordedActions.add(
      RecordedAction(
        PlayerControlAction.PONG_FROM_DISCARD, pov, floatArrayOf(
          if (result) 1.0f else 0.0f,
        )
      )
    );
    return result
  }

  override fun chowFromDiscard(pov: PlayerPov): TileType? {
    val result = actualPlayerControl.chowFromDiscard(pov)
    recordedActions.add(
      RecordedAction(
        PlayerControlAction.CHOW_FROM_DISCARD, pov, floatArrayOf(
          if (result != null) 1.0f else 0.0f,
          if (result != null && result.numeral == pov.discarded.lastOrNull()?.numeral) 1.0f else 0.0f,
          if (result != null && result.numeral == pov.discarded.lastOrNull()?.numeral?.minus(1)) 1.0f else 0.0f,
          if (result != null && result.numeral == pov.discarded.lastOrNull()?.numeral?.minus(2)) 1.0f else 0.0f,
        )
      )
    );
    return result
  }
}