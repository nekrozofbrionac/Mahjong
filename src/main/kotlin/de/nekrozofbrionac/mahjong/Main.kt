package de.nekrozofbrionac.mahjong

import de.nekrozofbrionac.mahjong.game.Game
import de.nekrozofbrionac.mahjong.game.GameReport
import de.nekrozofbrionac.mahjong.player.BotPlayer
import de.nekrozofbrionac.mahjong.player.control.bot.LobotomizedBot
import de.nekrozofbrionac.mahjong.player.control.bot.NoIdeaAboutHandsBot
import de.nekrozofbrionac.mahjong.player.control.bot.ml.Brain
import de.nekrozofbrionac.mahjong.player.control.bot.ml.NeuralNetBot
import kotlin.random.Random

fun main() {
  println("Mahjong")

  val brain = Brain("Wan_Yumeko")
  val wanHuBrain = Brain("Wan_Hu")
  val winners = mutableMapOf<String?, Int>()
  val maxGames = 1000
  val verbose = false

  for (i in 0..<maxGames) {
    val players = listOf(
      // TerminalPlayer("Literally me"),
      // BotPlayer("Wan_Yumeko", NeuralNetBot(verbose = false,  brain = brain)),
      // BotPlayer("Wan_Hu", NeuralNetBot(verbose, brain = wanHuBrain)),
      BotPlayer("niah2", NoIdeaAboutHandsBot(verbose = false, iterations = 1000)),
      BotPlayer("niah3", NoIdeaAboutHandsBot(verbose = false, iterations = 1000)),
      BotPlayer("niah4", NoIdeaAboutHandsBot(verbose = false, iterations = 1000)),
      BotPlayer("niah", NoIdeaAboutHandsBot(verbose = false, iterations = 1000)),
      // BotPlayer("wy1", NeuralNetBot(verbose = false,  brain = brain)),
      // BotPlayer("wy2", NeuralNetBot(verbose = false,  brain = brain)),
      // BotPlayer("wy3", NeuralNetBot(verbose = false,  brain = brain)),
    )
    val game = Game(
      playerEast = players[i % 4],
      playerSouth = players[(i + 1) % 4],
      playerWest = players[(i + 2) % 4],
      playerNorth = players[(i + 3) % 4],
      {
        printGameReport(it)
        val winnerName = it.winnerSeat?.let { seat -> it.players[seat]?.name } ?: "Draw"
        winners[winnerName] = winners.getOrDefault(winnerName, 0) + 1


        // println("")
        // println("*** Current Status ***")
        // println()
        // val longestName = winners.maxOf { it.key.toString().length }
        // val longestScore = i.toString().length
        // winners.toList().sortedByDescending { it.second }.forEach {
        //   println(it.first.toString().padStart(longestName) + ": " + it.second.toString().padStart(longestScore))
        // }
        // println("Total".padStart(longestName) + ": " + i.toString().padStart(longestScore))
      },
      verbose = verbose,
      wind = ((i / 4) % 4),
      minFaan = 8 // Random.nextInt(0, 9) // 0 to 8
    );
    game.start();
  }

  println()
  println("*** Conclusion ***")
  println()
  val longestName = winners.maxOf { it.key.toString().length }
  val longestScore = winners.maxOf { it.value.toString().length }
  winners.forEach {
    println(it.key.toString().padStart(longestName) + ": " + it.value.toString().padStart(longestScore))
  }
}

fun printGameReport(gr: GameReport) {
  if (gr.winnerSeat == null) {
    print("\n-------------------------------------------------------------------------------------------")
    print("\nGame Faan: ${gr.minFaan}")
    print("\nDrawn Game. ")
    print("\n")
    print("\n")
  } else {
    val formattedHand = formatTiles(gr.players[gr.winnerSeat]?.fullHand ?: listOf(), 18, "")
    print("\n-------------------------------------------------------------------------------------------")
    print("\nGame Faan: ${gr.minFaan}")
    print("\n   Winner:     ${gr.players[gr.winnerSeat]?.name} ${gr.winnerSeat}, ${gr.winnerScore} faan, ${gr.winnerScoreDesc}")
    print("\n            $formattedHand")
    print("\n")
  }
}