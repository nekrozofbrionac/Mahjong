package de.nekrozofbrionac.mahjong.player.control.bot

import de.nekrozofbrionac.mahjong.board.SimpleBoard
import de.nekrozofbrionac.mahjong.board.TileType
import de.nekrozofbrionac.mahjong.combinations
import de.nekrozofbrionac.mahjong.determineHiddenTiles
import de.nekrozofbrionac.mahjong.formatTiles
import de.nekrozofbrionac.mahjong.game.PlayerPov
import de.nekrozofbrionac.mahjong.game.Turn
import de.nekrozofbrionac.mahjong.hands.*
import de.nekrozofbrionac.mahjong.player.control.PlayerControl
import de.nekrozofbrionac.mahjong.printPov
import kotlin.Double.Companion.NaN
import kotlin.math.min
import kotlin.math.pow

val mahjongSet = SimpleBoard().wall.filter { !it.isFlower() }.toList()

class NoIdeaAboutHandsBot(
  val iterations: Int = 10000,
  val verbose: Boolean = true,
) : PlayerControl {

  override fun win(pov: PlayerPov): Boolean {
    return true;
  }

  override fun chooseDiscard(pov: PlayerPov): TileType {
    if (verbose) {
      printPov(pov)
    }

    say("I have to discard...")
    val searchOutcome = search(iterations, pov, pov.minFaan)
    say("Thought for ${searchOutcome.actualIterations} and came up with:")
    searchOutcome.tileProbabilities.forEachIndexed { i, tp ->
      say("${i.toString().padStart(2)}. : ${formatTiles(listOf(tp.first), 1, "")} ${tp.second}")
    }
    say("Winnable simulation runs: ${searchOutcome.predictedWins} of ${searchOutcome.actualIterations}")
    say("Average tiles drawn: ${searchOutcome.averageDepth} Tiles ")
    say("Average number of tiles mse: ${searchOutcome.meanSquareError} ")
    say("Median tiles drawn: ${searchOutcome.medianDepth} Tiles ")
    say("I will discard ${formatTiles(listOf(searchOutcome.tileProbabilities[0].first), 1, "")}.")
    return searchOutcome.tileProbabilities[0].first
  }

  override fun kongFromDraw(pov: PlayerPov): Boolean {
    return true
  }

  override fun kongFromDiscard(pov: PlayerPov): Boolean {
    return true
  }

  override fun pongFromDiscard(pov: PlayerPov): Boolean {
    val pongedTile = pov.discarded.last()
    val formattedPong = formatTiles(listOf(pongedTile, pongedTile, pongedTile), 3, "")

    if (verbose) {
      printPov(pov)
    }
    say("Pong with $formattedPong ?")
    say("Thinking about not ponging...")
    val searchNoPong = search(iterations, pov, pov.minFaan)
    say("... ${searchNoPong.predictedWins} of ${searchNoPong.actualIterations} winnable futures. Avg depth: ${searchNoPong.averageDepth} (-1).")

    val copyOfHand = pov.hand.toMutableList()
    val copyOfMelds = pov.melds.toMutableList()

    copyOfHand.add(pongedTile)
    repeat(3) { copyOfHand.remove(pongedTile) }
    copyOfMelds.add(listOf(pongedTile, pongedTile, pongedTile))
    val povAfterPong = pov.copy(
      hand = copyOfHand,
      melds = copyOfMelds,
      discarded = pov.discarded.dropLast(1),
    )

    say("Thinking about $formattedPong...")
    val searchWithPong = search(iterations, povAfterPong, pov.minFaan)
    say("... ${searchWithPong.predictedWins} of ${searchWithPong.actualIterations} winnable futures. Avg depth: ${searchWithPong.averageDepth}.")

    // -1 because of needing one tile more for 14
    val noPongTerm = pathCost(
      searchNoPong.predictedWins.toDouble() / searchNoPong.actualIterations.toDouble(),
      searchNoPong.averageDepth - 1
    )
    val withPongTerm = pathCost(
      searchWithPong.predictedWins.toDouble() / searchWithPong.actualIterations.toDouble(),
      searchWithPong.averageDepth
    )

    val output = if (withPongTerm.isNaN() && noPongTerm.isNaN()) {
      true
    } else if (withPongTerm.isNaN()) {
      false
    } else if (noPongTerm.isNaN()) {
      true
    } else {
      withPongTerm <= noPongTerm
    }
    say("I will go with ${if (output) formattedPong else "no pong"}.")
    say("PathCosts: (No Pong: $noPongTerm), ($formattedPong : $withPongTerm).")
    return output
  }

  override fun chowFromDiscard(pov: PlayerPov): TileType? {
    if (verbose) {
      printPov(pov)
    }
    val detectedChows = determineChowPossibilities(pov.hand, pov.discarded.last());
    val formattedChows = detectedChows.associateWith { formatTiles(it, 3, "") }
    val costMap = mutableMapOf<List<TileType>?, Double>();

    say("Chow with ${formattedChows.values.joinToString(", ")} ?")
    // todo probably not perfect, i am forgetting kongs, but so did i within search
    say("Thinking about not chowing...")
    val searchNoChow = search(iterations, pov, pov.minFaan) // keep in mind that this starts with 13 tiles instead of 14
    say("... ${searchNoChow.predictedWins} of ${searchNoChow.actualIterations} winnable futures. Avg depth: ${searchNoChow.averageDepth} (-1).")

    costMap[null] =
      pathCost(searchNoChow.predictedWins.toDouble() / searchNoChow.actualIterations, searchNoChow.averageDepth - 1)

    detectedChows.forEach { chow ->

      val copyOfHand = pov.hand.toMutableList()
      val copyOfMelds = pov.melds.toMutableList()
      copyOfHand.add(pov.discarded.last())
      copyOfHand.remove(chow[0])
      copyOfHand.remove(chow[1])
      copyOfHand.remove(chow[2])
      copyOfMelds.add(mutableListOf(chow[0], chow[1], chow[2]))
      val hypotheticalPovAfterChow = pov.copy(
        hand = copyOfHand,
        melds = copyOfMelds,
        discarded = pov.discarded.dropLast(1)
      )
      say("Thinking about ${formattedChows[chow]} ...")
      val r = search(iterations, hypotheticalPovAfterChow, pov.minFaan)
      costMap[chow] = pathCost(r.predictedWins.toDouble() / r.actualIterations.toDouble(), r.averageDepth)
      say("... ${r.predictedWins} of ${r.actualIterations} winnable futures. Avg depth: ${r.averageDepth}.")
    }

    // chose the smallest average?
    // probably smaller mse if too similar?

    val pathCosts = costMap.toList().sortedBy { it.second }
    val tileToPrint = if (null == pathCosts[0].first) "no chow" else formattedChows[pathCosts[0].first]

    say("I will go with ${tileToPrint}.")
    say("PathCosts: ${pathCosts.joinToString(", ") { "(${it.first}, cost: ${it.second})" }}")
    return pathCosts[0].first?.get(0);
  }

  private fun say(thing: String) {
    if (verbose) {
      println(thing)
    }
  }
}

