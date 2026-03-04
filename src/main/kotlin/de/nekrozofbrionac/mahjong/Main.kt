package de.nekrozofbrionac.mahjong

import de.nekrozofbrionac.mahjong.game.Game
import de.nekrozofbrionac.mahjong.game.Turn
import de.nekrozofbrionac.mahjong.player.BotPlayer
import de.nekrozofbrionac.mahjong.player.TerminalPlayer
import de.nekrozofbrionac.mahjong.player.control.bot.LobotomizedBot
import de.nekrozofbrionac.mahjong.player.control.bot.PongSpeedRunBot

fun main() {
  println("Mahjong")

  val winners = mutableListOf<Turn?>()
  val maxGames = 100
  val verbose = false
  repeat(maxGames) {
    val game = Game(
      // TerminalPlayer(),
      BotPlayer(LobotomizedBot(verbose = verbose)),
      BotPlayer(PongSpeedRunBot(verbose = verbose)),
      BotPlayer(PongSpeedRunBot(verbose = verbose)),
      BotPlayer(PongSpeedRunBot(verbose = verbose)),
      //BotPlayer(NoIdeaAboutHandsBot(iterations = 100)),
      //BotPlayer(NoIdeaAboutHandsBot(iterations = 100)),
      { p, t ->
        winners.add(t)
      },
      verbose = verbose
    );
    game.start();
  }

  println()
  println("*** Conclusion ***")
  println()
  println("East won ${winners.count { it == Turn.EAST }} times.")
  println("South won ${winners.count { it == Turn.SOUTH }} times.")
  println("West won ${winners.count { it == Turn.WEST }} times.")
  println("North won ${winners.count { it == Turn.NORTH }} times.")
  println("Meaning ${winners.count { it == null }} ended in a draw.")
}