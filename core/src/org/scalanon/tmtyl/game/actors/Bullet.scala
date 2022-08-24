package org.scalanon.tmtyl
package game.actors

import com.badlogic.gdx.graphics.g2d.PolygonSpriteBatch
import org.scalanon.tmtyl.Tmtyl.screenPixel
import org.scalanon.tmtyl.game.Game
import org.scalanon.tmtyl.{Actor, AssetLoader, Geometry, Vec2}

class Bullet(x: Float, y: Float, left: Boolean, game: Game) extends Actor {
  import Bullet._

  val loc = new Vec2(x, y)
  val vel = new Vec2(if (left) -Speed else Speed, 0)

  private val width  = image.width
  private val height = image.height

  override def update(delta: Float): List[Actor] = {
    loc.mulAdd(vel, delta)
    val scale = left.fold(-screenPixel, screenPixel)
    val gone  =
      (loc.x + width / 2 - game.player.centerX) * scale > Geometry.ScreenWidth / 2
    if (gone) {
      Nil
    } else {
      List(this)
    }
  }

  override def draw(batch: PolygonSpriteBatch): Unit = {
    batch.draw(
      image,
      loc.x * screenPixel,
      loc.y * screenPixel,
      width * screenPixel,
      height * screenPixel
    )
  }
}

object Bullet {
  val Speed = 60f

  private def image = AssetLoader.image("target.png")
}
