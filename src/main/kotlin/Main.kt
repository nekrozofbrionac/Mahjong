package de.nekrozofbrionac

import de.nekrozofbrionac.game.Game
import de.nekrozofbrionac.game.Turn
import de.nekrozofbrionac.player.BotPlayer
import de.nekrozofbrionac.player.TerminalPlayer
import de.nekrozofbrionac.player.control.bot.LobotomizedBot
import de.nekrozofbrionac.player.control.bot.PongSpeedRunBot

fun main() {
  println("Mahjong")

  val winners = mutableListOf<Turn?>()
  val maxGames = 1
  val verbose = true
  repeat(maxGames) {
    val game = Game(
      BotPlayer(LobotomizedBot(verbose = verbose)),
      BotPlayer(PongSpeedRunBot(verbose = verbose)),
      BotPlayer(LobotomizedBot(verbose = verbose)),
      BotPlayer(LobotomizedBot(verbose = verbose)),
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