data class SearchOutcome(
  val winnable: Boolean,
  val tileProbabilities: List<Pair<TileType, Double>>,
  val actualIterations: Int,
  val predictedWins: Int,
  val averageDepth: Double,
  val medianDepth: Int,
  val meanSquareError: Double,
)

fun search(iterations: Int, initialState: PlayerPov, minFaan: Int): SearchOutcome {
  val tilesToDraw = determineHiddenTiles(initialState);
  val futures = mutableListOf<Pair<List<List<TileType>>, List<TileType>>>()
  val tally: MutableMap<TileType, Double> = mutableMapOf<TileType, Double>()

  val winPossibility = findStandardWin(
    minimumFaanRequired = minFaan,
    hand = initialState.hand + tilesToDraw,
    fixedMelds = initialState.melds,
    playerWind = initialState.playerSeat,
    prevailingWind = initialState.prevailingWind
  )
  if (winPossibility == null) {
    return SearchOutcome(
      winnable = false,
      tileProbabilities = initialState.hand.toSet().shuffled().map { Pair(it, NaN) },
      actualIterations = 0,
      predictedWins = 0,
      averageDepth = 0.0,
      medianDepth = 0,
      meanSquareError = 0.0
    )
  }

  // that means it IS possible to win!
  // loop
  // draw tiles until win, what tiles not to use
  // discard what tile was most unused

  // instead of for loop, maybe a while until iterations futures has been found?

  val maxTilesToDraw = tilesToDraw.size / 4
  val numberOfPermutations = numberOfPermutations(tilesToDraw.size, maxTilesToDraw)
  val upperBound = min(iterations.toLong(), numberOfPermutations)

  val seqGen = RandomTileSequenceGenerator(
    tilesToDraw,
    maxTilesToDraw,
  )

  var runCounter = 0;
  while (
    runCounter < upperBound
  ) { // ok, for i in again
    runCounter++;
    val hypotheticalRestToDraw = seqGen.generateRandomSequence()

    val hypotheticalWinPossibility = findStandardWin(
      minimumFaanRequired = minFaan,
      hand = initialState.hand + hypotheticalRestToDraw,
      fixedMelds = initialState.melds,
      playerWind = initialState.playerSeat,
      prevailingWind = initialState.prevailingWind,
    )
    if (null == hypotheticalWinPossibility) {
      continue;
    }

    for (j in 1..hypotheticalRestToDraw.size) {
      val hypotheticalSuperHand = initialState.hand + hypotheticalRestToDraw.subList(0, j)
      // win detected
      val result = findStandardWin(
        minimumFaanRequired = minFaan,
        hand = hypotheticalSuperHand,
        fixedMelds = initialState.melds,
        playerWind = initialState.playerSeat,
        prevailingWind = initialState.prevailingWind,
      )
      if (null != result) {
        futures.add(Pair(result.first, result.second))
        break;
      }
    }
  }

  if (futures.isEmpty()) {
    return SearchOutcome(
      winnable = true,
      tileProbabilities = initialState.hand.shuffled().toSet().map { Pair(it, NaN) },
      actualIterations = runCounter,
      predictedWins = futures.size,
      averageDepth = NaN,
      medianDepth = NaN.toInt(),
      meanSquareError = NaN
    )
  }

  futures.sortBy { it.second.size }
  val minRestHandSize: Int = futures.minOfOrNull { it.second.size } ?: 0

  futures.forEach { f ->
    f.first.flatten().forEach { tally[it] = (tally[it] ?: 0.0) - 2.0.pow(minRestHandSize - f.second.size) }
  }

  val uselessScore = initialState.hand.toSet().map { Pair(it, (tally[it] ?: 0.0)) }.sortedByDescending { it.second }

  val averageDepth = futures.map { it.second.size }.average()
  val medianDepth = futures.map { it.second.size }.sorted().getOrNull(futures.size / 2) ?: -1
  val meanSquareError = futures.map { it.second.size }.map { val diff = it - averageDepth; diff * diff }.average()

  return SearchOutcome(
    winnable = true,
    tileProbabilities = uselessScore,
    actualIterations = runCounter,
    predictedWins = futures.size,
    averageDepth = averageDepth,
    medianDepth = medianDepth,
    meanSquareError = meanSquareError
  )
}

