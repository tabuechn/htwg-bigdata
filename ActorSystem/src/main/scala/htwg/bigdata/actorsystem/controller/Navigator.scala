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

  val actorsys = Navigator.actorSystem

  var positions = TrieMap[ActorRef, Position]()

  // init
  /*
  createAnt(new Position(0, 0), "f1")
  createAnt(new Position(Presets.FieldWidth, 0), "f2")
  createAnt(new Position(0, Presets.FieldWidth),"f3")
   */

  var a = 0;
  for( a <- 1 to 10000){
    createAnt(new Position(random.nextInt(10000000), random.nextInt(10000000)), "Ant"+a)
  }

  override def receive = {
    case pos: Position => {
      antPositions.put(sender(), pos)
      printPositions
      checkCollisions
    }
    case _ => {
    }
  }

  def checkCollisions = {
   println("   XXXXXX Number of ants: " + antPositions.size + " XXXX")

    /*
    antPositions.keySet.foreach(antRef => {
      if (antPositions.get(antRef).get == ) {
        println("killed ant")
        antRef ! PoisonPill.getInstance
        antPositions.remove(antRef)
        "free"
      }
      else
        {
          "taken"
        }
    })

    */
  }

  def printPositions = {
    var string = ""
    antPositions.foreach(p => string += (p._2.toString + "  "))
    println("Ants on positions: " + antPositions.toString())
  }

  def createAnt(position: Position, name: String) = {
    val ant = Navigator.actorSystem.actorOf(Props[Ant], name)
    ant ! position
    antPositions.put(ant, position)
    Navigator.actorSystem.scheduler.schedule(Presets.Delay, Presets.AntFreq, ant, Presets.Trigger)
    println("new ant " + ant.path.name + " on " + position)
    //println("Ants on positions: " + antPositions.toString())
  }
}

object Navigator {

  val actorSystem = ActorSystem("antSimulationSystem")

  def main(args: Array[String]) {
    actorSystem.actorOf(Props[Navigator], "navigatorActor")
  }
}