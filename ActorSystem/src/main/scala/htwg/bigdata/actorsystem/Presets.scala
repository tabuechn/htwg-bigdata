package htwg.bigdata.actorsystem

import htwg.bigdata.actorsystem.util.Position

/**
  * Created by Michael Walz and Fabian Mog on 22.03.2017.
  */
object Presets {

  // params
  val FieldWidth = 8
  val SpawnWidth = 3
  val MaxAnts = 100
  val FinalPosition = new Position(FieldWidth, FieldWidth)

  // if too low --> dead letters occur!
  val MinDuration = 30
  val MaxDuration = 200

  // print options
  val ShowBoard = true
  val ShowStats = false
  val ShowProgressBar = false
  val WriteToFile = true
}
