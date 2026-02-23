package de.nekrozofbrionac.player

import de.nekrozofbrionac.formatTiles
import de.nekrozofbrionac.game.PlayerPov
import de.nekrozofbrionac.mahjong.board.TileType
import de.nekrozofbrionac.player.control.PlayerControl
import de.nekrozofbrionac.printPov

class TerminalPlayer : AbstractPlayer() {
  override val playerControl: PlayerControl = object : PlayerControl {
    override fun win(pov: PlayerPov): Boolean {
      printPov(pov)

      return interact("Win?") { l ->
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

    override fun chooseDiscard(pov: PlayerPov): TileType {
      printPov(pov);
      return interact("Name a tile to discard, e.g. \"b1\":") { l ->
        pov.hand.find {
          l.uppercase().trim() == it.shortHand
        }
      }
    }

    override fun kongFromDraw(pov: PlayerPov): Boolean {
      printPov(pov);

      return interact("Kong with drawn tile ${pov.hand.last().name.lowercase()}? (y/n)") { l ->
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

    override fun kongFromDiscard(pov: PlayerPov): Boolean {
      printPov(pov);
      if (pov.discarded.isEmpty()) {
        throw UnsupportedOperationException("Option to kong even though there are no discards?")
      }

      return interact("Kong with ${pov.discarded.last().name.lowercase()}? (y/n)") { l ->
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

      return interact("Pong with ${pov.discarded.last().name.lowercase()}? (y/n)") { l ->
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

    override fun chowFromDiscard(pov: PlayerPov): TileType? {
      printPov(pov);
      if (pov.discarded.isEmpty()) {
        throw UnsupportedOperationException("Option to chow even though there are no discards?")
      }

      val lastDiscard = pov.discarded.last()
      if (null == lastDiscard.numeral) {
        throw UnsupportedOperationException("Why am I being told to chow with a tile that doesnt even have a numeral?")
      }

      val chowPossibilities = determineChowPossibilities(pov.hand, lastDiscard)

      if (chowPossibilities.size == 1) {
        val chowToTake = interact("Chow with ${pov.discarded.last().name.lowercase()} to form ${formatTiles(chowPossibilities[0], 3, "")}? (Y/n)") { l ->
          when (l.trim().lowercase()) {
            "y" -> chowPossibilities[0]
            "yes" -> chowPossibilities[0]
            "true" -> chowPossibilities[0]
            "n" -> listOf()
            "no" -> listOf()
            "false" -> listOf()
            else -> null
          }
        }
        return if (chowToTake.isEmpty()) {
          null
        } else {
          chowToTake[0]
        }
      }

      val prompt: String = "Possible Chows: " + chowPossibilities.joinToString(" or ") { formatTiles(it, 3, "") } +
          "\n    Name the first tile of the chow e.g. for b3 b4 b5 \"b3\", or to pass type \"n\" for pass"

      val chowToTake = interact(prompt) { l ->
        val choice = pov.hand.find { l.uppercase().trim() == it.shortHand }
        val chosenChow = chowPossibilities.find { cp -> cp.isNotEmpty() && cp[0] == choice }
        if (null != chosenChow) {
          return@interact chosenChow
        }

        return@interact when (l.trim().lowercase()) {
          "pass" -> listOf()
          "n" -> listOf()
          "no" -> listOf()
          "false" -> listOf()
          else -> null
        }
      }
      return if (chowToTake.isEmpty()) {
        null
      } else {
        chowToTake[0]
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