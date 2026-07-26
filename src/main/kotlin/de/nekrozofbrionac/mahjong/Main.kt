package de.nekrozofbrionac.mahjong

import de.nekrozofbrionac.mahjong.game.Game
import de.nekrozofbrionac.mahjong.game.GameReport
import de.nekrozofbrionac.mahjong.player.BotPlayer
import de.nekrozofbrionac.mahjong.player.control.bot.LobotomizedBot
import de.nekrozofbrionac.mahjong.player.control.bot.NoIdeaAboutHandsBot
import de.nekrozofbrionac.mahjong.player.control.bot.PongSpeedRunBot

fun main() {
  println("Mahjong")

  val winners = mutableMapOf<String?, Int>()
  val maxGames = 1000
  val verbose = false

  for (i in 0..<maxGames) {
    val players = listOf(
      BotPlayer("Lobo01", LobotomizedBot(verbose = verbose)),
      BotPlayer("Pong1", PongSpeedRunBot(verbose = verbose)),
      BotPlayer("Pong2", PongSpeedRunBot(verbose = verbose)),
      //BotPlayer("Pong3", PongSpeedRunBot(verbose = verbose)),
      BotPlayer("NIAH", NoIdeaAboutHandsBot(iterations = 1000, verbose = verbose)),
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

        println("")
        println("*** Current Status ***")
        println()
        val longestName = winners.maxOf { it.key.toString().length }
        val longestScore = i.toString().length
        winners.toList().sortedByDescending{ it.second }.forEach {
          println(it.first.toString().padStart(longestName) + ": " + it.second.toString().padStart(longestScore))
        }
        println("Total".padStart(longestName) + ": " + i.toString().padStart(longestScore))
      },
      verbose = verbose,
      wind = ((i / 4) % 4),
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
    print("\nDrawn Game. ")
    print("\n")
    print("\n")
  } else {
    val formattedHand = formatTiles(gr.players[gr.winnerSeat]?.fullHand ?: listOf(), 18, "")
    print("\nWinner:     ${gr.players[gr.winnerSeat]?.name} ${gr.winnerSeat}, ${gr.winnerScore} faan, ${gr.winnerScoreDesc}")
    print("\n            $formattedHand")
    print("\n")
  }
}