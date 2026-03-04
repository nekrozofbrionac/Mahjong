package de.nekrozofbrionac.mahjong.player.control.bot

import de.nekrozofbrionac.mahjong.board.SimpleBoard
import de.nekrozofbrionac.mahjong.board.TileType
import de.nekrozofbrionac.mahjong.formatTiles
import de.nekrozofbrionac.mahjong.game.PlayerPov
import de.nekrozofbrionac.mahjong.hands.evaluateStandardHand
import de.nekrozofbrionac.mahjong.player.control.PlayerControl
import de.nekrozofbrionac.mahjong.printPov
import kotlin.Double.Companion.NaN
import kotlin.math.abs
import kotlin.math.min

val mahjongSet = SimpleBoard().wall.filter { !it.isFlower() }.toList()

class NoIdeaAboutHandsBot(
  val iterations: Int = 10000,
) : PlayerControl {

  override fun win(pov: PlayerPov): Boolean {
    return true;
  }

  override fun chooseDiscard(pov: PlayerPov): TileType {
    printPov(pov)
    println("I have to discard...")
    println("Thinking for $iterations iterations...")
    val searchOutcome = search(iterations, pov)
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
    println("I can pong...")
    println("I am thinking about not ponging at all... for $iterations times.")
    val searchNoPong = search(iterations, pov)
    println("After thinking about not ponging i came up with:")
    printSearchResults(searchNoPong)
    println("Keep in mind i started with 13 tiles when searching, not 14")

    val pongedTile = pov.discarded.last()
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
    println(
      "I am thinking about ponging with ${
        formatTiles(
          listOf(pongedTile, pongedTile, pongedTile),
          3,
          ""
        )
      }... for $iterations times."
    )
    val searchWithPong = search(iterations, povAfterPong)
    println(
      "After thinking about chowing with ${
        formatTiles(
          listOf(pongedTile, pongedTile, pongedTile),
          3,
          ""
        )
      } i came up with:"
    )
    printSearchResults(searchWithPong)


    val noPongTerm = pathCost(searchNoPong.averageDepth / iterations.toDouble(), searchNoPong.averageDepth + 1)
    val withPongTerm = pathCost(searchWithPong.averageDepth / iterations.toDouble(), searchWithPong.averageDepth)

    val output = withPongTerm <= noPongTerm
    println("After thinking about it, I think I will go with ${if (output) "ponging" else "not ponging"} ($noPongTerm no Pong, $withPongTerm with Pong).")
    return output
  }

  override fun chowFromDiscard(pov: PlayerPov): TileType? {
    printPov(pov)
    println("I can chow...")
    val possiblities = determineChowPossibilities(pov.hand, pov.discarded.last());

    println("I am thinking about not chowing at all... for $iterations times.")

    // todo probably not perfect, i am forgetting kongs, but so did i within search
    val searchNoChow = search(iterations, pov) // keep in mind that this starts with 13 tiles instead of 14
    println("After thinking about not chowing i came up with:")
    printSearchResults(searchNoChow)
    println("Keep in mind that i thought about it using 1 tile less, so the amount of tiles i needed to ’draw' in my head is greater")


    val chowMap = mutableMapOf<TileType, SearchOutcome>();
    possiblities.forEach { chow ->

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
      println("I am thinking about chowing with ${formatTiles(chow, 3, "")}... for $iterations times.")
      chowMap[chow[0]] = search(iterations, hypotheticalPovAfterChow)
      println("After thinking about chowing with ${formatTiles(chow, 3, "")} i came up with:")
      printSearchResults(chowMap[chow[0]]!!)
    }

    // chose the smallest average?
    // probably smaller mse if too similar?


    val results: MutableList<Pair<TileType?, SearchOutcome>> =
      mutableListOf(Pair(null, searchNoChow.copy(averageDepth = searchNoChow.averageDepth + 1))) // extra tile
    chowMap.forEach { e -> results.add(Pair(e.key, e.value)) }

    val pathCosts = results.map { p ->
      Pair(
        p.first,
        pathCost(p.second.successfulSimulations.toDouble() / p.second.iterations.toDouble(), p.second.averageDepth)
      )
    }.toMutableList()

    pathCosts.sortBy { it.second }


    val tileToPrint = if (null == pathCosts[0].first) "none" else formatTiles(listOf(pathCosts[0].first!!), 1, "")

    println("After thinking about it ${1 + possiblities.size} times, I think I will go with ${tileToPrint}.")
    println("PathCosts: ${pathCosts.joinToString(", ") { "(${it.first}, cost: ${it.second})" }}")
    return pathCosts[0].first;
  }


}

