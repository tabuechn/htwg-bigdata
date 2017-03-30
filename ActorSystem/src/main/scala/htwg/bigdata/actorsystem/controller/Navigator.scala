package htwg.bigdata.actorsystem.controller

import akka.actor.{Actor, ActorRef, ActorSystem, Props}
import htwg.bigdata.actorsystem.Presets
import htwg.bigdata.actorsystem.model.Ant
import htwg.bigdata.actorsystem.util.Position
import htwg.bigdata.actorsystem.util.Timer
import org.slf4j.LoggerFactory

import scala.collection.concurrent.TrieMap

/**
  * Created by Michael Walz and Fabian Mog on 22.03.2017.
  */
class Navigator(val antPositions: TrieMap[ActorRef, Position]) extends Actor {

  val logger = LoggerFactory.getLogger(this.getClass)

  override def receive = {

    case pos: Position => {

      // TODO: replace with textual ui
      //println
      //println("============= Navigator =============")
      /*
      println("          TreeMap: " + antPositions.values)
      println("           Sender: " + sender)
      println("  Actual position: " + antPositions.get(sender).getOrElse(null))
      println("Demanded position: " + pos)
      */
      logger.info("TreeMap Size: " + antPositions.size)

      if (pos != Presets.FinalPosition) {
        if (causesCollisions(pos)) {
          //println("-> Position change denied!")
          sender ! "fieldOccupied"
        } else {
          //println("-> Position change accepted!")

          antPositions.put(sender, pos)

          sender ! pos

        }
      } else {
        // ant demands finish position --> kill ant
        println("--> Final position demanded -> Kill ant!")

        antPositions.remove(sender)

        sender ! "kill"

        // shutdown actor system if all ants have finished
        if (antPositions.isEmpty) {
          ActorSystem("antSystem").terminate
          Navigator.kill
        }
      }
    }
    case _ => {
      // do nothing
    }
  }

  def causesCollisions(position: Position): Boolean = {
    antPositions.values.exists(pos => {
      pos == position
    })
  }
}

object Navigator {

  val system = ActorSystem("antSystem")
  val random = scala.util.Random
  val timer = new Timer
  val logger = LoggerFactory.getLogger(this.getClass)

  def main(args: Array[String]) {

    val positions = TrieMap[ActorRef, Position]()

    // create navigator actor
    val navigator = system.actorOf(Props(new Navigator(positions)), "navigatorActor")

    // create ants and schedule them to ask navigator for collisions
    var it = 0
    for (it <- 0 to Presets.MaxAnts - 1) {
      val antPosition = new Position(random.nextInt(Presets.SpawnWidth + 1), random.nextInt(Presets.SpawnWidth + 1))
      val antActor = system.actorOf(Props(new Ant(navigator, antPosition)), name = "ant_" + it)
      positions.put(antActor, antPosition)
    }

    // start time measurement
    timer.start
  }

  def kill = {
    logger.info("time elapsed: " + timer.getElapsedTime)
    System.exit(0)
  }
}