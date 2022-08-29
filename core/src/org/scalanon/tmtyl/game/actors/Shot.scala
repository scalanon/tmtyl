package org.scalanon.tmtyl
package game.actors

import com.badlogic.gdx.graphics.g2d.PolygonSpriteBatch
import org.scalanon.tmtyl.game.{Game, Rect}
import org.scalanon.tmtyl.{Actor, AssetLoader, Geometry, Vec2}

class Shot(x: Float, y: Float, vX: Float, vY: Float, game: Game) extends Actor {
  import Shot._

  val loc = new Vec2(x, y)
  val vel = new Vec2(vX, vY)

  private val width  = image.width
  private val height = image.height

  override def update(delta: Float): List[Actor] = {
    loc.mulAdd(vel, delta)
    val screenX = (loc.x + game.translateX) * Geometry.ScreenPixel
    val hitRect = Rect(loc.x + 1, loc.y + 1, width - 2, height - 2)
    val gone    =
      (vel.x < 0 && screenX < 0) || (vel.x > 0 && screenX > Geometry.ScreenWidth) || loc.y < 0 || loc.y > game.level.height || game.entities.floors
        .exists(floor => floor.solid && hitRect.intersects(floor))
    if (gone) {
      Nil
    } else if (shotRect.within(game.player.hitRect())) {
      game.player.die()
      List(Splatter(loc.x, loc.y))
    } else {
      List(this)
    }
  }

  private def shotRect =
    Rect(loc.x - width / 2 + 1, loc.y - height / 2 + 1, width - 2, height - 2)

  override def draw(batch: PolygonSpriteBatch): Unit = {
    batch.draw(
      image,
      (loc.x - width / 2) * Geometry.ScreenPixel,
      (loc.y - height / 2) * Geometry.ScreenPixel,
      width * Geometry.ScreenPixel,
      height * Geometry.ScreenPixel
    )
  }
}

object Shot {
  val Xtra          = 16f
  private def image = AssetLoader.image("shot.png")
}
