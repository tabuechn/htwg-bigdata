package htwg.bigdata.actorsystem.simple.actors

import java.util.concurrent.atomic.AtomicInteger

import akka.actor.{Actor, ActorRef, ActorSystem}
import htwg.bigdata.actorsystem.simple.{AntsSimulation, Presets}
import htwg.bigdata.actorsystem.simple.util.Position
import htwg.bigdata.actorsystem.simple.view.TextualUI
import org.slf4j.LoggerFactory

import scala.collection.concurrent.TrieMap

/**
  * Created by Michael Walz and Fabian Mog on 22.03.2017.
  */
class Navigator(val antPositions: TrieMap[ActorRef, Position]) extends Actor {

  val logger = LoggerFactory.getLogger(this.getClass)

  val collisions = new AtomicInteger(0)
  val kills = new AtomicInteger(0)
  val failedKills = new AtomicInteger(0)
  val movesDone = new AtomicInteger(0)

  override def receive = {

    case demandedPosition: Position => {

      // check if ant reaches finish position
      if (demandedPosition != Presets.FinalPosition) {

        // check if position is empty
        if (causesCollisions(demandedPosition)) {
          collisions.incrementAndGet

          sender ! Messages.FieldOccupied

        } else {
          antPositions.put(sender, demandedPosition)
          movesDone.incrementAndGet
          draw(antPositions, collisions, kills, failedKills, movesDone)

          sender ! demandedPosition
        }

      } else {
        // ant demands finish position --> kill ant
        val removed = antPositions.remove(sender)

        if (!removed.isDefined) {
          failedKills.incrementAndGet

        } else {
          kills.incrementAndGet

          if (Presets.ShowProgressBar) println("\nAnt finished! Finished ants count is: " + kills)
          draw(antPositions, collisions, kills, failedKills, movesDone)

          sender ! Messages.Finished

          // shutdown actor system if all ants have finished
          if (antPositions.isEmpty) {
            ActorSystem("antSystem").terminate
            AntsSimulation.exitSimulation(antPositions, collisions, kills, failedKills, movesDone)
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
