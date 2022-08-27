package org.scalanon.tmtyl
package game.actors

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.PolygonSpriteBatch
import com.badlogic.gdx.math.MathUtils
import org.scalanon.tmtyl.game.{Floor, Game}

final case class Missile(
    x: Float,
    y: Float,
    targetX: Float,
    targetY: Float,
    floor: Floor,
    game: Game
) extends Actor {
  import Missile._
  import Tmtyl.screenPixel

  private val width  = image.width
  private val height = image.height

  // the thrust angle
  private var angle = -45f
  private val pos   = Vec2(x, y)
  private val vel   = Vec2(0, 0)
  private var oldY  = y

  def update(delta: Float): List[Actor] = {
    // the angle I'm moving, -180 to 180
    val velocityAngle         = MathUtils.atan2(vel.y, vel.x).degrees
    // the angle to my target, -180 to 180
    val targetAngle           = MathUtils.atan2(targetY - pos.y, targetX - pos.x).degrees
    // delta from the angle i'm facing to the target angle
    val angleToTarget         = (angle - targetAngle).onCircle
    // delta from the angle i'm moving to the target angle
    val velocityAngleToTarget = (velocityAngle - targetAngle).onCircle
    // if i'm way off, just aim for the target, else try to correct for velocity angle mismatch
    val deltaAngle            =
      if (angleToTarget.abs >= 45 || velocityAngleToTarget >= 45) angleToTarget
      else angleToTarget + velocityAngleToTarget
    angle =
      angle + delta * (if (deltaAngle < 0) Angeleration else -Angeleration)
    vel.mulAdd(
      MathUtils.cosDeg(angle),
      MathUtils.sinDeg(angle),
      delta * Acceleration
    )
    pos.mulAdd(vel, delta)
    val tipX                  = pos.x + width / 2f * MathUtils.cosDeg(velocityAngle)
    val tipY                  = pos.y + width / 2f * MathUtils.sinDeg(velocityAngle)
    if (pos.y + height < 0) {
      Nil
    } else if (
      tipY < targetY && oldY >= targetY && tipX + MissileBreadth / 2 >= floor.x && tipX - MissileBreadth / 2 < floor.x + floor.width
    ) {
      boom.play(pos.x + game.translateX)
      val y       = targetY
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
    batch.draw(
      target,
      (targetX - target.width / 2f) * screenPixel,
      (targetY - target.height / 2f) * screenPixel,
      target.width * screenPixel,
      target.height * screenPixel
    )
  }
}

object Missile {
  val Acceleration   = 50f
  val Angeleration   = 60f
  val MissileBreadth = 5f

  private def image  = AssetLoader.image("missile.png")
  private def target = AssetLoader.image("target.png")
  private def boom   = AssetLoader.sound("boom.mp3")

  def launch(x: Float, game: Game): Option[Missile] = {
    game.player.aboveFloor.map(floor =>
      Missile(
        x,
        MathUtils.random(400f, 500f),
        game.player.centerX
          .clamp(
            floor.x.toFloat,
            floor.x.toFloat + floor.width.toFloat - 1
          ),
        floor.y + floor.height,
        floor,
        game
      )
    )
  }

}
