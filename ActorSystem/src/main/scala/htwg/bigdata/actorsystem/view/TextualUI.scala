package htwg.bigdata.actorsystem.view

import java.util.concurrent.atomic.AtomicInteger

import akka.actor.ActorRef
import htwg.bigdata.actorsystem.Presets
import htwg.bigdata.actorsystem.util.Position

import scala.collection.concurrent.TrieMap
import scala.collection.mutable

/**
  * Created by Michael Walz and Fabian Mog on 30.03.2017.
  */
object TextualUI {

  def printBoard(antPositions: TrieMap[ActorRef, Position], collisions: AtomicInteger, kills: AtomicInteger, failedKills: AtomicInteger, movesDone: AtomicInteger) = {

    /*
    val positions = antPositions.values.toList
    val strBuilder = new StringBuilder
    for (row <- 0 to Presets.FieldWidth) {
      for (col <- 0 to Presets.FieldWidth) {
        if (positions.exists(p => p.x == col && p.y == row)) {
          strBuilder ++= " "
          strBuilder ++= Console.BLACK_B + Console.RED + "@" + Console.BLACK
        } else {
          strBuilder ++= " O"
        }
      }
      strBuilder ++= "\n"
    }

    println
    println(strBuilder.toString)
    */
    println("Moves: " + movesDone + ", Ants: " + antPositions.size + ", Collisions: " + collisions + ", Successful kills: " + kills + ", Failed kills: " + failedKills)
  }

}
