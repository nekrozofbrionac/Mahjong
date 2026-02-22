package de.nekrozofbrionac

import de.nekrozofbrionac.game.Game
import de.nekrozofbrionac.player.TerminalPlayer
import de.nekrozofbrionac.player.VoidPlayer

fun main() {
  println("Hello World!")

  val game = Game(
    TerminalPlayer(),
    VoidPlayer(),
    VoidPlayer(),
    VoidPlayer()
  );
  game.start();
}