package htwg.bigdata.actorsystem.controller

import java.io.{BufferedWriter, File, FileWriter}
import java.util.Calendar
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
          draw(antPositions, collisions, kills, failedKills, movesDone)
          sender ! pos
        }
      } else {
        // ant demands finish position --> kill ant
        val removed = antPositions.remove(sender)

        if (!removed.isDefined) {
          failedKills.incrementAndGet

        } else {
          kills.incrementAndGet
          draw(antPositions, collisions, kills, failedKills, movesDone)
          sender ! "kill"

          // shutdown actor system if all ants have finished
          if (antPositions.isEmpty) {
            ActorSystem("antSystem").terminate
            Navigator.exitSimulation(antPositions, collisions, kills, failedKills, movesDone)
          }
        }
      }
    }
    case _ => // do nothing
  }

  def causesCollisions(position: Position): Boolean = {
    antPositions.values.exists(pos => pos == position)
  }

  def draw(antPositions: TrieMap[ActorRef, Position], collisions: AtomicInteger, kills: AtomicInteger,
           failedKills: AtomicInteger, movesDone: AtomicInteger) = {
    if (Presets.ShowBoard || Presets.ShowStats) {
      TextualUI.printBoard(antPositions, collisions, kills, failedKills, movesDone,
        Presets.ShowBoard, Presets.ShowStats)
    } else if (Presets.ShowProgressBar) {
      print('.')
    }
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

  def exitSimulation(antPositions: TrieMap[ActorRef, Position], collisions: AtomicInteger, kills: AtomicInteger,
                     failedKills: AtomicInteger, movesDone: AtomicInteger) = {

    var result = ""
    if (movesDone.get >= 0 && collisions.get >= 0 && kills.get == Presets.MaxAnts) {
      if (failedKills.get > 0) {
        result = "OK (failed removes from ants map have been handled)"
      } else {
        result = "OK"
      }
    } else {
      result = "NOT OK"
    }

    val strBuilder = new StringBuilder

    strBuilder ++= "\n\n\n"
    strBuilder ++= "-------------------------------------------------------------\n"
    strBuilder ++= "   FieldWidth: " + Presets.FieldWidth + "\n"
    strBuilder ++= "   SpawnWidth: " + Presets.SpawnWidth + "\n"
    strBuilder ++= "      MaxAnts: " + Presets.MaxAnts + "\n"
    strBuilder ++= "  MinVelocity: " + Presets.MinDuration + "\n"
    strBuilder ++= "FinalPosition: " + Presets.FinalPosition + "\n"
    strBuilder ++= "    ShowBoard: " + Presets.ShowBoard + "\n"
    strBuilder ++= "    ShowStats: " + Presets.ShowStats + "\n"
    strBuilder ++= "-------------------------------------------------------------\n"
    strBuilder ++= "        Moves: " + movesDone + "\n"
    strBuilder ++= "   Collisions: " + collisions + "\n"
    strBuilder ++= " Ants started: " + Presets.MaxAnts + "\n"
    strBuilder ++= "Ants finished: " + kills + "\n"
    strBuilder ++= " Failed kills: " + failedKills + "\n"
    strBuilder ++= "-------------------------------------------------------------\n"
    strBuilder ++= "       Result: " + result + "\n"
    strBuilder ++= "-------------------------------------------------------------\n"
    strBuilder ++= "         Time: " + timer.getElapsedTime / 1000000000F + " sec\n"
    strBuilder ++= "-------------------------------------------------------------\n"
    strBuilder ++= "\n\n\n"
    
    print(strBuilder)

    if (Presets.WriteToFile) {
      val fileName = "simulation_out_" + System.nanoTime
      val file = new File("output/" + fileName + ".txt")
      val bw = new BufferedWriter(new FileWriter(file))
      bw.write(strBuilder.toString)
      bw.close()
    }

    System.exit(0)
  }
}









































