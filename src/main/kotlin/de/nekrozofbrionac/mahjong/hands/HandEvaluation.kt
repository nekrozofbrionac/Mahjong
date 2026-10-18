package de.nekrozofbrionac.mahjong.hands

import de.nekrozofbrionac.mahjong.board.TileType
import de.nekrozofbrionac.mahjong.game.Turn
import kotlin.math.min

fun evaluateHand(
  prevWind: Turn,
  playerWind: Turn,
  hand: List<TileType>,
  melds: List<List<TileType>>,
  flowers: List<TileType>,
  extraTileToConsider: TileType?
): Triple<Int, String, List<List<TileType>>> {
  val maxFaan = 13

  val handCopy = hand.toMutableList()
  if (null != extraTileToConsider) {
    handCopy.add(extraTileToConsider)
  }
  handCopy.sort()
  val sortedHand = handCopy.toList();

  // handle special hands

  if (hand.isNotEmpty() && isThirteenOrphans(sortedHand)) {
    return Triple(maxFaan, "13 Orphans", listOf(sortedHand))
  } else if (hand.isNotEmpty() && isNinePorts(sortedHand)) {
    return Triple(maxFaan, "Nine Ports", listOf(sortedHand))
  }

  // "normal" hands consisting of melds and whatnot
  val meldsIncludingEye = determineAverageWin(sortedHand, melds)
    ?: return Triple(-1, "No win", emptyList())

  return evaluateStandardHand(meldsIncludingEye, prevWind, playerWind, flowers, maxFaan)
}

fun evaluateStandardHand(
  meldsIncludingEye: List<List<TileType>>,
  prevWind: Turn,
  playerWind: Turn,
  flowers: List<TileType>,
  maxFaan: Int = 13
): Triple<Int, String, List<List<TileType>>> {
  val eye = meldsIncludingEye.find { it.size == 2 }
    ?: throw UnsupportedOperationException("How do we have an average hand from determineAverageWin with no eye")
  val remainingMelds = meldsIncludingEye.filter { it.size > 2 }

  var winDescription = ""
  var faan = 0;
  var noDragonBonus = false; // no dragon pong points for small/great dragon

  if (meldsIncludingEye.all { isSameSuit(it, null) }) { // all honours
    faan += 7
    winDescription += " All Honours"
  } else if (isDragonEyePongKong(eye) && remainingMelds.count { isDragonEyePongKong(it) } == 2) { // small dragon
    faan += 5
    noDragonBonus = true;
    winDescription += " Small Dragons"
  } else if (remainingMelds.count { isDragonEyePongKong(it) } == 3) { // great dragon
    faan += 8
    noDragonBonus = true;
    winDescription += " Great Dragons"
  } else if (isWindEyePongKong(eye) && remainingMelds.count { isWindEyePongKong(it) } == 3) {// small wind
    faan += 5
    winDescription += " Small Winds"
  } else if (remainingMelds.count { isWindEyePongKong(it) } == 4) { // great wind
    faan += 10 // includes all pongs later
    winDescription += " Great Winds"
  } else if (meldsIncludingEye.all { isTerminalEyePongKong(it) }) { // all Terminals
    faan += 7
    winDescription += " All Orphans"
  }

  // check for all chows
  if (remainingMelds.all { isChow(it) }) {
    faan += 1;
    winDescription += " All Chows"
  }
  // all pongs
  if (remainingMelds.all { isPongOrKong(it) }) {
    faan += 3
    winDescription += " All Pongs"
  }
  // Same Suit (without honour)
  val firstActualSuit = meldsIncludingEye.firstOrNull { it.isNotEmpty() && null != it[0].suit }?.get(0)?.suit
  if (null != firstActualSuit && meldsIncludingEye.all { isSameSuit(it, firstActualSuit) }) {
    faan += 7
    winDescription += " Pure Hand"
  }
  // Mixed one suit
  if (null != firstActualSuit && meldsIncludingEye.all { isSameSuit(it, firstActualSuit) || isSameSuit(it, null) }) {
    faan += 3
    winDescription += " Mixed One Suit"
  }

  // Dragon Bonus
  if (!noDragonBonus) {
    val dragons = remainingMelds.count { isDragonEyePongKong(it) }
    if (dragons > 0) {
      faan += dragons
      winDescription += " $dragons Dragons"
    }
  }

  // Wind Bonus
  if (remainingMelds.any { isWindEyePongKong(it) && it[0] == cardinalDirectionToWindTile(prevWind) }) {
    faan += 1
    winDescription += " Prevailing Wind"
  }
  if (remainingMelds.any { isWindEyePongKong(it) && it[0] == cardinalDirectionToWindTile(playerWind) }) {
    faan += 1
    winDescription += " Player Wind"
  }

  // Flower Bonus
  if (flowers.isEmpty()) {
    faan += 1
    winDescription += " No Flowers/Seasons"
  } else if (flowers.contains(cardinalDirectionToFlower(playerWind))) {
    faan += 1
    winDescription += " Seat Flower"
  } else if (flowers.contains(cardinalDirectionToSeason(playerWind))) {
    faan += 1
    winDescription += " Seat Season"
  } else if (flowers.containsAll(
      listOf(
        TileType.FLOWER_1,
        TileType.FLOWER_2,
        TileType.FLOWER_3,
        TileType.FLOWER_4
      )
    )
  ) {
    faan += 2
    winDescription += " All Flowers"
  } else if (flowers.containsAll(
      listOf(
        TileType.SEASON_1,
        TileType.SEASON_2,
        TileType.SEASON_3,
        TileType.SEASON_4
      )
    )
  ) {
    faan += 2
    winDescription += " All Seasons"
  }

  return Triple(min(faan, maxFaan), winDescription.trim(), meldsIncludingEye);
}


