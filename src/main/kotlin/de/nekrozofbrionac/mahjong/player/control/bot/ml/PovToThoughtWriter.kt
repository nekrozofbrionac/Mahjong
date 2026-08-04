package de.nekrozofbrionac.mahjong.player.control.bot.ml

import de.nekrozofbrionac.mahjong.game.PlayerPov
import de.nekrozofbrionac.mahjong.player.control.PlayerControlAction
import java.io.BufferedOutputStream
import java.io.DataOutputStream
import java.io.FileOutputStream

val saveFile = "bruh_data"

fun saveRecording(rec: List<Triple<PlayerControlAction, PlayerPov, Thought>>) {
  DataOutputStream(
    BufferedOutputStream(
      FileOutputStream(saveFile, true) // true = append mode
    )
  ).use { output ->
    rec.forEach {
      val firstFloats = povToInputLayer(it.first, it.second)
      val secondFloats = thoughtToFloat(it.third)
      firstFloats.forEach { value ->
        output.writeFloat(value)
      }
      secondFloats.forEach { value ->
        output.writeFloat(value)
      }
    }
  }
}