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
    val scale      = left.fold(-screenPixel, screenPixel)
    val gone       =
      (loc.x + width / 2 - game.player.centerX) * scale > Geometry.ScreenWidth / 2
    val playerRect = game.player.hitRect()
    // fully within
    val hit        =
      loc.x - width < playerRect.x + playerRect.width && loc.x >= playerRect.x && loc.y + height < playerRect.y + playerRect.height && loc.y >= playerRect.y
    if (hit) {
      game.player.die()
    }
    if (gone) {
      Nil
    } else if (hit) {
      List(Splatter(loc.x, loc.y))
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
      height * screenPixel,
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
