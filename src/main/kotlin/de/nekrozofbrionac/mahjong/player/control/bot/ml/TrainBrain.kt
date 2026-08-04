package de.nekrozofbrionac.mahjong.player.control.bot.ml

fun main() {
  // val brain = Brain(listOf(1000, 1000, 1000, 1000))
  // brain.save("brain_scan")

  val brain = Brain("brain_scan")
  println("Layers: " + brain.layers.size)
  brain.layers.forEachIndexed{ index, layer -> println("Layer $index: " + layer.size) }
}