data class SearchOutcome(
  val tileProbabilities: List<Pair<TileType, Double>>,
  val iterations: Int,
  val successfulSimulations: Int,
  val averageDepth: Double,
  val medianDepth: Int,
  val meanSquareError: Double,
)

fun search(iterations: Int, initialState: PlayerPov): SearchOutcome {
  val tilesToDraw = determineHiddenTiles(initialState);
  val futures = mutableListOf<Pair<List<List<TileType>>, List<TileType>>>()
  val tally: MutableMap<TileType, Double> = mutableMapOf<TileType, Double>()
  // loop
  // draw tiles until win, what tiles not to use
  // discard what tile was most unused

  // instead of for loop, maybe a while until iterations futures has been found?

  // is it possible to win at all?
  val winPossibility = determineAverageWinWithMoreTiles(initialState, 0, tilesToDraw, initialState.melds)
  if (winPossibility == null) {
    val uselessScore = initialState.hand.shuffled().map { Pair(it, NaN) }
    return SearchOutcome(
      tileProbabilities = uselessScore.map { Pair(it.first, it.second) },
      iterations = 0,
      successfulSimulations = 0,
      averageDepth = 0.0,
      medianDepth = 0,
      meanSquareError = 0.0
    )
  }

  // that means it IS possible to win!

  val numberOfCombinations = nOverK(tilesToDraw.size, tilesToDraw.size / 4)
  val upperBound = min(iterations.toLong(), numberOfCombinations)

  val seqGen = RandomTileSequenceGenerator(
    tilesToDraw,
    tilesToDraw.size/4
  )

  println("upperBound: $upperBound, numberOfCombinations: $numberOfCombinations")
  var runCounter = 0;
  while (seqGen.existingSequences.size < numberOfCombinations && futures.size < iterations) { // ok, for i in again
    runCounter++;
    val hypotheticalRestToDraw = seqGen.generateRandomSequence()
    // val hypotheticalRestToDraw = generateRandomSequence(triedHypotheticals, tilesToDraw, tilesToDraw.size / 4)
    // val hypotheticalRestToDraw = tilesToDraw.shuffled().subList(0, tilesToDraw.size / 4)
    if (hypotheticalRestToDraw.isEmpty()) {
      throw UnsupportedOperationException ("HOW is this empty=!!?!??!?!")
    }
    /*
    if (seqGen.existingSequences.contains(hypotheticalRestToDraw)) {
      println("hey, i already tried this :( also we tried ${seqGen.existingSequences.size} combinations")
    }
    */

    for (j in 1..hypotheticalRestToDraw.size) {
      val hypotheticalSuperHand = initialState.hand + hypotheticalRestToDraw.subList(0, j)
      // win detected
      val result = determineAverageWinWithMoreTiles(initialState, 3, hypotheticalSuperHand, initialState.melds)
      if (null != result) {
        futures.add(Pair(result.first, result.second))
        break;
      }
    }
  }


  futures.sortBy { it.second.size }

  /*
  val initialHand = initialState.hand.toMutableList()
  initialHand.addAll(initialState.melds.flatten()) // kongs will be weird.
  initialHand.sort()

  val winningMeldsMap = mutableMapOf<List<TileType>, Int>()

  futures.map { it.first.flatten().sorted() }
    .forEach { winningMeldsMap[it] = (winningMeldsMap[it] ?: 0) + 1 }

  winningMeldsMap
    .toList()
    .sortedByDescending{ it.second }
    .sortedBy { compareHands(initialHand, it.first) }
    .forEach { p ->
      println("${formatTiles(p.first, 14, "")} : ${p.second} times, ${compareHands(initialHand, p.first)} diff")
    }
  val smallest = futures.firstOrNull()?.second?.size ?: NaN
  */
  futures.forEach { f ->
    f.first.flatten().toSet().forEach { tally[it] = (tally[it] ?: 0.0) - (1.0) }
    f.second.toSet().forEach { tally[it] = (tally[it] ?: 0.0) + (1.0) }
    // f.second.forEach { tally[it] = (tally[it] ?: 0.0) + (1.0 / ((f.second.size - smallest.toDouble() + 1).pow(2.0))) }
    // println("Detected future win: " + f.first.joinToString("*") { formatTiles(it, 14, "") })
    // println("With leftovers: " + formatTiles(f.second, 200, ""))
  }


  val uselessScore =
    initialState.hand.toSet().map { Pair(it, (tally[it] ?: -10000.0)) }.sortedByDescending { it.second }

  // println("Run lengths: [ ${futures.map { it.second.size }.joinToString(", ")} ]")

  val averageDepth = futures.map { it.second.size }.average()
  val medianDepth = futures.map { it.second.size }.sorted().getOrNull(futures.size / 2) ?: -1
  val meanSquareError = futures.map { it.second.size }.map { val diff = it - averageDepth; diff * diff }.average()

  return SearchOutcome(
    tileProbabilities = uselessScore.map { Pair(it.first, it.second.toDouble()) },
    iterations = runCounter,
    successfulSimulations = futures.size,
    averageDepth = averageDepth,
    medianDepth = medianDepth,
    meanSquareError = meanSquareError
  )
}


