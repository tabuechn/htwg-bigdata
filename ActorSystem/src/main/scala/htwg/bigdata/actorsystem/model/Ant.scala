package htwg.bigdata.actorsystem.model

import akka.actor.Actor
import htwg.bigdata.actorsystem.Presets
import htwg.bigdata.actorsystem.util.Position

/**
  * Created by Michael Walz on 22.03.2017.
  */
class Ant(var position: Position) extends Actor {

  val random = scala.util.Random

  override def receive = {
    case pos: Position => position = pos
    case _ => {
      move
      sender() ! position
    }
  }

  def move = {
    val r = random.nextInt(4) // range is 0 inclusive to 3 inclusive
    r match {
      case 0 => position = new Position(
        position.x,
        if (position.y > 0) position.y - 1 else 0
      )
      case 1 => position = new Position(
        if (position.x < Presets.FieldWidth) position.x + 1 else Presets.FieldWidth,
        position.y
      )
      case 2 => position = new Position(
        position.x,
        if (position.y < Presets.FieldWidth) position.y + 1 else Presets.FieldWidth
      )
      case 3 => position = new Position(
        if (position.x > 0) position.x - 1 else 0,
        position.y
      )
    }
  }
}