fun findStandardWin(
  minimumFaanRequired: Int = 0,
  hand: List<TileType>, // more than 14 tiles possible
  fixedMelds: List<List<TileType>>,
  playerWind: Turn,
  prevailingWind: Turn,
): Pair<List<List<TileType>>, List<TileType>>? {
  val sortedHand = hand.sorted();
  val possibleEyes = sortedHand.toSet().filter { st -> sortedHand.count { it == st } >= 2 }

  possibleEyes.forEach { eyeTile ->
    // remove the eye first
    val mutableRestHand = sortedHand.toMutableList()
    val mutableRestMelds: MutableList<List<TileType>> = mutableListOf()
    repeat(2) { mutableRestHand.remove(eyeTile) }
    mutableRestMelds.add(listOf(eyeTile, eyeTile))

    // try to find concealed kongs that are a pong and part of a chow
    while (true) {
      val firstPossibleKong = mutableRestHand.firstOrNull() { st -> mutableRestHand.count { st == it } >= 4 }
        ?: break;

      repeat(3) { mutableRestHand.remove(firstPossibleKong) }
      mutableRestMelds.add(listOf(firstPossibleKong, firstPossibleKong, firstPossibleKong))
    }

    // since we are working with more tiles than a hand, lets count both chows first and pong first
    val mutableChowFirstMelds = mutableRestMelds.toMutableList()
    val mutableChowFirstRestHand = mutableRestHand.toMutableList()
    putAllChowsIntoMelds(mutableChowFirstMelds, mutableChowFirstRestHand)
    putAllPongsIntoMelds(mutableChowFirstMelds, mutableChowFirstRestHand)
    val chowResult = evaluateMelds(
      eyeTile,
      mutableChowFirstMelds,
      fixedMelds,
      mutableChowFirstRestHand,
      prevailingWind,
      playerWind,
      minimumFaanRequired
    )
    if (chowResult != null) {
      return chowResult
    }

    val mutablePongFirstMelds = mutableRestMelds.toMutableList()
    val mutablePongFirstRestHand = mutableRestHand.toMutableList()
    putAllPongsIntoMelds(mutablePongFirstMelds, mutablePongFirstRestHand)
    putAllChowsIntoMelds(mutablePongFirstMelds, mutablePongFirstRestHand)
    val pongResult = evaluateMelds(
      eyeTile,
      mutablePongFirstMelds,
      fixedMelds,
      mutablePongFirstRestHand,
      prevailingWind,
      playerWind,
      minimumFaanRequired
    )
    if (pongResult != null) {
      return pongResult
    }
  }
  return null;
}

