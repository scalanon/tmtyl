package org.scalanon.tmtyl
package game.actors

import com.badlogic.gdx.graphics.g2d.PolygonSpriteBatch
import com.badlogic.gdx.math.MathUtils
import org.scalanon.tmtyl.Actor
import org.scalanon.tmtyl.game.Game

class Drone(x: Float, game: Game) extends Actor {
  import Drone._

  override def update(delta: Float): List[Actor] = {
    val launch =
      game.player.right >= x && game.player.left < x + Geometry.ScreenWidth / Geometry.ScreenPixel && MathUtils
        .randomBoolean(FireChance * delta)
    launch
      .flatOption(Missile.launch(x, game))
      .cata(missile => List(this, missile), List(this))
  }

  override def draw(batch: PolygonSpriteBatch): Unit = ()
}

object Drone {
  val FireChance = .5f
}
