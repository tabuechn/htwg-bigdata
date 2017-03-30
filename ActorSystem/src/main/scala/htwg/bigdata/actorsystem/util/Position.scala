package htwg.bigdata.actorsystem.util

/**
  * Created by Michael Walz and Fabian Mog on 22.03.2017.
  */
case class Position(val x: Int, val y: Int) {

  override def toString = {
    x + "/" + y
  }

  override def equals(o: Any) = o match {
    case that: Position => that.x == this.x && that.y == this.y
    case _ => false
  }

  override def hashCode = {
    var result = x
    result = 31 * result + y
    result
  }
}
