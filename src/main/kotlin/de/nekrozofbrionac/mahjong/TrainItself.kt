package de.nekrozofbrionac.mahjong

import de.nekrozofbrionac.mahjong.player.control.bot.ml.Brain
import de.nekrozofbrionac.mahjong.game.Game
import de.nekrozofbrionac.mahjong.player.control.bot.LobotomizedBot
import de.nekrozofbrionac.mahjong.player.control.bot.NoIdeaAboutHandsBot
import de.nekrozofbrionac.mahjong.player.control.bot.PongSpeedRunBot
import de.nekrozofbrionac.mahjong.player.control.bot.ml.GamblingNeuralNetBot
import de.nekrozofbrionac.mahjong.player.control.bot.ml.PlayerControlAction
import de.nekrozofbrionac.mahjong.player.control.bot.ml.RecordingBotPlayer
import de.nekrozofbrionac.mahjong.player.control.bot.ml.RecordingPlayerControl
import java.io.File
import kotlin.random.Random


fun main() {
  println("Mahjong Training, Gambling, Playing Itself")

  val brainFile = "Wan_Yumeko"
  val brain = if (File(brainFile).exists() && File(brainFile).isDirectory) {
    Brain(brainFile)
  } else {
    val hiddenLayers = listOf(1024, 512, 256, 128)
    Brain(
      brainFile, mapOf(
        Pair(PlayerControlAction.WIN, listOf(1)),
        Pair(PlayerControlAction.CHOOSE_DISCARD, hiddenLayers),
        Pair(PlayerControlAction.KONG_FROM_DRAW, hiddenLayers),
        Pair(PlayerControlAction.KONG_FROM_DISCARD, hiddenLayers),
        Pair(PlayerControlAction.PONG_FROM_DISCARD, hiddenLayers),
        Pair(PlayerControlAction.CHOW_FROM_DISCARD, hiddenLayers),
      )
    )
  }
  println("\n" + brain.smallSummary())


  val recordingPlayerControl1 = RecordingPlayerControl(GamblingNeuralNetBot(verbose = false, brain = brain, gamblingFactors = mapOf(
    PlayerControlAction.WIN to 0.0f,
    PlayerControlAction.CHOOSE_DISCARD to 0.2f,
    PlayerControlAction.KONG_FROM_DRAW to 0.2f,
    PlayerControlAction.KONG_FROM_DISCARD to 0.2f,
    PlayerControlAction.PONG_FROM_DISCARD to 0.2f,
    PlayerControlAction.CHOW_FROM_DISCARD to 0.2f
  )))
  val recordingPlayerControl2 = RecordingPlayerControl(GamblingNeuralNetBot(verbose = false, brain = brain, gamblingFactors = mapOf(
    PlayerControlAction.WIN to 0.0f,
    PlayerControlAction.CHOOSE_DISCARD to 0.01f,
    PlayerControlAction.KONG_FROM_DRAW to 0.1f,
    PlayerControlAction.KONG_FROM_DISCARD to 0.1f,
    PlayerControlAction.PONG_FROM_DISCARD to 0.01f,
    PlayerControlAction.CHOW_FROM_DISCARD to 0.01f
  )))
  val recordingPlayerControl3 = RecordingPlayerControl(GamblingNeuralNetBot(verbose = false, brain = brain, gamblingFactors = mapOf(
    PlayerControlAction.WIN to 0.0f,
    PlayerControlAction.CHOOSE_DISCARD to 0.00f,
    PlayerControlAction.KONG_FROM_DRAW to 0.3f,
    PlayerControlAction.KONG_FROM_DISCARD to 0.3f,
    PlayerControlAction.PONG_FROM_DISCARD to 0.0f,
    PlayerControlAction.CHOW_FROM_DISCARD to 0.0f
  )))
  val recordingPlayerControl4 = RecordingPlayerControl(GamblingNeuralNetBot(verbose = false, brain = brain, gamblingFactors = mapOf(
    PlayerControlAction.WIN to 0.0f,
    PlayerControlAction.CHOOSE_DISCARD to 0.0f,
    PlayerControlAction.KONG_FROM_DRAW to 0.3f,
    PlayerControlAction.KONG_FROM_DISCARD to 0.3f,
    PlayerControlAction.PONG_FROM_DISCARD to 0.0f,
    PlayerControlAction.CHOW_FROM_DISCARD to 0.0f
  )))
  val recordingRandom = RecordingPlayerControl(LobotomizedBot())
  val recordingRandom2 = RecordingPlayerControl(LobotomizedBot())
  val pongSpeedRunBot = RecordingPlayerControl(PongSpeedRunBot())
  val pongSpeedRunBot2 = RecordingPlayerControl(PongSpeedRunBot())
  val niah = RecordingPlayerControl(NoIdeaAboutHandsBot(iterations = 100, verbose = false))
  val niah2 = RecordingPlayerControl(NoIdeaAboutHandsBot(iterations = 100, verbose = false))
  val niah3 = RecordingPlayerControl(NoIdeaAboutHandsBot(iterations = 100, verbose = false))
  val niah4 = RecordingPlayerControl(NoIdeaAboutHandsBot(iterations = 100, verbose = false))


  val wonGames = mutableMapOf<Int, Int>()

  var gamesPlayed = 0
  var totalGamesPlayed = 0
  while (true) {
    if (!brain.minBatchSizeSatisfied()) {
      val players = listOf(
        RecordingBotPlayer("1", recordingPlayerControl1),
        RecordingBotPlayer("2", recordingPlayerControl2),
        RecordingBotPlayer("3", recordingPlayerControl3),
        RecordingBotPlayer("4", recordingPlayerControl4),
        RecordingBotPlayer("5", recordingRandom),
        RecordingBotPlayer("6", recordingRandom2),
        RecordingBotPlayer("7", pongSpeedRunBot),
        RecordingBotPlayer("8", pongSpeedRunBot2),
        RecordingBotPlayer("9", niah),
        RecordingBotPlayer("10", niah2),
        RecordingBotPlayer("11", niah3),
        RecordingBotPlayer("11", niah4),
      ).shuffled().take(4).toList()
      val game = Game(
        playerEast = players[0],
        playerSouth = players[1],
        playerWest = players[2],
        playerNorth = players[3],
        { gr ->
          players.forEach {
            if (it.playerControl.winRecorded) {
              brain.addToTrainingCache(it.playerControl.recording())
              wonGames[gr.minFaan] = (wonGames[gr.minFaan] ?: 0) + 1
            }
            it.playerControl.reset();
          }

        },
        verbose = false,
        wind = Random.nextInt(0, 4),
        minFaan = listOf(
          0, 0, 0,
          1, 1, 1,
          2, 2,// 2,
          3, 3,// 3, 3,
          4, 4, 4,// 4, 4,
          5, 5, 5,// 5, 5, 5,
          6, 6, 6, 6,// 6, 6, 6,
          7, 7, 7, 7,// 7, 7, 7, 7,
          8, 8, 8, 8,// 8, 8, 8, 8, 8
        ).random()// Random.nextInt(0, 9) // 0 to 8
      );
      game.start();
      gamesPlayed++
      totalGamesPlayed++

      print("\r${brain.cacheSize} samples collected from $gamesPlayed games ($totalGamesPlayed total) (${wonGames.entries.sortedBy { (k, v) -> k }.joinToString(", ") { "(${it.key}: ${it.value})" }}, total: ${wonGames.entries.sumOf {(k, v) -> v}})")
      continue
    }
    gamesPlayed = 0

    print("\rEvaluating before training...                                                                                           ")
    brain.evalOnCache()
    print("\rTraining on ${brain.cacheSize} samples                                                                                                           ")
    brain.trainOnCache()
    print("\rClearing Cache...                                                                                           ")
    brain.clearCache()

    print("\r" + brain.smallSummary())

    print("\nSaving brain...   ")
    brain.save()
    print("\rSaving finished.")
  }
}
