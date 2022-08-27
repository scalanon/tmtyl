package org.scalanon.tmtyl
package game.actors

import org.scalanon.tmtyl.game.{Enemy, Game}

object Enemies {
  def spawn(enemy: Enemy, game: Game): Actor = enemy.kind match {
    case "Drone"     =>
      new Drone(enemy.x.toFloat, game)
    case "Fighter"   =>
      new Fighter(enemy.x.toFloat, enemy.y.toFloat, game)
    case "Flame"     =>
      new Flame(enemy.x.toFloat, enemy.y.toFloat, Orientation.Up, game)
    case "FlameLeft" =>
      new Flame(enemy.x.toFloat, enemy.y.toFloat, Orientation.Left, game)
    case "FlameRight" =>
      new Flame(enemy.x.toFloat, enemy.y.toFloat, Orientation.Right, game)
    case "Mortar"    =>
      new Mortar(enemy.x.toFloat, enemy.y.toFloat, game)
  }
}
