package de.nekrozofbrionac.mahjong.player.control.bot

import de.nekrozofbrionac.mahjong.board.SimpleBoard
import de.nekrozofbrionac.mahjong.board.TileType
import de.nekrozofbrionac.mahjong.formatTiles
import de.nekrozofbrionac.mahjong.game.PlayerPov
import de.nekrozofbrionac.mahjong.game.Turn
import de.nekrozofbrionac.mahjong.hands.*
import de.nekrozofbrionac.mahjong.player.control.PlayerControl
import de.nekrozofbrionac.mahjong.printPov
import kotlin.Double.Companion.NaN
import kotlin.math.abs
import kotlin.math.min
import kotlin.math.pow

val mahjongSet = SimpleBoard().wall.filter { !it.isFlower() }.toList()

class NoIdeaAboutHandsBot(
  val iterations: Int = 10000,
  val minFaan: Int = 3,
) : PlayerControl {

  override fun win(pov: PlayerPov): Boolean {
    return true;
  }

  override fun chooseDiscard(pov: PlayerPov): TileType {
    printPov(pov)
    println("I have to discard...")
    println("Thinking up to $iterations win ways to win...")
    val searchOutcome = search(iterations, pov, minFaan)
    printSearchResults(searchOutcome)
    println("I will discard ${formatTiles(listOf(searchOutcome.tileProbabilities[0].first), 1, "")}.")
    return searchOutcome.tileProbabilities[0].first
  }

  override fun kongFromDraw(pov: PlayerPov): Boolean {
    println("KONG! from draw")
    return true
  }

  override fun kongFromDiscard(pov: PlayerPov): Boolean {
    println("KONG! from discard")
    return true
  }

  override fun pongFromDiscard(pov: PlayerPov): Boolean {
    printPov(pov)

    val pongedTile = pov.discarded.last()
    val formattedPong = formatTiles(listOf(pongedTile, pongedTile, pongedTile), 3, "")
    println("I can pong with $formattedPong !")

    println("Thinking about not ponging up to $iterations future wins...")
    val searchNoPong = search(iterations, pov, minFaan)

    println("After thinking about not ponging I came up with:")
    printSearchResults(searchNoPong)
    println("Keep in mind i started with 13 tiles when searching, not 14, so average depth is 1 larger than it should")

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
    println("Thinking about ponging with $formattedPong up to $iterations wins...")
    val searchWithPong = search(iterations, povAfterPong, minFaan)
    println("After thinking about pong with $formattedPong i came up with:")
    printSearchResults(searchWithPong)

    // -1 because of needing one tile more for 14
    val noPongTerm = pathCost(
      searchNoPong.predictedWins.toDouble() / searchNoPong.actualIterations.toDouble(),
      searchNoPong.averageDepth - 1
    )
    val withPongTerm = pathCost(
      searchWithPong.predictedWins.toDouble() / searchNoPong.actualIterations.toDouble(),
      searchWithPong.averageDepth
    )

    val output = withPongTerm <= noPongTerm
    println("After thinking about it, I will go with ${if (output) formattedPong else "not ponging"} ($noPongTerm no Pong, $withPongTerm with Pong).")
    return output
  }

  override fun chowFromDiscard(pov: PlayerPov): TileType? {
    printPov(pov)
    val detectedChows = determineChowPossibilities(pov.hand, pov.discarded.last());
    val formattedChows = detectedChows.associateWith { formatTiles(it, 3, "") }
    val costMap = mutableMapOf<List<TileType>?, Double>();

    println("I can chow with ${formattedChows.values.joinToString(", ")} !")
    println("Thinking about not chowing up to $iterations wins.")
    // todo probably not perfect, i am forgetting kongs, but so did i within search
    val searchNoChow = search(iterations, pov, minFaan) // keep in mind that this starts with 13 tiles instead of 14
    println("After thinking about not chowing i came up with:")
    printSearchResults(searchNoChow)

    println("Keep in mind that i thought about it using 1 tile less, so the amount of tiles i needed to ’draw' in my head is greater")
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
      println("Thinking about chowing with ${formattedChows[chow]} for up to $iterations wins.")
      val r = search(iterations, hypotheticalPovAfterChow, minFaan)
      costMap[chow] = pathCost(r.predictedWins.toDouble() / r.actualIterations.toDouble(), r.averageDepth)
      println("After thinking about chowing with ${formattedChows[chow]} i came up with:")
      printSearchResults(r)
    }

    // chose the smallest average?
    // probably smaller mse if too similar?

    val pathCosts = costMap.toList().sortedBy { it.second }
    val tileToPrint = if (null == pathCosts[0].first) "none" else formattedChows[pathCosts[0].first]

    println("After thinking about it ${1 + detectedChows.size} times, I think I will go with ${tileToPrint}.")
    println("PathCosts: ${pathCosts.joinToString(", ") { "(${it.first}, cost: ${it.second})" }}")
    return pathCosts[0].first?.get(0);
  }


}