fun printSearchResults(searchOutcome: SearchOutcome) {
  println("Thought for ${searchOutcome.iterations} and came up with:")
  val repetition = min(10, searchOutcome.tileProbabilities.size)
  repeat(repetition) {
    println(
      "$it. : ${
        formatTiles(
          listOf(searchOutcome.tileProbabilities[it].first),
          1,
          ""
        )
      } ${searchOutcome.tileProbabilities[it].second}"
    )
  }
  println("Winnable simulation runs: ${searchOutcome.successfulSimulations}")
  println("Average simulation run length: ${searchOutcome.averageDepth} Tiles ")
  println("Average simulation run length mse: ${searchOutcome.meanSquareError} ")
  println("Median simulation run length: ${searchOutcome.medianDepth} Tiles ")
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

fun determineAverageWinWithMoreTiles(
  initialState: PlayerPov,
  minimumFaanRequired: Int = 0,
  hand: List<TileType>, // more than 14 tiles possible
  fixedMelds: List<List<TileType>>,
): Pair<List<List<TileType>>, List<TileType>>? {
  val sortedHand = hand.sorted();
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

    if (mutableRestMelds.size + fixedMelds.size >= 5) { // 1 eye and maybe more than 4 melds, we need to get to more FAAN!
      // there is only one eye
      val eyelessMelds = mutableRestMelds.filter { it.size > 2 }.toMutableList();
      eyelessMelds.combinations(4 - (fixedMelds.size))
        .forEach {
          val recombinedEyeMelds = it.toMutableList()
          recombinedEyeMelds.add(listOf(eyeTile, eyeTile))
          recombinedEyeMelds.addAll(fixedMelds)

          val result = evaluateStandardHand(
            meldsIncludingEye = recombinedEyeMelds,
            prevWind = initialState.prevailingWind,
            playerWind = initialState.playerSeat,
            flowers = initialState.flowers,
            maxFaan = 13
          )

          if (result.first >= minimumFaanRequired) {
            for (i in 0..<it.size) {
              if (!mutableRestMelds.remove(it[i])) {
                throw UnsupportedOperationException("WHAT THE HELL WHY NOT")
              }
            }
            if (!mutableRestMelds.remove(listOf(eyeTile, eyeTile))) {
              println("WHAT THE HELL")
              println(formatTiles(mutableRestMelds.flatten(), 100, ""))
              throw UnsupportedOperationException("WHAT THE HELL WHY NOT WITH THE EYETILE")
            }

            mutableRestHand.addAll(mutableRestMelds.flatten())
            return Pair(recombinedEyeMelds, mutableRestHand)
          }
        }
    }
  }

  return null;
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
  return probability * (turns) + (1 - probability) * 100000
}

