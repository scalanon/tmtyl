package org.scalanon.tmtyl.util

sealed abstract class Orientation(val degrees: Int)

object Orientation {
  def forName(name: String): Orientation = name match {
    case "Up"    => Up
    case "Down"  => Down
    case "Left"  => Left
    case "Right" => Right
    case o       => throw new IllegalArgumentException(o)
  }

  case object Up    extends Orientation(90)
  case object Down  extends Orientation(-90)
  case object Left  extends Orientation(180)
  case object Right extends Orientation(0)
}
