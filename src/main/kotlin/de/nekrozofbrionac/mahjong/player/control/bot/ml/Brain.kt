package de.nekrozofbrionac.mahjong.player.control.bot.ml

import de.nekrozofbrionac.mahjong.board.TileType
import de.nekrozofbrionac.mahjong.formatTile
import de.nekrozofbrionac.mahjong.formatTiles
import de.nekrozofbrionac.mahjong.game.PlayerPov
import java.io.File
import java.nio.file.Files
import java.nio.file.StandardOpenOption
import java.time.LocalDateTime
import java.util.Locale
import kotlin.io.path.Path

class Brain {
  val brainFileName: String
  private val lobes: Map<PlayerControlAction, Lobe>

  private var cacheInUse = false
  private val trainingCache: Map<PlayerControlAction, MutableList<RecordedAction>> =
    PlayerControlAction.entries.associateWith { mutableListOf() }
  private val secondTrainingCache: Map<PlayerControlAction, MutableList<RecordedAction>> =
    PlayerControlAction.entries.associateWith { mutableListOf() }
  private val minBatchSize: Map<PlayerControlAction, Int> = mapOf(
    PlayerControlAction.WIN to 50,
    PlayerControlAction.CHOOSE_DISCARD to 100,
    PlayerControlAction.KONG_FROM_DRAW to 10,
    PlayerControlAction.KONG_FROM_DISCARD to 10,
    PlayerControlAction.PONG_FROM_DISCARD to 50,
    PlayerControlAction.CHOW_FROM_DISCARD to 50,
  )

  private val lastRecordedError: MutableMap<PlayerControlAction, Pair<Long, Float>> =
    PlayerControlAction.entries.associateWith { Pair(0L, Float.NaN) }.toMutableMap()

  var cacheSize = 0
    get() = trainingCache.values.sumOf { it.size }
    private set

  constructor(
    fileName: String,
    intermediateLayerSizes: Map<PlayerControlAction, List<Int>>
  ) {
    if (File(fileName).exists()) {
      throw UnsupportedOperationException("Hey this constructor is for creating a new brain, but the file $fileName already exists. Use the other constructor to load an existing brain.")
    }
    PlayerControlAction.entries.forEach { action ->
      if (!intermediateLayerSizes.containsKey(action)) {
        throw IllegalStateException("No intermediate layer size for $action")
      }
    }

    File(fileName).mkdir()
    val initialLobes = mutableMapOf<PlayerControlAction, Lobe>()
    PlayerControlAction.entries.forEach { action ->
      val intermediateSizesToUse = intermediateLayerSizes[action]!!
      val lobe = Lobe(POV_TO_INPUT_SIZE, action.outputSize, intermediateSizesToUse, action.predictionType)
      lobe.save("$fileName/${action.name}")
      initialLobes[action] = lobe
    }

    brainFileName = fileName
    lobes = initialLobes
  }

  constructor(fileName: String) {
    if (!File(fileName).exists()) {
      throw UnsupportedOperationException("Hey this constructor is for loading an existing brain, but the file $fileName does not exist. Use the other constructor to create a new brain.")
    }
    val loadedLobes = mutableMapOf<PlayerControlAction, Lobe>()
    PlayerControlAction.entries.forEach { action ->
      val lobe = Lobe("$fileName/${action.name}")
      loadedLobes[action] = lobe
    }

    brainFileName = fileName
    lobes = loadedLobes
  }

  fun thinkAbout(
    playerControlAction: PlayerControlAction,
    pov: PlayerPov
  ): FloatArray {
    val lobe = lobes[playerControlAction] ?: throw IllegalStateException("No lobe for $playerControlAction")
    // println("Thinking about $playerControlAction with POV: ${pov.hand.toString()}")
    return lobe.think(povToInputLayer(pov), generateOutputMask(pov, playerControlAction))
  }

  fun minBatchSizeSatisfied(): Boolean {
    return PlayerControlAction.entries.any { action -> (trainingCache[action]!!.size >= minBatchSize[action]!!) }
  }

  @Synchronized
  fun addToTrainingCache(input: List<RecordedAction>) {
    if (cacheInUse) {
      input.forEach {
        secondTrainingCache[it.action]!!.add(it)
      }
    } else {
      input.forEach {
        trainingCache[it.action]!!.add(it)
      }
    }
  }

  @Synchronized
  fun relocateLonelySamples() {
    trainingCache.forEach { (action, recordedActions) ->
      if (recordedActions.size < minBatchSize[action]!!) {
        secondTrainingCache[action]!!.addAll(trainingCache[action]!!)
        trainingCache[action]!!.clear()
      }
    }
  }

  fun trainOnCache() {
    cacheInUse = true
    relocateLonelySamples()

    lobes.forEach { (action, lobe) ->
      val trainingData = trainingCache[action]!!.map {
        TrainingSample(
          povToInputLayer(it.pov),
          it.decision,
          generateOutputMask(it.pov, action)
        )
      }
      lobe.massTrain(trainingData)
    }
  }

