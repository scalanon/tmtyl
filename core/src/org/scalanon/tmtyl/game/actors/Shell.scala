package org.scalanon.tmtyl
package game.actors

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.PolygonSpriteBatch
import com.badlogic.gdx.math.MathUtils
import org.scalanon.tmtyl.game.Game

// TODO: unified projectile...
final case class Shell(
    x: Float,
    y: Float,
    vx: Float,
    vy: Float,
    game: Game
) extends Actor {
  import Shell._
  import Tmtyl.screenPixel

  private val width  = image.width
  private val height = image.height

  private val pos  = Vec2(x, y)
  private val vel  = Vec2(vx, vy)
  private var oldY = y

  def update(delta: Float): List[Actor] = {
    vel.y -= Gravity * delta
    pos.mulAdd(vel, delta)
    val velocityAngle = MathUtils.atan2(vel.y, vel.x).degrees
    val tipX          = pos.x + width / 2f * MathUtils.cosDeg(velocityAngle)
    val tipY          = pos.y + width / 2f * MathUtils.sinDeg(velocityAngle)
    if (pos.y + height < 0) {
      Nil
    } else if (
      game.player.aboveFloor.exists(floor =>
        tipY < floor.top && oldY >= floor.top && tipX + MissileBreadth / 2 >= floor.left && tipX - MissileBreadth / 2 < floor.right
      )
    ) {
      boom.play(pos.x + game.translateX)
      val y       = game.player.aboveFloor.cata(_.top, 0)
      val hitRect = game.player.hitRect()
      // explosion range is about your height
      val range   = hitRect.height
      if (
        hitRect.y >= y && hitRect.y < y + range && hitRect.x + hitRect.width >= tipX - range / 2 && hitRect.x < tipX + range / 2
      ) {
        game.player.die()
      }
      List(Explosion(tipX, y))
    } else {
      oldY = tipY
      List(this)
    }
  }

  def draw(batch: PolygonSpriteBatch): Unit = {
    batch.setColor(Color.WHITE)
    val velocityAngle = MathUtils.atan2(vel.y, vel.x).degrees
    batch.draw(
      image,
      (pos.x - width / 2f) * screenPixel,
      (pos.y - width / 2f) * screenPixel,
      width * screenPixel / 2f,
      height * screenPixel / 2f,
      width.toFloat * screenPixel,
      height.toFloat * screenPixel,
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
  val LaunchVelocity = (250f, 350f)
  val MissileBreadth = 4f

  private def image = AssetLoader.image("shell.png")
  private def boom  = AssetLoader.sound("boom.mp3")
  private def fire  = AssetLoader.sound("mortar.mp3")

  def fire(x: Float, y: Float, game: Game): Option[Shell] = {
    game.player.aboveFloor.flatMap(floor => {
      val vY        = MathUtils.random(LaunchVelocity._1, LaunchVelocity._2)
      val targetY   = floor.top.toFloat - y
      val quadratic = vY * vY - 2f * Gravity * targetY
      (quadratic > 0).option({
        // When will we hit the player's Y
        val t       = (vY + Math.sqrt(quadratic)).toFloat / Gravity
        // Where will the player be
        val playerX = game.player.centerX + game.player.vel.x * t
        // What horizontal velocity will get us there, within limits
        val vX      = ((playerX - x) / t).clamp(-vY / 2, vY / 2)

        fire.play(x + game.translateX)
        Shell(
          x,
          y,
          vX,
          vY,
          game
        )
      })
    })
  }
}
