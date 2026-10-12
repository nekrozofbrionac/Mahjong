package de.nekrozofbrionac.mahjong.player.control.bot.ml

import de.nekrozofbrionac.mahjong.board.TileType
import de.nekrozofbrionac.mahjong.game.PlayerPov
import de.nekrozofbrionac.mahjong.player.control.PlayerControl

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
    recordedActions.add(RecordedAction(PlayerControlAction.WIN, deepPovCopy(pov), floatArrayOf((if (result) 1.0f else 0.0f))));
    return result
  }

  override fun chooseDiscard(pov: PlayerPov): TileType {
    val result = actualPlayerControl.chooseDiscard(pov)
    if (result !in pov.hand) {
      throw IllegalStateException("chooseDiscard returned a tile not in hand: $result, hand: ${pov.hand}")
    }
    // println("RecordingPlayerControl: chooseDiscard returned ${formatTile(result)}, hand: ${formatTiles(pov.hand.distinct().sorted(), 14, "")}")
    recordedActions.add(
      RecordedAction(
        PlayerControlAction.CHOOSE_DISCARD,
        deepPovCopy(pov),
        tileTypeToWeightsWithoutFlowers(result)
      )
    );
    return result
  }

  override fun kongFromDraw(pov: PlayerPov): Boolean {
    val result = actualPlayerControl.kongFromDraw(pov)
    recordedActions.add(
      RecordedAction(
        PlayerControlAction.KONG_FROM_DRAW,
        deepPovCopy(pov),
        floatArrayOf(if (result) 1.0f else 0.0f)
      )
    );
    return result
  }

  override fun kongFromDiscard(pov: PlayerPov): Boolean {
    val result = actualPlayerControl.kongFromDiscard(pov)
    recordedActions.add(
      RecordedAction(
        PlayerControlAction.KONG_FROM_DISCARD, deepPovCopy(pov), floatArrayOf(if (result) 1.0f else 0.0f)
      )
    );
    return result
  }

  override fun pongFromDiscard(pov: PlayerPov): Boolean {
    val result = actualPlayerControl.pongFromDiscard(pov)
    recordedActions.add(
      RecordedAction(
        PlayerControlAction.PONG_FROM_DISCARD, deepPovCopy(pov), floatArrayOf(
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
        PlayerControlAction.CHOW_FROM_DISCARD, deepPovCopy(pov), floatArrayOf(
          if (result == null) 1.0f else 0.0f,
          if (result != null && result.numeral == pov.discarded.lastOrNull()?.numeral?.minus(2)) 1.0f else 0.0f,
          if (result != null && result.numeral == pov.discarded.lastOrNull()?.numeral?.minus(1)) 1.0f else 0.0f,
          if (result != null && result.numeral == pov.discarded.lastOrNull()?.numeral) 1.0f else 0.0f,
        )
      )
    );
    return result
  }

  fun deepPovCopy(pov: PlayerPov): PlayerPov {
    return pov.copy(
      prevMelds = pov.prevMelds.map { it.toList() }.toList(),
      nextMelds = pov.nextMelds.map { it.toList() }.toList(),
      oppoMelds = pov.oppoMelds.map { it.toList() }.toList(),
      prevFlowers = pov.prevFlowers.toList(),
      nextFlowers = pov.nextFlowers.toList(),
      oppoFlowers = pov.oppoFlowers.toList(),

      discarded = pov.discarded.toList(),
      hand = pov.hand.toList(),
      melds = pov.melds.map { it.toList() }.toList(),
      flowers = pov.flowers.toList(),
    )
  }
}