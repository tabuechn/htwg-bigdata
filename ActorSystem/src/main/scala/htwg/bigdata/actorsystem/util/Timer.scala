package htwg.bigdata.actorsystem.util

/**
  * Created by Michael Walz on 30.03.2017.
  */
class Timer {

  private var now = 0L

  def start = {
    now = System.nanoTime
  }

  def getElapsedTime = {
    System.nanoTime - now
  }
}
