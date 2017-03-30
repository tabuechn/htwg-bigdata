package htwg.bigdata.actorsystem.view

import akka.actor.ActorRef
import htwg.bigdata.actorsystem.Presets
import htwg.bigdata.actorsystem.util.Position

import scala.collection.concurrent.TrieMap

/**
  * Created by Michael Walz and Fabian Mog on 30.03.2017.
  */
object TextualUI {

  def printBoard(antPositions: TrieMap[ActorRef, Position]) = {

    //val board = Array.ofDim[Array[Char]](Presets.FieldWidth)

    var board = ""


    for (i <- 0 to Presets.FieldWidth) {
      for (j <- 0 to Presets.FieldWidth) {

      }
    }

    for (
      i <- 1 to 2;
      j <- 1 to 2
    ) {
      println(i)
      println(j)
    }

    println

  }

}
