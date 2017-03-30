package htwg.bigdata.actorsystem.controller

import java.util.concurrent.atomic.AtomicInteger

import akka.actor.{Actor, ActorRef, ActorSystem, Props}
import htwg.bigdata.actorsystem.Presets
import htwg.bigdata.actorsystem.model.Ant
import htwg.bigdata.actorsystem.util.{Position, Timer}
import htwg.bigdata.actorsystem.view.TextualUI
import org.slf4j.LoggerFactory

import scala.collection.concurrent.TrieMap

/**
  * Created by Michael Walz and Fabian Mog on 22.03.2017.
  */
class Navigator(val antPositions: TrieMap[ActorRef, Position]) extends Actor {

  val logger = LoggerFactory.getLogger(this.getClass)
  var collisions = new AtomicInteger(0)
  var kills = new AtomicInteger(0)
  var failedKills = new AtomicInteger(0)
  var movesDone = new AtomicInteger(0)

  override def receive = {

    case pos: Position => {
      if (pos != Presets.FinalPosition) {
        if (causesCollisions(pos)) {
          collisions.incrementAndGet
          sender ! "fieldOccupied"
        } else {
          antPositions.put(sender, pos)

          movesDone.incrementAndGet

          TextualUI.printBoard(antPositions, collisions, kills, failedKills, movesDone)
          sender ! pos
        }
      } else {
        // ant demands finish position --> kill ant
        val removed = antPositions.remove(sender)

        if (!removed.isDefined) {
          failedKills.incrementAndGet

        } else {

          kills.incrementAndGet

          TextualUI.printBoard(antPositions, collisions, kills, failedKills, movesDone)
          sender ! "kill"

          // shutdown actor system if all ants have finished
          if (antPositions.isEmpty) {
            ActorSystem("antSystem").terminate
            Navigator.kill
          }
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

    // ask user to start
    println("Press any key to start simulation.")
    System.in.read

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
    logger.info("time elapsed: " + timer.getElapsedTime / 1000000000F)
    System.exit(0)
  }
}