data class SearchOutcome(
  val tileProbabilities: List<Pair<TileType, Double>>,
  val actualIterations: Int,
  val predictedWins: Int,
  val averageDepth: Double,
  val medianDepth: Int,
  val meanSquareError: Double,
)

fun search(amountOfWinsFound: Int, initialState: PlayerPov, minFaan: Int): SearchOutcome {
  val cancelAtPerWin = 10000
  val tilesToDraw = determineHiddenTiles(initialState);
  val futures = mutableListOf<Pair<List<List<TileType>>, List<TileType>>>()
  val tally: MutableMap<TileType, Double> = mutableMapOf<TileType, Double>()
  // is it possible to win at all?
  val winPossibility = findStandardWin(
    minimumFaanRequired = minFaan,
    hand = tilesToDraw,
    fixedMelds = initialState.melds,
    playerWind = initialState.playerSeat,
    prevailingWind = initialState.prevailingWind
  )
  if (winPossibility == null) {
    println("Winning is impossible :(")
    val uselessScore = initialState.hand.shuffled().map { Pair(it, NaN) }
    return SearchOutcome(
      tileProbabilities = uselessScore.map { Pair(it.first, it.second) },
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

  val maxTilesToDraw = min(14, tilesToDraw.size / 4)
  val numberOfPermutations = numberOfPermutations(tilesToDraw.size, maxTilesToDraw)
  val upperBound = min(amountOfWinsFound.toLong(), numberOfPermutations)

  val seqGen = RandomTileSequenceGenerator(
    tilesToDraw,
    maxTilesToDraw,
  )

  println("winsToFind: $upperBound, numberOfPermutations: ${if (numberOfPermutations == Long.MAX_VALUE) "Long.MAX_VALUE" else numberOfPermutations}, picking $maxTilesToDraw of ${tilesToDraw.size} tiles.")
  var runCounter = 0;
  while (
    //seqGen.existingSequences.size < numberOfPermutations &&
    runCounter < numberOfPermutations &&
    futures.size < amountOfWinsFound &&
    (runCounter < 5 * cancelAtPerWin || runCounter < futures.size * cancelAtPerWin)
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
        //print("\rfutures: ${futures.size}; runCounter: $runCounter; existingSequences: ${seqGen.existingSequences.size}; runCounter == existingSequences: ${runCounter == seqGen.existingSequences.size}")
        print("\rfutures: ${futures.size}; runCounter: $runCounter")
        break;
      }
    }
  }
  println()
  futures.sortBy { it.second.size }

  val minRestHandSize: Int = futures.minOfOrNull { it.second.size } ?: 0


  futures.forEach { f ->
    f.first.flatten().forEach { tally[it] = (tally[it] ?: 0.0) - 2.0.pow(minRestHandSize - f.second.size) }
    // f.second.toSet().forEach { tally[it] = (tally[it] ?: 0.0) + (0) }
    // f.second.forEach { tally[it] = (tally[it] ?: 0.0) + (1.0 / ((f.second.size - smallest.toDouble() + 1).pow(2.0))) }
    // print("Detected future win: " + f.first.joinToString("*") { formatTiles(it, 14, "") })
    // println(" With leftovers: " + formatTiles(f.second, 200, ""))
  }

  val uselessScore = initialState.hand.toSet().map { Pair(it, (tally[it] ?: 0.0)) }.sortedByDescending { it.second }

  // println("Run lengths: [ ${futures.map { it.second.size }.joinToString(", ")} ]")

  val averageDepth = futures.map { it.second.size }.average()
  val medianDepth = futures.map { it.second.size }.sorted().getOrNull(futures.size / 2) ?: -1
  val meanSquareError = futures.map { it.second.size }.map { val diff = it - averageDepth; diff * diff }.average()

  return SearchOutcome(
    tileProbabilities = uselessScore.map { Pair(it.first, it.second) },
    actualIterations = runCounter,
    predictedWins = futures.size,
    averageDepth = averageDepth,
    medianDepth = medianDepth,
    meanSquareError = meanSquareError
  )
}


