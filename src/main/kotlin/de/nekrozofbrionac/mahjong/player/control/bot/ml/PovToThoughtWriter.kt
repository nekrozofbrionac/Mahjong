package de.nekrozofbrionac.mahjong.player.control.bot.ml

import de.nekrozofbrionac.mahjong.game.PlayerPov
import de.nekrozofbrionac.mahjong.player.control.PlayerControlAction
import java.io.BufferedInputStream
import java.io.BufferedOutputStream
import java.io.DataInputStream
import java.io.DataOutputStream
import java.io.FileInputStream
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

fun readRecording(howMany: Int = -1): List<Pair<FloatArray, FloatArray>> {
  val result = mutableListOf<Pair<FloatArray, FloatArray>>()
  DataInputStream(
    BufferedInputStream(
      FileInputStream(saveFile)
    )
  ).use { input ->
    while (result.size < howMany || howMany == -1) {
      try {
        val firstFloats = FloatArray(6629) { input.readFloat() }
        val secondFloats = FloatArray(22) { input.readFloat() }
        result.add(Pair(firstFloats, secondFloats))
      } catch (e: Exception) {
        break
      }
    }
  }
  return result
}