  fun evalOnCache() {
    cacheInUse = true
    lobes.forEach { (action, lobe) ->
      val samples = trainingCache[action]!!.map {
        println("\nfaan:     " + it.pov.minFaan)
        println(action)
        if (action == PlayerControlAction.CHOW_FROM_DISCARD || action == PlayerControlAction.PONG_FROM_DISCARD || action == PlayerControlAction.KONG_FROM_DISCARD) {
          println("discarded  " + formatTile(it.pov.discarded.lastOrNull() ?: throw IllegalStateException("No discarded tile found, but game suggested to chow/pong/kong.")))
        }
        println("pov hand   " + formatTiles(it.pov.hand.sorted(), 14, "") + "  " + it.pov.melds.joinToString("  ") {m ->  formatTiles(m, 4, "") })
        if (action == PlayerControlAction.CHOOSE_DISCARD) {
          println("           " + TileType.entries.joinToString(" ") { formatTile(it).padStart(4) })
        }
        println("Mask       " + generateOutputMask(it.pov, action).joinToString(" ") { "%.0f".format(it).padStart(4) })
        println("expected   " + it.decision.joinToString(" ") { "%.0f".format(it).padStart(4) })
        println("actual     " + lobe.think(povToInputLayer(it.pov), generateOutputMask(it.pov, action)).joinToString(" ") { "%.2f".format(it).padStart(4) })
        TrainingSample(povToInputLayer(it.pov), it.decision, generateOutputMask(it.pov, action))
      }
      val error = lobe.evaluateError(samples)
      if (error.isNaN() || lobe.amountOfTraining == lastRecordedError[action]!!.first) {
        return@forEach
      }
      File("$brainFileName/${action.name}_error.csv").appendText("${lobe.amountOfTraining},$error\n")
      lastRecordedError[action] = Pair(lobe.amountOfTraining, error)
    }
  }

  @Synchronized
  fun clearCache() {
    trainingCache.forEach { it.value.clear() }
    secondTrainingCache.forEach { (a, ra) -> trainingCache[a]!!.addAll(ra) }
    secondTrainingCache.forEach { it.value.clear() }
    cacheInUse = false
  }

  fun save() {
    File(brainFileName).mkdirs()
    lobes.forEach { (action, lobe) ->
      lobe.save("$brainFileName/${action.name}")
      lobe.save("$brainFileName/${action.name}_backup")
    }
    Files.writeString(
      Path("$brainFileName/$brainFileName"),
      "Brain saved at ${LocalDateTime.now()}\n" + brainSummary(),
      StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING
    )
  }

  fun brainSummary(): String {
    var loveLobeLine = brainFileName + " " + lobes.values.sumOf { it.amountOfTraining } + " total training iterations"
    lobes.forEach { (action, lobe) ->
      loveLobeLine += "\n${action.name}:"
      loveLobeLine += "\n    ${lobe.amountOfTraining} Iterations"
      loveLobeLine += "\n    Layers: ${lobe.layers.size} ${lobe.predictionType}"
      loveLobeLine += "\n            (${lobe.layers.joinToString(", ") { it.first.shape.contentToString() }})"
    }
    return loveLobeLine
  }

  fun smallSummary(): String {
    return buildString {
      val totalIterations = lobes.values.sumOf { it.amountOfTraining }
      val nameWidth = lobes.keys.maxOfOrNull { it.name.length } ?: 0
      val iterWidth = lobes.values.maxOfOrNull { it.amountOfTraining.toString().length } ?: 0

      fun formatFloat(mse: Float?): String =
        if (mse == null || mse.isNaN()) "n/a" else "%.6f".format(Locale.US, mse)

      val errorWidth = lobes.keys.maxOfOrNull { formatFloat(lastRecordedError[it]!!.second).length } ?: 0
      val extWidth = lobes.keys.maxOfOrNull { trainingCache[it]!!.size.toString().length } ?: 0
      val errorNameWidth = lobes.values.maxOf { it.predictionType.errorName.length }

      appendLine("------ SUMMARY ------")
      appendLine("$brainFileName $totalIterations total training iterations")

      lobes.forEach { (action, lobe) ->
        val name = "${action.name}:".padEnd(nameWidth + 1)
        val iterations = lobe.amountOfTraining.toString().padStart(iterWidth)


        val error = formatFloat(lastRecordedError[action]!!.second).padStart(errorWidth)
        val external = trainingCache[action]!!.size.toString().padStart(extWidth)
        val errorNameToDisplay = lobe.predictionType.errorName.padEnd(errorNameWidth)

        appendLine("$name Iterations: $iterations, $errorNameToDisplay: $error, Samples Waiting: $external")
      }
    }.trimEnd()
  }
}
