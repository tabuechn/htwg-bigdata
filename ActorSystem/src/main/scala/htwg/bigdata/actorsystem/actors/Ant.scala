package htwg.bigdata.actorsystem.actors

import akka.actor.{Actor, ActorRef, ActorSystem}
import htwg.bigdata.actorsystem.Presets
import htwg.bigdata.actorsystem.util.Position

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.Duration

/**
  * Created by Michael Walz and Fabian Mog on 22.03.2017.
  */
class Ant(val navigatorRef: ActorRef, var position: Position) extends Actor {

  val random = scala.util.Random
  val system = ActorSystem("antSystem")

  val duration = if (Presets.MaxDuration == 0) 0 else {
    random.nextInt(Presets.MaxDuration - Presets.MinDuration) + Presets.MinDuration
  }

  val cancellable =
    system.scheduler.schedule(Duration.Zero, Duration(duration, "millis"))(tellNewPosition)

  override def receive = {
    case pos: Position => {
      // set new position
      position = pos
    }
    case "fieldOccupied" => {
      // do nothing
    }
    case "kill" => {
      // final position reached
      cancellable.cancel
      context.stop(self)
      context.unbecome
    }
    case _ => {
      println("unknown message")
    }
  }

  def tellNewPosition = {

    // init result position with current position
    var result = new Position(position.x, position.y)
    var tmp = new Position(position.x, position.y)

    // increase x OR y randomly OR (increase x and y)
    var randomInt = random.nextInt(3)

    // x already on border
    if (position.x >= Presets.FinalPosition.x) {
      randomInt = 2
    }

    // y already on border
    if (position.y >= Presets.FinalPosition.y) {
      randomInt = 1
    }

    if (randomInt == 0) {
      if (position.x < Presets.FinalPosition.x && position.y < Presets.FinalPosition.y) {
        result = new Position(position.x + 1, position.y + 1)
      }
    } else if (randomInt == 1) {
      if (position.x < Presets.FinalPosition.x) {
        result = new Position(position.x + 1, position.y)
      }
    } else if (randomInt == 2) {
      if (position.y < Presets.FinalPosition.y) {
        result = new Position(position.x, position.y + 1)
      }
    }

    // ask navigator about new position
    navigatorRef ! result
  }
}