fun determineAverageWin(
  hand: List<TileType>,
  melds: List<List<TileType>>,
  extraTileToConsider: TileType? = null
): List<List<TileType>>? {
  val handCopy = hand.toMutableList()
  if (null != extraTileToConsider) {
    handCopy.add(extraTileToConsider)
  }
  handCopy.sort()
  val sortedHand = handCopy.toList();

  val possibleEyes = sortedHand
    .filter { st -> sortedHand.count { it == st } >= 2 }
    .distinct()

  possibleEyes.forEach { eyeTile ->
    // remove the eye first
    val mutableRestHand = sortedHand.toMutableList()
    val mutableRestMelds: MutableList<List<TileType>> = mutableListOf()
    val chowOrphansToIgnore: MutableList<TileType> = mutableListOf()
    repeat(2) { mutableRestHand.remove(eyeTile) }
    mutableRestMelds.add(listOf(eyeTile, eyeTile))


    // try to find concealed kongs that are a pong and part of a chow
    while (true) {
      val firstPossibleKong = mutableRestHand.firstOrNull() { st -> mutableRestHand.count { st == it } >= 4 }
        ?: break;

      repeat(3) { mutableRestHand.remove(firstPossibleKong) }
      mutableRestMelds.add(listOf(firstPossibleKong, firstPossibleKong, firstPossibleKong))
    }

    // try to find all chows
    while (true) {
      val firstPossibleChow =
        mutableRestHand.firstOrNull() { st -> mutableRestHand.count { st == it } < 3 && st.numeral != null && st.suit != null }
          ?: break;

      val susSecond =
        mutableRestHand.find { it.numeral == firstPossibleChow.numeral?.plus(1) && it.suit == firstPossibleChow.suit }
      val susThird =
        mutableRestHand.find { it.numeral == firstPossibleChow.numeral?.plus(2) && it.suit == firstPossibleChow.suit }

      if (null != susSecond && null != susThird) {
        mutableRestHand.remove(firstPossibleChow)
        mutableRestHand.remove(susSecond)
        mutableRestHand.remove(susThird)
        mutableRestMelds.add(listOf(firstPossibleChow, susSecond, susThird))
      } else {
        mutableRestHand.remove(firstPossibleChow)
        chowOrphansToIgnore.add(firstPossibleChow)
      }
    }

    mutableRestHand.addAll(chowOrphansToIgnore)
    chowOrphansToIgnore.removeAll { true };
    mutableRestHand.sort()

    // try to find all pongs
    while (true) {
      val firstPossiblePong =
        mutableRestHand.firstOrNull() { st -> mutableRestHand.count { st == it } >= 3 }
          ?: break;
      repeat(3) { mutableRestHand.remove(firstPossiblePong) }
      mutableRestMelds.add(listOf(firstPossiblePong, firstPossiblePong, firstPossiblePong))
    }

    if (mutableRestHand.isEmpty()) {
      mutableRestMelds.addAll(melds)
      return mutableRestMelds;
    }
  }

  return null;
}

fun isChow(meld: List<TileType>): Boolean {
  return meld.size == 3 &&
      meld[0].suit != null && meld[1].suit != null && meld[2].suit != null &&
      meld[0].numeral != null && meld[1].numeral != null && meld[2].numeral != null &&
      meld[0].suit == meld[1].suit && meld[1].suit == meld[2].suit &&
      meld[0].numeral!! + 1 == meld[1].numeral &&
      meld[1].numeral!! + 1 == meld[2].numeral
}

fun isPongOrKong(meld: List<TileType>): Boolean {
  return (meld.size == 3 || meld.size == 4) && meld.all { meld[0] == it }
}

