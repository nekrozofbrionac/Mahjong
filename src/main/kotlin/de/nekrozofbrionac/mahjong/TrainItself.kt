package de.nekrozofbrionac.mahjong

import de.nekrozofbrionac.mahjong.player.control.bot.ml.Brain
import de.nekrozofbrionac.mahjong.game.Game
import de.nekrozofbrionac.mahjong.player.control.bot.ml.NeuralNetBot
import de.nekrozofbrionac.mahjong.player.control.bot.ml.PlayerControlAction
import de.nekrozofbrionac.mahjong.player.control.bot.ml.RecordingBotPlayer
import de.nekrozofbrionac.mahjong.player.control.bot.ml.RecordingPlayerControl
import java.io.File
import kotlin.random.Random


fun main() {
  println("Mahjong Training, Reinforcement Learning")

  val brainFile = "WAN_HU_2"
  val brain = if (File(brainFile).exists() && File(brainFile).isDirectory) {
    Brain(brainFile)
  } else {
    Brain(
      brainFile, mapOf(
        Pair(PlayerControlAction.WIN, listOf()),
        Pair(PlayerControlAction.CHOOSE_DISCARD, listOf(1000, 500, 200, 100)),
        Pair(PlayerControlAction.KONG_FROM_DRAW, listOf(500, 200, 100)),
        Pair(PlayerControlAction.KONG_FROM_DISCARD, listOf(500, 200, 100)),
        Pair(PlayerControlAction.PONG_FROM_DISCARD, listOf(500, 200, 100)),
        Pair(PlayerControlAction.CHOW_FROM_DISCARD, listOf(500, 200, 100)),
      )
    )
  }
  println("\n" + brain.smallSummary())

  val recordingPlayerControl1 = RecordingPlayerControl(NeuralNetBot(verbose = false, brain = brain))
  val recordingPlayerControl2 = RecordingPlayerControl(NeuralNetBot(verbose = false, brain = brain))
  val recordingPlayerControl3 = RecordingPlayerControl(NeuralNetBot(verbose = false, brain = brain))
  val recordingPlayerControl4 = RecordingPlayerControl(NeuralNetBot(verbose = false, brain = brain))

  val minBatchSize = 100

  while (true) {
    var gamesPlayed = 0
    if (brain.biggestCacheSize() < minBatchSize) {
      val players = listOf(
        RecordingBotPlayer("1", recordingPlayerControl1),
        RecordingBotPlayer("2", recordingPlayerControl2),
        RecordingBotPlayer("3", recordingPlayerControl3),
        RecordingBotPlayer("4", recordingPlayerControl4),
      )
      val game = Game(
        playerEast = players[0],
        playerSouth = players[1],
        playerWest = players[2],
        playerNorth = players[3],
        { gr ->
          players.forEach {
            if (it.playerControl.winRecorded) {
              brain.addToTrainingCache(it.playerControl.recording())
            }
            it.playerControl.reset();
          }

        },
        verbose = false,
        wind = Random.nextInt(0, 4),
        minFaan = Random.nextInt(0, 9) // 0 to 8
      );
      game.start();


      gamesPlayed++
      print("\r${brain.biggestCacheSize()} (${brain.cacheSize} total) samples collected from $gamesPlayed games                                         ")
      continue
    }

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
