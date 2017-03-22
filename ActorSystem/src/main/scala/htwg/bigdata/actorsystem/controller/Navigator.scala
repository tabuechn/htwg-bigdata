package htwg.bigdata.actorsystem.controller

import akka.actor.{Actor, ActorRef, ActorSystem, PoisonPill, Props}
import htwg.bigdata.actorsystem.Presets
import htwg.bigdata.actorsystem.model.Ant
import htwg.bigdata.actorsystem.util.Position

import scala.collection.concurrent.TrieMap
import scala.collection.mutable.HashSet
import scala.concurrent.ExecutionContext.Implicits.global

/**
  * Created by Michael Walz on 22.03.2017.
  */
class Navigator extends Actor {

  val antPositions = new TrieMap[ActorRef, Position]()
  val random = scala.util.Random
  var foxPosition = new Position(0, 0)

  val actorsys = Navigator.actorSystem

  var positions = TrieMap[ActorRef, Position]()

  // init
  createFox(new Position(Presets.FieldWidth, Presets.FieldWidth))
  createAnt(new Position(0, 0))
  createAnt(new Position(Presets.FieldWidth, 0))
  createAnt(new Position(0, Presets.FieldWidth))

  override def receive = {
    case pos: Position => {
      if (sender().path.name == "foxActor") {
        foxPosition = pos
      } else {
        antPositions.put(sender(), pos)
      }
      printPositions
      checkCollisions
    }
    case _ => {
    }
  }

  def checkCollisions = {
    antPositions.keySet.foreach(antRef => {
      if (antPositions.get(antRef) == foxPosition) {
        println("killed ant")
        antRef ! PoisonPill.getInstance
        antPositions.remove(antRef)
      }
    })
    if (antPositions.isEmpty) {
      println("no ants left")
      Navigator.actorSystem.terminate()
    } else {
      val numberOfAnts = antPositions.size
      val distinctPositions = new HashSet[Position]
      antPositions.values.foreach(p => distinctPositions.add(p))
      if (distinctPositions.size < numberOfAnts && numberOfAnts < Presets.MaxAnts) {
        createAnt(new Position(random.nextInt(Presets.FieldWidth), random.nextInt(Presets.FieldWidth)))
      }
    }
  }

  def printPositions = {
    var string = ""
    antPositions.foreach(p => string += (p._2.toString + "  "))
    print("fox on position: " + foxPosition + ", ants on positions: " + antPositions.toString())
  }

  def createFox(position: Position) = {
    val fox = Navigator.actorSystem.actorOf(Props[Ant], "foxActor")
    fox ! position
    foxPosition = position
    Navigator.actorSystem.scheduler.schedule(Presets.Delay, Presets.FoxFreq, fox, Presets.Trigger)
    println(fox.path.name + " on " + position)
  }

  def createAnt(position: Position) = {
    val ant = Navigator.actorSystem.actorOf(Props[Ant], "antActor")
    ant ! position
    antPositions.put(ant, position)
    Navigator.actorSystem.scheduler.schedule(Presets.Delay, Presets.AntFreq, ant, Presets.Trigger)
    println("new ant " + ant.path.name + " on " + position)
  }
}

object Navigator {

  val actorSystem = ActorSystem("antSimulationSystem")

  def main(args: Array[String]) {
    actorSystem.actorOf(Props[Navigator], "navigatorActor")
  }
}