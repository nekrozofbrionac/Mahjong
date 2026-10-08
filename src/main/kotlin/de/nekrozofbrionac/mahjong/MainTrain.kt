package de.nekrozofbrionac.mahjong

import de.nekrozofbrionac.mahjong.player.control.bot.ml.Brain
import de.nekrozofbrionac.mahjong.game.Game
import de.nekrozofbrionac.mahjong.player.BotPlayer
import de.nekrozofbrionac.mahjong.player.control.PlayerControlAction
import de.nekrozofbrionac.mahjong.player.control.RecordedAction
import de.nekrozofbrionac.mahjong.player.control.RecordingPlayerControl
import de.nekrozofbrionac.mahjong.player.control.bot.NoIdeaAboutHandsBot
import java.io.File
import kotlin.random.Random



fun gameSimulation(whatToDoWhenConcluded: (List<RecordedAction>) -> Unit): () -> Unit = {
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
            whatToDoWhenConcluded(it.playerControl.recording())
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
  println("Mahjong Training")

  val brainFile = "new_brain"
  val brain = if (File(brainFile).exists() && File(brainFile).isDirectory) {
    Brain(brainFile)
  } else {
    Brain(brainFile, mapOf(
      Pair(PlayerControlAction.WIN, listOf()),
      Pair(PlayerControlAction.CHOOSE_DISCARD, listOf(1000, 500, 200, 100)),
      Pair(PlayerControlAction.KONG_FROM_DRAW, listOf(500, 200, 100)),
      Pair(PlayerControlAction.KONG_FROM_DISCARD, listOf(500, 200, 100)),
      Pair(PlayerControlAction.PONG_FROM_DISCARD, listOf(500, 200, 100)),
      Pair(PlayerControlAction.CHOW_FROM_DISCARD, listOf(500, 200, 100)),
    ))
  }
  println("\n" + brain.smallSummary())

  (0..12)
    .map { Thread(gameSimulation { brain.addToTrainingCache(it) }) }
    .forEach { it.start() }

  val minBatchSize = 50

  var waitDotCounter = 0
  while (true) {
    if (brain.biggestCacheSize() < minBatchSize) {
      Thread.sleep(2000)
      print("\r${brain.biggestCacheSize()} (${brain.cacheSize} total) samples collected.${".".repeat(waitDotCounter % 5)}                                       ")
      waitDotCounter++
      continue
    }
    waitDotCounter = 0

    print("\rTraining on ${brain.cacheSize} samples                                                                                                           ")
    brain.trainOnCache(minBatchSize)
    print("\rSaving Evaluations...                                                                                           ")
    brain.evalOnCache()
    print("\rClearing Cache...                                                                                           ")
    brain.clearCache()

    print("\r" + brain.smallSummary())

    print("\nSaving brain...   ")
    brain.save()
    print("\rSaving finished.")
  }
}