fun evaluateMelds(
  eyeTile: TileType,
  mutableRestMelds: MutableList<List<TileType>>,
  fixedMelds: List<List<TileType>>,
  mutableRestHand: MutableList<TileType>,
  prevailingWind: Turn,
  playerWind: Turn,
  minimumFaanRequired: Int,
): Pair<List<List<TileType>>, List<TileType>>? {
  if (mutableRestMelds.size + fixedMelds.size >= 5) { // 1 eye and maybe more than 4 melds, we need to get to more FAAN!
    // there is only one eye
    val eyelessMelds = mutableRestMelds.filter { it.size > 2 }.toMutableList();
    eyelessMelds.combinations(4 - (fixedMelds.size))
      .forEach { meldCombination ->
        val recombinedEyeMelds = meldCombination.toMutableList()
        recombinedEyeMelds.add(listOf(eyeTile, eyeTile))
        recombinedEyeMelds.addAll(fixedMelds)

        val result = fastEvaluateStdHand(
          meldsIncludingEye = recombinedEyeMelds,
          prevWind = prevailingWind,
          playerWind = playerWind,
        )

        if (result >= minimumFaanRequired) {
          meldCombination.forEach { m -> mutableRestMelds.remove(m) }
          mutableRestMelds.remove(listOf(eyeTile, eyeTile))

          mutableRestHand.addAll(mutableRestMelds.flatten())
          return Pair(recombinedEyeMelds, mutableRestHand)
        }
      }
  }
  return null;
}

/**
 * An assertion is that mutableRestHands is sorted!
 */
fun putAllChowsIntoMelds(
  mutableRestMelds: MutableList<List<TileType>>,
  mutableRestHand: MutableList<TileType>
) {
  val chowOrphansToIgnore = mutableListOf<TileType>()
  // try to find all chows
  while (true) {
    val susFirst =
      mutableRestHand.firstOrNull() { st -> mutableRestHand.count { st == it } < 3 && st.numeral != null && st.suit != null }
        ?: break;

    val susSecond = mutableRestHand.find { it.numeral == susFirst.numeral!! + 1 && it.suit == susFirst.suit }
    val susThird = mutableRestHand.find { it.numeral == susFirst.numeral!! + 2 && it.suit == susFirst.suit }

    if (null != susSecond && null != susThird) {
      mutableRestHand.remove(susFirst)
      mutableRestHand.remove(susSecond)
      mutableRestHand.remove(susThird)
      mutableRestMelds.add(listOf(susFirst, susSecond, susThird))
    } else {
      mutableRestHand.remove(susFirst)
      chowOrphansToIgnore.add(susFirst)
    }
  }

  mutableRestHand.addAll(chowOrphansToIgnore)
  // done.
}

fun putAllPongsIntoMelds(
  mutableRestMelds: MutableList<List<TileType>>,
  mutableRestHand: MutableList<TileType>
) {
  // try to find all pongs
  while (true) {
    val firstPossiblePong =
      mutableRestHand.firstOrNull() { st -> mutableRestHand.count { st == it } >= 3 }
        ?: break;
    mutableRestHand.remove(firstPossiblePong)
    mutableRestHand.remove(firstPossiblePong)
    mutableRestHand.remove(firstPossiblePong)
    mutableRestMelds.add(listOf(firstPossiblePong, firstPossiblePong, firstPossiblePong))
  }
}

fun pathCost(probability: Double, turns: Double): Double {
  return (probability * (turns)) + ((1.0 - probability) * Integer.MAX_VALUE)
}

fun numberOfPermutations(n: Int, k: Int): Long {
  // require(n >= 0 && k >= 0 && k <= n) { "Require 0 ≤ k ≤ n" }
  var result = 1L
  for (i in 0 until k) {
    val hm = result * (n - i)
    if (hm < 0) {
      return Long.MAX_VALUE
    }
    result = hm
  }
  return result
}

