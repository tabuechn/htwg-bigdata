package htwg.bigdata.actorsystem

import java.util.concurrent.TimeUnit

import scala.concurrent.duration.FiniteDuration

/**
  * Created by user on 22.03.2017.
  */
object Presets {

  val FieldWidth = 5
  val Trigger = ""
  val FoxFreq = new FiniteDuration(100, TimeUnit.MILLISECONDS)
  val AntFreq = new FiniteDuration(200, TimeUnit.MILLISECONDS)
  val Delay = new FiniteDuration(10, TimeUnit.MILLISECONDS)
  val MaxAnts = 6
}
