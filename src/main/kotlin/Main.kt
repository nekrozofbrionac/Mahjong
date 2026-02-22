package de.nekrozofbrionac

import de.nekrozofbrionac.game.Game
import de.nekrozofbrionac.player.BotPlayer
import de.nekrozofbrionac.player.TerminalPlayer
import de.nekrozofbrionac.player.control.bot.LobotomizedBot
import de.nekrozofbrionac.player.control.bot.PongSpeedRunBot

fun main() {
  println("Hello World!")

  val game = Game(
    TerminalPlayer(),
    // BotPlayer(PongSpeedRunBot),
    BotPlayer(PongSpeedRunBot),
    BotPlayer(PongSpeedRunBot),
    BotPlayer(PongSpeedRunBot),
  );
  game.start();
}