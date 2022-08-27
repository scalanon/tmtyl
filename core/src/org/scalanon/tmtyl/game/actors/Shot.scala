package org.scalanon.tmtyl
package game.actors

import com.badlogic.gdx.graphics.g2d.PolygonSpriteBatch
import org.scalanon.tmtyl.Tmtyl.screenPixel
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
    val screenX = (loc.x + game.translateX) * screenPixel
    val screenY = loc.y * screenPixel
    val gone =
      screenX < 0 || screenX > Geometry.ScreenWidth || screenY < 0 || screenY > Geometry.ScreenHeight
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
    Rect(loc.x - width / 2, loc.y - height / 2, width, height)

  override def draw(batch: PolygonSpriteBatch): Unit = {
    batch.draw(
      image,
      (loc.x - width / 2) * screenPixel,
      (loc.y - height / 2) * screenPixel,
      width * screenPixel,
      height * screenPixel
    )
  }
}

object Shot {
  private def image = AssetLoader.image("shot.png")
}