fun compareHands(hand: List<TileType>, hand2: List<TileType>): Int {
  val tileMap1 = mutableMapOf<TileType, Int>()
  val tileMap2 = mutableMapOf<TileType, Int>()

  hand.forEach { tileMap1[it] = (tileMap1[it] ?: 0) + 1 }
  hand2.forEach { tileMap2[it] = (tileMap2[it] ?: 0) + 1 }
  return TileType.entries.sumOf { abs((tileMap1[it] ?: 0) - (tileMap2[it] ?: 0)) }
}

fun nOverK(n: Int, k: Int): Long {
  require(n >= 0) { "n must be >= 0" }
  require(k >= 0) { "k must be >= 0" }
  require(k <= n) { "k must be <= n" }

  // symmetry: C(n, k) == C(n, n-k)
  val kk = minOf(k, n - k)

  var result = 1L
  for (i in 1..kk) {
    val hm = (result * (n - kk + i) / i)
    if (hm < 0) {
      return result
    }
    result = hm
  }

  return result
}

class RandomTileSequenceGenerator(
  private val tilesToDrawFrom: List<TileType>,
  private val length: Int,
) {
  val existingSequences get() = _existingSequences.toSet()

  private val takenPrefixes: MutableMap<Int, MutableMap<List<TileType>, MutableSet<TileType>>> = mutableMapOf()
  private val _existingSequences = mutableSetOf<List<TileType>>()

  fun generateRandomSequence() : List<TileType> {
    val remainingTiles = tilesToDrawFrom.toMutableList()
    val output = mutableListOf<TileType>()

    while (output.size < length) {
      val prefixCheck = takenPrefixes[output.size]?.get(output.toList())
      // prefix not found, return randomSeqeunce after booking everything into prefixes
      if (prefixCheck == null) {
        // build randomsequence and check everything in
        val actualOutput = output + remainingTiles.shuffled().subList(0, length - output.size)
        for (i in 1..actualOutput.size) {
          val set = takenPrefixes.getOrPut(i) { mutableMapOf() }.get(actualOutput.subList(0,i))
          if (set == null) {
            val actualRemainingTiles = tilesToDrawFrom.toMutableList()
            actualOutput.subList(i, actualOutput.size).forEach { actualRemainingTiles.remove(it) }
            val startingSet = mutableSetOf<TileType>()
            actualRemainingTiles.forEach { startingSet.add(it) }
            takenPrefixes.getOrPut(i) { mutableMapOf() }[actualOutput.subList(0,i)] = startingSet
          } else {
            if (i < actualOutput.size) { // no other checks necessary, since we should be at the end, and thus its 0
              set.remove(actualOutput[i])
            }
          }

        }
        _existingSequences.add(actualOutput)
        return actualOutput
      }
      if (prefixCheck.isEmpty()) {
        println("prefixcheck is Empty?")
        return listOf()// I guess we have everything?
      }
      // prefix found, lets choose our next tile
      val nextTile = prefixCheck.random()
      output.add(nextTile)
      remainingTiles.remove(nextTile)
    }
    return output
  }
}

fun generateRandomSequence(existingSequences: Set<List<TileType>>, tilesToDrawFrom: List<TileType>, length: Int): List<TileType> {
  val remainingTiles = tilesToDrawFrom.toMutableList();
  val output = mutableListOf<TileType>()

  while (output.size < length) {

    if (existingSequences.any{ it.subList(0, output.size) != output.toList() }) {
      return output + remainingTiles.shuffled().subList(0, length - output.size)
    }
    if (output.size == length) {
      println("output.size == length, also output : ${formatTiles(output, 1000, "")}")
      return listOf()
    }
    // val tilesToChooseFrom = remainingTiles.filterNot { rt -> existingSequences.map { it[output.size] }.contains(rt) }
    // val randomTile = if (tilesToChooseFrom.isEmpty()) remainingTiles.random() else tilesToChooseFrom.random()
    val randomTile = remainingTiles.random()
    output.add(randomTile)
    remainingTiles.remove(randomTile)
  }
  return output
}