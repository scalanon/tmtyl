package org.scalanon.tmtyl
package game.actors

import org.scalanon.tmtyl.game.{Enemy, Game}
import org.scalanon.tmtyl.util.Orientation

object Enemies {
  def spawn(enemy: Enemy, game: Game): Actor = enemy.kind match {
    case "Drone"                                                  =>
      new Drone(enemy.x.toFloat, game)
    case "Fighter"                                                =>
      new Fighter(enemy.x.toFloat, enemy.y.toFloat, game)
    case "FlameUp" | "FlameLeft" | "FlameRight"                   =>
      new Flame(
        enemy.x.toFloat,
        enemy.y.toFloat,
        Orientation.forName(enemy.kind.stripPrefix("Flame")),
        game
      )
    case "Mortar"                                                 =>
      new Mortar(enemy.x.toFloat, enemy.y.toFloat, enemy.algorithm, game)
    case "CannonUp" | "CannonDown" | "CannonLeft" | "CannonRight" =>
      new Cannon(
        enemy.x.toFloat,
        enemy.y.toFloat,
        Orientation.forName(enemy.kind.stripPrefix("Cannon")),
        game
      )
  }
}
