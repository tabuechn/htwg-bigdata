package htwg.bigdata.actorsystem.model

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

  val cancellable =
    system.scheduler.schedule(Duration.Zero, Duration(random.nextInt(Presets.MinVelocity), "millis"))(tellNewPosition)

  override def receive = {
    case pos: Position => {
      // set new position
      position = pos
    }
    case "fieldOccupied" => {
      // ask again for a new position
      tellNewPosition
    }
    case "kill" => {
      // final position reached
      cancellable.cancel
      context.unbecome
    }
    case _ => {
      println("unknown message")
    }
  }

  def tellNewPosition = {

    // init result position with current position
    var result = new Position(position.x, position.y)

    // increase x OR y randomly
    if (random.nextInt(2) == 0) {
      if (position.x < Presets.FinalPosition.x) {
        result = new Position(position.x + 1, position.y)
      }
    } else {
      if (position.y < Presets.FinalPosition.y) {
        result = new Position(position.x, position.y + 1)
      }
    }

    // ask navigator about new position
    navigatorRef ! result
  }
}
