package org.scalanon.tmtyl
package game.actors

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.PolygonSpriteBatch
import com.badlogic.gdx.math.MathUtils
import org.scalanon.tmtyl.game.Game

// TODO: unified projectile...
class Shell(
    x: Float,
    y: Float,
    vx: Float,
    vy: Float,
    game: Game
) extends Actor {
  import Shell._

  private val width  = image.width
  private val height = image.height

  private val pos  = Vec2(x, y)
  private val vel  = Vec2(vx, vy)
  private var oldY = y

  def update(delta: Float): List[Actor] = {
    vel.y -= Gravity * delta
    pos.mulAdd(vel, delta)
    if (pos.y + height < 0) {
      Nil
    } else {
      val velAngle = MathUtils.atan2(vel.y, vel.x).degrees
      val tipX     = pos.x + width / 2f * MathUtils.cosDeg(velAngle)
      val tipY     = pos.y + width / 2f * MathUtils.sinDeg(velAngle)
      game.entities.floors.find(floor =>
        floor.top <= game.player.bottom && tipY < floor.top && oldY >= floor.top && tipX + MissileBreadth / 2 >= floor.left && tipX - MissileBreadth / 2 < floor.right
      ) match {
        case Some(floor) =>
          boom.play(pos.x + game.translateX)
          val y       = floor.top
          val hitRect = game.player.hitRect()
          // explosion range is about your height
          val range   = hitRect.height
          if (
            hitRect.y >= y && hitRect.y < y + range && hitRect.x + hitRect.width >= tipX - range / 2 && hitRect.x < tipX + range / 2
          ) {
            game.player.die()
          }
          List(Explosion(tipX, y))
        case _           =>
          oldY = tipY
          List(this)
      }
    }
  }

  def draw(batch: PolygonSpriteBatch): Unit = {
    batch.setColor(Color.WHITE)
    val velocityAngle = MathUtils.atan2(vel.y, vel.x).degrees
    batch.draw(
      image,
      (pos.x - width / 2f) * Geometry.ScreenPixel,
      (pos.y - width / 2f) * Geometry.ScreenPixel,
      width * Geometry.ScreenPixel / 2f,
      height * Geometry.ScreenPixel / 2f,
      width.toFloat * Geometry.ScreenPixel,
      height.toFloat * Geometry.ScreenPixel,
      1f,
      1f,
      velocityAngle,
      0,
      0,
      width,
      height,
      false,
      false
    )
  }
}

object Shell {
  val Gravity        = 180f
  val MissileBreadth = 4f

  private def image = AssetLoader.image("shell.png")
  private def boom  = AssetLoader.sound("boom.mp3")
}
