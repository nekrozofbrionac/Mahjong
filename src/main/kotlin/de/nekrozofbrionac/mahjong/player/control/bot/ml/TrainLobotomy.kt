package de.nekrozofbrionac.mahjong.player.control.bot.ml

import java.io.File

fun main() {
  var stopRequested = false
  val brainFile = "fuckassbrain"

  //val brain = Brain(listOf(100, 100))
  //brain.save(brainFile)

  val brain = Brain(brainFile)
  println("Trained on: ${brain.amountOfTraining} gamestates")
  println("Layers: " + brain.layers.size)
  brain.layers.forEachIndexed{ index, layer -> println("Layer $index: " + layer.first.shape.contentToString() + " " + layer.second.size) }

  // Thought(winning=2.42802125E9, discard01=-1.31141568E9, discard02=4.97068032E8, discard03=-1.34006259E9, discard04=6.9281464E9, discard05=-5.8459181E8, discard06=-1.59790208E9, discard07=9.2635075E9, discard08=-4.74488032E8, discard09=1.88949043E9, discard10=1.06113925E10, discard11=-5.12738304E8, discard12=2.09576755E10, discard13=-4.8763596E7, discard14=-1.92730224E8, kongFromDraw=2.67462328E10, kongFromDiscard=1.81751583E10, pongFromDiscard=1.04094126E10, chowFromDiscardLowest=-3.7336936E7, chowFromDiscardMiddle=1.31641559E10, chowFromDiscardHighest=-1.36304416E8, chowAtAll=1.31408241E10)
  // println(brain.think(PlayerControlAction.WIN, emptyPov))


  println("loading just 100 game sets")
  val penisSet = readRecording(100)

  println("now training...")
  // println(brain.trainSingle(firstThousand[0]).size)
  val inputThread = Thread {
    val input = System.`in`
    while (input.read() != -1) {
      stopRequested = true
      break
    }
  }
  inputThread.start()

  while (true) {
    if (stopRequested) {
      break
    }
    val mseBefore = brain.evaluateError(penisSet)
    brain.massTrain(penisSet)
    val mseAfter = brain.evaluateError(penisSet)
    saveMse(brainFile  + "_mse_log.csv", mseBefore, mseAfter)
    brain.save(brainFile)
    print("\n ${brain.amountOfTraining}:  MSE: " + mseBefore + " -> " + mseAfter + " aka " + (mseBefore - mseAfter) + " improvement")
  }
}
