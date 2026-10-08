package de.nekrozofbrionac.mahjong.player.control.bot.ml

import de.nekrozofbrionac.mahjong.game.PlayerPov
import de.nekrozofbrionac.mahjong.player.control.PlayerControlAction
import de.nekrozofbrionac.mahjong.player.control.RecordedAction
import java.io.File
import java.nio.file.Files
import java.nio.file.StandardOpenOption
import java.time.LocalDateTime
import java.util.Locale
import java.util.concurrent.ConcurrentMap
import kotlin.io.path.Path

class Brain {
  val brainFileName: String
  private val lobes: Map<PlayerControlAction, Lobe>

  private var cacheInUse = false
  private val trainingCache: Map<PlayerControlAction, MutableList<RecordedAction>> =
    PlayerControlAction.entries.associateWith { mutableListOf() }
  private val secondTrainingCache: Map<PlayerControlAction, MutableList<RecordedAction>> =
    PlayerControlAction.entries.associateWith { mutableListOf() }

  private val lastMse: MutableMap<PlayerControlAction, Float> = PlayerControlAction.entries.associateWith { Float.NaN }.toMutableMap()

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
      val lobe = Lobe(POV_TO_INPUT_SIZE, action.outputSize, intermediateSizesToUse)
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
    return lobe.think(povToInputLayer(pov))
  }

  fun biggestCacheSize(): Int {
    return trainingCache.values.maxOf{ it.size }
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
  fun relocateLonelySamples(minBatchSize: Int) {
    trainingCache.forEach { (action, recordedActions) ->
      if (recordedActions.size < minBatchSize) {
        secondTrainingCache[action]!!.addAll(trainingCache[action]!!)
        trainingCache[action]!!.clear()
      }
    }
  }

  fun trainOnCache(minBatchSize: Int) {
    cacheInUse = true
    relocateLonelySamples(minBatchSize)

    lobes.forEach { (action, lobe) ->
      val trainingData = trainingCache[action]!!.map { Pair(povToInputLayer(it.pov), it.decision) }
      lobe.massTrain(trainingData)
    }
  }

  fun evalOnCache() {
    cacheInUse = true
    lobes.forEach { (action, lobe) ->
      val samples = trainingCache[action]!!.map { Pair(povToInputLayer(it.pov), it.decision) }
      val mse = lobe.evaluateError(samples)
      if (mse.isNaN()) {
        return@forEach
      }
      File("$brainFileName/${action.name}_mse.csv").appendText("${lobe.amountOfTraining},$mse\n")
      lastMse[action] = mse
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
      loveLobeLine += "\n    Layers: ${lobe.layers.size} (${lobe.layers.joinToString(", ") { it.first.shape.contentToString() }})"
    }
    return loveLobeLine
  }

  fun smallSummary(): String {
    return buildString {
      val totalIterations = lobes.values.sumOf { it.amountOfTraining }
      val nameWidth = lobes.keys.maxOfOrNull { it.name.length } ?: 0
      val iterWidth = lobes.values.maxOfOrNull { it.amountOfTraining.toString().length } ?: 0

      fun formatMse(mse: Float?): String =
        if (mse == null || mse.isNaN()) "n/a" else "%.6f".format(Locale.US, mse)

      val mseWidth = lobes.keys.maxOfOrNull { formatMse(lastMse[it]).length } ?: 0
      val extWidth = lobes.keys.maxOfOrNull { trainingCache[it]!!.size.toString().length } ?: 0

      appendLine("------ SUMMARY ------")
      appendLine("$brainFileName $totalIterations total training iterations")

      lobes.forEach { (action, lobe) ->
        val name = "${action.name}:".padEnd(nameWidth + 1)
        val iterations = lobe.amountOfTraining.toString().padStart(iterWidth)
        val mse = formatMse(lastMse[action]).padStart(mseWidth)
        val external = trainingCache[action]!!.size.toString().padStart(extWidth)
        appendLine("$name $iterations Iterations, MSE: $mse, Samples Withheld: $external")
      }
    }.trimEnd()
  }
}