fun isSameSuit(meld: List<TileType>, suit: String?): Boolean {
  return meld.all { it.suit == suit }
}

fun isDragonEyePongKong(meld: List<TileType>): Boolean {
  return (meld[0] == TileType.DRAGON_RED || meld[0] == TileType.DRAGON_GREEN || meld[0] == TileType.DRAGON_WHITE) &&
      meld.all { meld[0] == it }
}

fun isWindEyePongKong(meld: List<TileType>): Boolean {
  return (meld[0] == TileType.WIND_EAST || meld[0] == TileType.WIND_SOUTH || meld[0] == TileType.WIND_WEST || meld[0] == TileType.WIND_NORTH) &&
      meld.all { meld[0] == it }
}

fun cardinalDirectionToWindTile(cardinalDirection: Turn): TileType {
  return when (cardinalDirection) {
    Turn.EAST -> TileType.WIND_EAST
    Turn.SOUTH -> TileType.WIND_SOUTH
    Turn.WEST -> TileType.WIND_WEST
    Turn.NORTH -> TileType.WIND_NORTH
  }
}

fun cardinalDirectionToFlower(cardinalDirection: Turn): TileType {
  return when (cardinalDirection) {
    Turn.EAST -> TileType.FLOWER_1
    Turn.SOUTH -> TileType.FLOWER_2
    Turn.WEST -> TileType.FLOWER_3
    Turn.NORTH -> TileType.FLOWER_4
  }
}

fun cardinalDirectionToSeason(cardinalDirection: Turn): TileType {
  return when (cardinalDirection) {
    Turn.EAST -> TileType.SEASON_1
    Turn.SOUTH -> TileType.SEASON_2
    Turn.WEST -> TileType.SEASON_3
    Turn.NORTH -> TileType.SEASON_4
  }
}

fun isTerminalEyePongKong(meld: List<TileType>): Boolean {
  return meld.isNotEmpty() &&
      meld[0].suit != null && (meld[0].numeral == 1 || meld[0].numeral == 9) &&
      meld.all { meld[0] == it }
}

val THIRTEEN_ORPHANS = listOf(
  TileType.BAMBOO_1,
  TileType.BAMBOO_9,
  TileType.STONE_1,
  TileType.STONE_9,
  TileType.CHARACTER_1,
  TileType.CHARACTER_9,
  TileType.WIND_EAST,
  TileType.WIND_SOUTH,
  TileType.WIND_WEST,
  TileType.WIND_NORTH,
  TileType.DRAGON_RED,
  TileType.DRAGON_GREEN,
  TileType.DRAGON_WHITE,
)
fun isThirteenOrphans(hand: List<TileType>): Boolean {
  return hand.containsAll(THIRTEEN_ORPHANS) && hand.all { THIRTEEN_ORPHANS.contains(it) }
}

fun isNinePorts(hand: List<TileType>): Boolean {
  return hand[0].suit != null &&
      hand.all { isSameSuit(hand, hand[0].suit) } &&
      (hand.count { it.numeral == 1 } >= 3) &&
      (hand.count { it.numeral == 2 } >= 1) &&
      (hand.count { it.numeral == 3 } >= 1) &&
      (hand.count { it.numeral == 4 } >= 1) &&
      (hand.count { it.numeral == 5 } >= 1) &&
      (hand.count { it.numeral == 6 } >= 1) &&
      (hand.count { it.numeral == 7 } >= 1) &&
      (hand.count { it.numeral == 8 } >= 1) &&
      (hand.count { it.numeral == 9 } >= 3)
}

fun generateNinePorts(tile: TileType): List<TileType> {
  val suit = tile.suit ?: return emptyList()

  return listOf(
    TileType.entries.find { it.suit == suit && it.numeral == 1 }!!,
    TileType.entries.find { it.suit == suit && it.numeral == 1 }!!,
    TileType.entries.find { it.suit == suit && it.numeral == 1 }!!,
    TileType.entries.find { it.suit == suit && it.numeral == 2 }!!,
    TileType.entries.find { it.suit == suit && it.numeral == 3 }!!,
    TileType.entries.find { it.suit == suit && it.numeral == 4 }!!,
    TileType.entries.find { it.suit == suit && it.numeral == 5 }!!,
    TileType.entries.find { it.suit == suit && it.numeral == 6 }!!,
    TileType.entries.find { it.suit == suit && it.numeral == 7 }!!,
    TileType.entries.find { it.suit == suit && it.numeral == 8 }!!,
    TileType.entries.find { it.suit == suit && it.numeral == 9 }!!,
    TileType.entries.find { it.suit == suit && it.numeral == 9 }!!,
    TileType.entries.find { it.suit == suit && it.numeral == 9 }!!,
  )
}
