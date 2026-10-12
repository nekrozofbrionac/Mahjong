package de.nekrozofbrionac.mahjong.player.control.bot.ml

import de.nekrozofbrionac.mahjong.board.TileType
import de.nekrozofbrionac.mahjong.determineChowPossibilities
import de.nekrozofbrionac.mahjong.game.PlayerPov
import de.nekrozofbrionac.mahjong.game.Turn

/*
  Action is a matter of the whoever wants this view

  | Value             | Encoding       |
  |-------------------|----------------|
  | minFaan           | THERM 0-8      |
  | prevailingWind    | Turn (HOE)     |
  | playerSeat        | Turn (HOE)     |
  | prevSeat          | Turn (HOE)     |
  | nextSeat          | Turn (HOE)     |
  | oppoSeat          | Turn (HOE)     |
  | currentTurnPlayer | Turn (HOE)     |

  OTHER IDEA: zones per tile type:
  tiletype and then THERM whether:
  prevMeld 4*4
  nextmeld 4*4
  oppomeld 4*4
  prevFlower 1
  nextFlower 1
  oppoFlower 1
  discarded 4
  hand 4
  melds 4*4
  flowers 1

  turn and wall-remaining are encoded via tiles that are visible to the player.

 */
private val minFaanIndex = 0
private val prevailingWindIndex = minFaanIndex + 9
private val playerSeatIndex = prevailingWindIndex + Turn.entries.size
private val prevSeatIndex = playerSeatIndex + Turn.entries.size
private val nextSeatIndex = prevSeatIndex + Turn.entries.size
private val oppoSeatIndex = nextSeatIndex + Turn.entries.size
private val currentTurnPlayerIndex = oppoSeatIndex + Turn.entries.size
private val firstTileIndex = currentTurnPlayerIndex + Turn.entries.size

private const val zoneSize = 16 + 16 + 16 + 1 + 1 + 1 + 4 + 4 + 16 + 1
val POV_TO_INPUT_SIZE = firstTileIndex + TileType.entries.size * zoneSize

fun povToInputLayer(pov: PlayerPov): FloatArray {
  if (pov.minFaan !in 0..8) {
    throw AssertionError("minFaan must be between 0 and 8, but was ${pov.minFaan}")
  }

  val output = FloatArray(POV_TO_INPUT_SIZE)
  for (i in 0..pov.minFaan) {
    output[minFaanIndex + i + 1] = 1.0f
  }
  output[prevailingWindIndex + pov.prevailingWind.ordinal] = 1.0f
  output[playerSeatIndex + pov.playerSeat.ordinal] = 1.0f
  output[prevSeatIndex + pov.prevSeat.ordinal] = 1.0f
  output[nextSeatIndex + pov.nextSeat.ordinal] = 1.0f
  output[oppoSeatIndex + pov.oppoSeat.ordinal] = 1.0f
  output[currentTurnPlayerIndex + pov.currentTurnPlayer.ordinal] = 1.0f

  TileType.entries.forEachIndexed { i, tileToEncode ->
    val indexWithinZone = firstTileIndex + (i * zoneSize)
    pov.prevMelds.forEachIndexed { pmi, m ->
      repeat(m.count { tileToEncode == it }) { indexInMeld -> output[indexWithinZone + pmi * 4 + indexInMeld] = 1.0f }
    }
    pov.nextMelds.forEachIndexed { nmi, m ->
      repeat(m.count { tileToEncode == it }) { indexInMeld ->
        output[indexWithinZone + 4 * 4 + nmi * 4 + indexInMeld] = 1.0f
      }
    }
    pov.oppoMelds.forEachIndexed { omi, m ->
      repeat(m.count { tileToEncode == it }) { indexInMeld ->
        output[indexWithinZone + 4 * 4 + 4 * 4 + omi * 4 + indexInMeld] = 1.0f
      }
    }
    if (pov.prevFlowers.contains(tileToEncode)) output[indexWithinZone + 16 * 3 + 0] = 1.0f
    if (pov.nextFlowers.contains(tileToEncode)) output[indexWithinZone + 16 * 3 + 1] = 1.0f
    if (pov.oppoFlowers.contains(tileToEncode)) output[indexWithinZone + 16 * 3 + 2] = 1.0f
    repeat(pov.discarded.count { tileToEncode == it }) { indexInDiscarded ->
      output[indexWithinZone + 16 * 3 + 3 + indexInDiscarded] = 1.0f
    }
    repeat(pov.hand.count { tileToEncode == it }) { indexInDiscarded ->
      output[indexWithinZone + 16 * 3 + 3 + 4] = 1.0f
    }
    pov.melds.forEachIndexed { mi, m ->
      repeat(m.count { tileToEncode == it }) { indexInMeld ->
        output[indexWithinZone + 16 * 3 + 3 + 4 * 2 + mi * 4 + indexInMeld] = 1.0f
      }
    }
    if (pov.flowers.contains(tileToEncode)) output[indexWithinZone + 16 * 3 + 3 + 4 * 2 + 16] = 1.0f
  }

  return output
}

fun generateOutputMask(pov: PlayerPov, action: PlayerControlAction): FloatArray {
  val outputSize = action.outputSize
  val outputMask = FloatArray(outputSize) { 0.0f }

  when (action) {
    PlayerControlAction.WIN,
    PlayerControlAction.KONG_FROM_DRAW,
    PlayerControlAction.KONG_FROM_DISCARD,
    PlayerControlAction.PONG_FROM_DISCARD -> {
      outputMask[0] = 1.0f
    }

    PlayerControlAction.CHOOSE_DISCARD -> {
      // println("Generating output mask for CHOOSE_DISCARD with hand: ${formatTiles(pov.hand, 14, "")}")
      pov.hand.distinct().forEach { tile ->
        outputMask[tile.ordinal] = 1.0f
      }
    }

    PlayerControlAction.CHOW_FROM_DISCARD -> {
      outputMask[0] = 1.0f // Option to not chow
      val discardedTile = pov.discarded.lastOrNull()
        ?: throw IllegalStateException("No discarded tile found, but game suggested to chow.")

      val possibleChows = determineChowPossibilities(pov.hand, discardedTile)

      if (possibleChows.isEmpty()) {
        throw IllegalStateException("No possible chows found, but game suggested to chow.")
      }
      possibleChows.forEach { chow ->
        when (chow[0].numeral) {
          // score for chow starting with the tile two below the discarded tile
          discardedTile.numeral?.minus(2) -> outputMask[1] = 1.0f
          // score for chow starting with the tile one below the discarded tile
          discardedTile.numeral?.minus(1) -> outputMask[2] = 1.0f
          // score for chow starting with the discarded tile itself
          discardedTile.numeral -> outputMask[3] = 1.0f
        }
      }
    }
  }

  return outputMask
}