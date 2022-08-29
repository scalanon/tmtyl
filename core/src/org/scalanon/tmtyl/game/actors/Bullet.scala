package org.scalanon.tmtyl
package game.actors

import com.badlogic.gdx.graphics.g2d.PolygonSpriteBatch
import org.scalanon.tmtyl.Geometry.ScreenPixel
import org.scalanon.tmtyl.game.{Game, Rect}
import org.scalanon.tmtyl.{Actor, AssetLoader, Geometry, Vec2}

class Bullet(x: Float, y: Float, left: Boolean, game: Game) extends Actor {
  import Bullet._

  val loc = new Vec2(x, y)
  val vel = new Vec2(if (left) -Speed else Speed, 0)

  private val width  = image.width
  private val height = image.height

  override def update(delta: Float): List[Actor] = {
    loc.mulAdd(vel, delta)
    val scale  = left.fold(-ScreenPixel, Geometry.ScreenPixel)
    val hitBox = Rect(loc.x, loc.y, width, height)
    val gone   =
      (loc.x + width / 2 - game.player.centerX) * scale > Geometry.ScreenWidth / 2 || game.entities.floors
        .exists(floor => floor.solid && hitBox.intersects(floor))
    if (gone) {
      Nil
    } else if (bulletRect.within(game.player.hitRect())) {
      game.player.die()
      List(Splatter(loc.x, loc.y))
    } else {
      List(this)
    }
  }

  private def bulletRect =
    Rect(loc.x - width / 2, loc.y - height / 2, width, height)

  override def draw(batch: PolygonSpriteBatch): Unit = {
    batch.draw(
      image,
      loc.x * Geometry.ScreenPixel,
      loc.y * Geometry.ScreenPixel,
      width * Geometry.ScreenPixel,
      height * Geometry.ScreenPixel,
      0,
      0,
      width,
      height,
      left,
      false
    )
  }
}

object Bullet {
  val Speed = 60f

  private def image = AssetLoader.image("bullet.png")
}