fun printSearchResults(searchOutcome: SearchOutcome) {
  println("Thought for ${searchOutcome.actualIterations} and came up with:")
  val repetition = min(100, searchOutcome.tileProbabilities.size)
  repeat(repetition) {
    println(
      "${it.toString().padStart(2)}. : ${
        formatTiles(
          listOf(searchOutcome.tileProbabilities[it].first),
          1,
          ""
        )
      } ${searchOutcome.tileProbabilities[it].second}"
    )
  }
  println("Winnable simulation runs: ${searchOutcome.predictedWins}")
  println("Average tiles drawn: ${searchOutcome.averageDepth} Tiles ")
  println("Average number of tiles mse: ${searchOutcome.meanSquareError} ")
  println("Median tiles drawn: ${searchOutcome.medianDepth} Tiles ")
}

fun determineHiddenTiles(pov: PlayerPov): List<TileType> {
  val allTiles = mahjongSet.toMutableList()
  allTiles.removeAll(pov.discarded)
  allTiles.removeAll(pov.hand)
  allTiles.removeAll(pov.melds.flatten())
  allTiles.removeAll(pov.nextMelds.flatten())
  allTiles.removeAll(pov.prevMelds.flatten())
  allTiles.removeAll(pov.oppoMelds.flatten())
  return allTiles
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

fun <T> List<T>.combinations(k: Int): List<List<T>> {
  if (k == 0) return listOf(emptyList())
  if (k == 1) return this.map { listOf(it) }

  var result = mutableListOf<List<T>>()

  for (i in 0..(this.size - k)) {
    val head = this[i]
    val tailCombinations = subList(i + 1, this.size).combinations(k - 1)
    for (tail in tailCombinations) {
      result += listOf(head) + tail
    }
  }
  return result
}

fun pathCost(probability: Double, turns: Double): Double {
  return probability * (turns) + (1 - probability) * Integer.MAX_VALUE
}

fun compareHands(hand: List<TileType>, hand2: List<TileType>): Int {
  val tileMap1 = mutableMapOf<TileType, Int>()
  val tileMap2 = mutableMapOf<TileType, Int>()

  hand.forEach { tileMap1[it] = (tileMap1[it] ?: 0) + 1 }
  hand2.forEach { tileMap2[it] = (tileMap2[it] ?: 0) + 1 }
  return TileType.entries.sumOf { abs((tileMap1[it] ?: 0) - (tileMap2[it] ?: 0)) }
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
  private val length: Int,
) {
  // val existingSequences get() = _existingSequences.toSet()

  private val takenPrefixes: MutableMap<Int, MutableMap<List<TileType>, MutableList<TileType>>> = mutableMapOf()
  // private val _existingSequences = mutableSetOf<List<TileType>>()

  init {
    repeat(length) {
      takenPrefixes[it] = mutableMapOf()
    }
    takenPrefixes[0]!![emptyList()] = tilesToDrawFrom.toMutableList()
    takenPrefixes[length] = mutableMapOf()
  }

  fun generateRandomSequence(): List<TileType> {
    val remainingTiles = tilesToDrawFrom.toMutableList()
    val currentPrefix = mutableListOf<TileType>()

    while (currentPrefix.size < length) {
      val pickableTiles = takenPrefixes[currentPrefix.size]!![currentPrefix.toList()]
      // prefix not found, return randomSequence after booking everything into prefixes
      if (pickableTiles == null) {
        // build randomSequence and check everything in
        val output = currentPrefix + remainingTiles.shuffled().subList(0, length - currentPrefix.size)
        for (i in 1..output.size) {
          val set = takenPrefixes[i]!![output.subList(0, i)]
          if (set == null) {
            val actualRemainingTiles = tilesToDrawFrom.toMutableList()
            output.subList(i, output.size).forEach { actualRemainingTiles.remove(it) }
            val startingList = mutableListOf<TileType>()
            actualRemainingTiles.forEach { startingList.add(it) }
            takenPrefixes[i]!![output.subList(0, i)] = startingList
          } else {
            if (i < output.size) { // no other checks necessary, since we should be at the end, and thus its 0
              set.remove(output[i])
            }
          }

        }
        // _existingSequences.add(output)
        return output
      }

      // prefix found, lets choose our next tile
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
