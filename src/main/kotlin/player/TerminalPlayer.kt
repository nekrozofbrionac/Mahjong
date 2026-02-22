package de.nekrozofbrionac.player

import Player
import de.nekrozofbrionac.mahjong.board.TileType
import de.nekrozofbrionac.player.control.PlayerControl

class TerminalPlayer : AbstractPlayer() {
  override val playerControl: PlayerControl = object : PlayerControl {
    override fun act() = Unit
    override fun chooseDiscard(p: Player): TileType {
      return interact("Name a tile to discard, e.g. \"b1\":") { l ->
        p.hand.find {
          l.uppercase().trim() == it.shortHand
        }
      }
    }

    override fun kongFromDiscard(player: Player, tile: TileType): Boolean {
      return interact("Kong with ${tile.name.lowercase()}? (Y/n)") { l ->
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

    override fun pongFromDiscard(player: Player, tile: TileType): Boolean {
      return interact("Pong with ${tile.name.lowercase()}? (Y/n)") { l ->
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

    override fun chowFromDiscard(player: Player, tile: TileType): Boolean {
      return interact("Chow with ${tile.name.lowercase()}? (Y/n)") { l ->
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