class RandomTileSequenceGenerator(
  private val tilesToDrawFrom: List<TileType>,
  private val maxLength: Int,
) {
  private val takenPrefixes: MutableMap<List<TileType>, MutableList<TileType>> = mutableMapOf()

  init {
    repeat(maxLength) {
    }
    takenPrefixes[emptyList()] = tilesToDrawFrom.toMutableList()
  }

  fun generateRandomSequence(): List<TileType> {
    val remainingTiles = tilesToDrawFrom.toMutableList()
    val currentPrefix = mutableListOf<TileType>()

    while (currentPrefix.size < maxLength) {
      val pickableTiles = takenPrefixes[currentPrefix]

      if (null == pickableTiles) {
        // build randomSequence and check everything in
        val output = currentPrefix + remainingTiles.shuffled().subList(0, maxLength - currentPrefix.size)

        for (i in 1..output.size) {
          val set = takenPrefixes[output.subList(0, i)]
          if (set == null) {
            val actualRemainingTiles = tilesToDrawFrom.toMutableList()
            output.subList(i, output.size).forEach { actualRemainingTiles.remove(it) }
            val startingList = mutableListOf<TileType>()
            actualRemainingTiles.forEach { startingList.add(it) }
            takenPrefixes[output.subList(0, i)] = startingList
          } else {
            if (i < output.size) { // no other checks necessary, since we should be at the end, and thus its 0
              set.remove(output[i])
            }
          }
        }
        return output
      }

      val nextTile = if (pickableTiles.isNotEmpty()) {
        pickableTiles.random()
      } else {
        remainingTiles.random()
      }
      currentPrefix.add(nextTile)
      remainingTiles.remove(nextTile)
    }
    return currentPrefix
  }
}

fun fastEvaluateStdHand(
  meldsIncludingEye: List<List<TileType>>,
  prevWind: Turn,
  playerWind: Turn,
): Int {
  val eye = meldsIncludingEye.find { it.size == 2 }
    ?: throw UnsupportedOperationException("How do we have an average hand from determineAverageWin with no eye")
  val remainingMelds = meldsIncludingEye.filter { it.size > 2 }

  var faan = 0;
  var noDragonBonus = false; // no dragon pong points for small/great dragon

  if (meldsIncludingEye.all { isSameSuit(it, null) }) { // all honours
    faan += 7
  } else if (isDragonEyePongKong(eye) && remainingMelds.count { isDragonEyePongKong(it) } == 2) { // small dragon
    faan += 5
    noDragonBonus = true;
  } else if (remainingMelds.count { isDragonEyePongKong(it) } == 3) { // great dragon
    faan += 8
    noDragonBonus = true;
  } else if (isWindEyePongKong(eye) && remainingMelds.count { isWindEyePongKong(it) } == 3) {// small wind
    faan += 5
  } else if (remainingMelds.count { isWindEyePongKong(it) } == 4) { // great wind
    faan += 10 // includes all pongs later
  } else if (meldsIncludingEye.all { isTerminalEyePongKong(it) }) { // all Terminals
    faan += 7
  }

  // check for all chows
  if (remainingMelds.all { isChow(it) }) {
    faan += 1;
  }
  // all pongs
  if (remainingMelds.all { isPongOrKong(it) }) {
    faan += 3
  }
  // Same Suit (without honour)
  val firstActualSuit = meldsIncludingEye.firstOrNull { it.isNotEmpty() && null != it[0].suit }?.get(0)?.suit
  if (null != firstActualSuit && meldsIncludingEye.all { isSameSuit(it, firstActualSuit) }) {
    faan += 7
  }
  // Mixed one suit
  if (null != firstActualSuit && meldsIncludingEye.all { isSameSuit(it, firstActualSuit) || isSameSuit(it, null) }) {
    faan += 3
  }

  // Dragon Bonus
  if (!noDragonBonus) {
    val dragons = remainingMelds.count { isDragonEyePongKong(it) }
    if (dragons > 0) {
      faan += dragons
    }
  }

  // Wind Bonus
  if (remainingMelds.any { isWindEyePongKong(it) && it[0] == cardinalDirectionToWindTile(prevWind) }) {
    faan += 1
  }
  if (remainingMelds.any { isWindEyePongKong(it) && it[0] == cardinalDirectionToWindTile(playerWind) }) {
    faan += 1
  }

  return faan
}
