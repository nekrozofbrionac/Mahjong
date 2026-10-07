package de.nekrozofbrionac.mahjong

import de.nekrozofbrionac.mahjong.game.Game
import de.nekrozofbrionac.mahjong.game.PlayerPov
import de.nekrozofbrionac.mahjong.player.BotPlayer
import de.nekrozofbrionac.mahjong.player.control.PlayerControlAction
import de.nekrozofbrionac.mahjong.player.control.RecordingPlayerControl
import de.nekrozofbrionac.mahjong.player.control.bot.NoIdeaAboutHandsBot
import de.nekrozofbrionac.mahjong.player.control.bot.ml.Brain
import de.nekrozofbrionac.mahjong.player.control.bot.ml.Thought
import de.nekrozofbrionac.mahjong.player.control.bot.ml.povToInputLayer
import de.nekrozofbrionac.mahjong.player.control.bot.ml.thoughtToFloat
import java.io.File
import java.util.function.Function
import kotlin.random.Random


fun saveProgress(saveToFile: String, amountOfSamples: Long, mseBefore: Float, mseAfter: Float) {
  File(saveToFile).appendText("$amountOfSamples,$mseBefore,$mseAfter\n")
}

private val nooneMayUseThis: MutableList<Triple<PlayerControlAction, PlayerPov, Thought>> = mutableListOf()
@Synchronized
fun <R> doWithRecordedGames(function: Function<MutableList<Triple<PlayerControlAction, PlayerPov, Thought>>, R>): R {
  return function.apply(nooneMayUseThis)
}



val gameSimulation = {
  val recordingPlayerControl1 = RecordingPlayerControl(NoIdeaAboutHandsBot(iterations = 100, verbose = false))
  val recordingPlayerControl2 = RecordingPlayerControl(NoIdeaAboutHandsBot(iterations = 100, verbose = false))
  val recordingPlayerControl3 = RecordingPlayerControl(NoIdeaAboutHandsBot(iterations = 100, verbose = false))
  val recordingPlayerControl4 = RecordingPlayerControl(NoIdeaAboutHandsBot(iterations = 100, verbose = false))

  while (true) {
    val players = listOf(
      BotPlayer("1", recordingPlayerControl1),
      BotPlayer("2", recordingPlayerControl2),
      BotPlayer("3", recordingPlayerControl3),
      BotPlayer("4", recordingPlayerControl4),
    )
    val game = Game(
      playerEast = players[0],
      playerSouth = players[1],
      playerWest = players[2],
      playerNorth = players[3],
      { gr ->
        players.forEach {
          if (it.playerControl.winRecorded) {
            doWithRecordedGames { r ->
              r.addAll(it.playerControl.recording())
              print("\rPlayed game with wind ${gr.prevailingWind} and min faan ${gr.minFaan} (${r.size}) samples collected)                                      ")
            }
          }
          it.playerControl.reset();
        }

      },
      verbose = false,
      wind = Random.nextInt(0, 4),
      minFaan = Random.nextInt(0, 9) // 0 to 8
    );
    game.start();
  }
}



fun main() {
  println("Mahjong training")

  val brainFile = "new_brain"
  val mseLogFile = "${brainFile}_mse_log.csv"
  val brain = if (File(brainFile).exists()) Brain(brainFile) else Brain(listOf(1000, 500, 200))

  println("Trained on: ${brain.amountOfTraining} gamestates")
  println("Layers:     " + brain.layers.size)
  brain.layers.forEachIndexed{ index, layer -> println("Layer $index:    " + layer.first.shape.contentToString() + " " + layer.second.size) }


  (0..8).map { Thread(gameSimulation) }.forEach { it.start() }

  var waitDotCounter = 0
  while (true) {
    val sampleSize = doWithRecordedGames { it.size }
    if (sampleSize < 50) {
      Thread.sleep(500)
      print("\r${sampleSize} samples collected (${waitDotCounter})                                       ")
      waitDotCounter++
      continue
    }
    waitDotCounter = 0

    val samples = doWithRecordedGames { r ->
      val huh = r.map{ Pair(povToInputLayer(it.first, it.second), thoughtToFloat(it.third)) }
      r.clear()
      return@doWithRecordedGames huh
    }

    print("\rTraining on ${samples.size} samples                                                                      ")
    val mseBefore = brain.evaluateError(samples)
    brain.massTrain(samples)
    val mseAfter = brain.evaluateError(samples)

    print("\r${brain.amountOfTraining}:  $mseBefore -> $mseAfter  (Improvement: ${mseBefore - mseAfter})                                        " +
          "\nSaving file...   ")
    brain.save(brainFile)
    brain.save("${brainFile}_backup")
    saveProgress(mseLogFile, brain.amountOfTraining, mseBefore, mseAfter)
    print("\rSaving finished.")
  }
}