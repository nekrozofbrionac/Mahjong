package de.nekrozofbrionac.player

import de.nekrozofbrionac.game.PlayerPov
import de.nekrozofbrionac.mahjong.board.TileType
import de.nekrozofbrionac.player.control.PlayerControl
import de.nekrozofbrionac.printPov

class TerminalPlayer : AbstractPlayer() {
  override val playerControl: PlayerControl = object : PlayerControl {
    override fun chooseDiscard(pov: PlayerPov): TileType {
      printPov(pov);
      return interact("Name a tile to discard, e.g. \"b1\":") { l ->
        pov.hand.find {
          l.uppercase().trim() == it.shortHand
        }
      }
    }

    override fun kongFromDiscard(pov: PlayerPov): Boolean {
      printPov(pov);
      if (pov.discarded.isEmpty()) {
        throw UnsupportedOperationException("Option to kong even though there are no discards?")
      }


      return interact("Kong with ${pov.discarded.last().name.lowercase()}? (Y/n)") { l ->
        when (l.trim().lowercase()) {
          "y" -> true
          "yes" -> true
          "true" -> true
          "n" -> false
          "no" -> false
          "false" -> false
          else -> null
        }
      }
    }

    override fun pongFromDiscard(pov: PlayerPov): Boolean {
      printPov(pov);
      if (pov.discarded.isEmpty()) {
        throw UnsupportedOperationException("Option to pong even though there are no discards?")
      }
      return interact("Pong with ${pov.discarded.last().name.lowercase()}? (Y/n)") { l ->
        when (l.trim().lowercase()) {
          "y" -> true
          "yes" -> true
          "true" -> true
          "n" -> false
          "no" -> false
          "false" -> false
          else -> null
        }
      }
    }

    override fun chowFromDiscard(pov: PlayerPov): Boolean {
      printPov(pov);
      if (pov.discarded.isEmpty()) {
        throw UnsupportedOperationException("Option to pong even though there are no discards?")
      }
      return interact("Chow with ${pov.discarded.last().name.lowercase()}? (Y/n)") { l ->
        when (l.trim().lowercase()) {
          "y" -> true
          "yes" -> true
          "true" -> true
          "n" -> false
          "no" -> false
          "false" -> false
          else -> null
        }
      }
    }
  }

  private fun <T> interact(prompt: String, parse: (String) -> T?): T {
    var output: T? = null;
    println("")
    println("------ PLAYER INTERACTION REQUIRED ------")
    println("")
    println("    $prompt")

    while (output == null) {
      print("    > ")
      val line = readlnOrNull() ?: continue
      output = parse(line) ?: continue
    }
    println("    (given answer: $output)")
    println("")
    println("------- PLAYER INTERACTION ENDED --------")
    println("")
    return output;
  }
}