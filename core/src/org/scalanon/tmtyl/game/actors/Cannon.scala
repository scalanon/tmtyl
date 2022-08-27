package org.scalanon.tmtyl
package game.actors

import com.badlogic.gdx.graphics.g2d.PolygonSpriteBatch
import com.badlogic.gdx.math.MathUtils
import org.scalanon.tmtyl.Actor
import org.scalanon.tmtyl.Tmtyl.screenPixel
import org.scalanon.tmtyl.game.Game
import org.scalanon.tmtyl.util.Orientation

class Cannon(x: Float, y: Float, orientation: Orientation, game: Game)
    extends Actor {
  import Cannon._

  private val width   = base.width
  private val height  = base.height
  private val launchX = x + width / 2
  private val launchY = y + height / 2

  private var shotClock = random(ShotRate)
  private var angle     = orientation.degrees.toFloat

  override def update(delta: Float): List[Actor] = {
    shotClock = shotClock - delta
    val deltaX = game.player.centerX - launchX
    val deltaY = game.player.headY - launchY

    // figure out a target angle constrained by the rotation limit
    val targetAngle          = MathUtils.atan2(deltaY, deltaX).degrees
    val deltaFromOrientation =
      (targetAngle - orientation.degrees.toFloat).onCircle
    val clampCorrection      =
      if (deltaFromOrientation < -RotationLimit)
        -RotationLimit - deltaFromOrientation
      else if (deltaFromOrientation > RotationLimit)
        RotationLimit - deltaFromOrientation
      else 0f
    val clampedAngle         = targetAngle + clampCorrection

    // Rotate to target angle without rotating through the base
    val srcAngle   = (angle + orientation.degrees).onCircle
    val dstAngle   =
      (clampedAngle + orientation.degrees).onCircle
    val deltaAngle =
      if ((srcAngle < 0 && dstAngle > 0) || (srcAngle > 0 && dstAngle < 0))
        srcAngle - dstAngle
      else dstAngle - srcAngle
    angle = angle + deltaAngle.clamp(
      -RotationSpeed * delta,
      RotationSpeed * delta
    )

    if (shotClock <= 0) {
      gunshot.play(x + game.translateX)
      val shot = new Shot(
        launchX,
        launchY,
        ShotSpeed * MathUtils.cosDeg(angle),
        ShotSpeed * MathUtils.sinDeg(angle),
        game
      )
      shotClock = random(ShotRate)
      List(shot, this)
    } else {
      List(this)
    }
  }

  override def draw(batch: PolygonSpriteBatch): Unit = {
    batch.draw(
      barrel,
      x * screenPixel,
      y * screenPixel,
      width / 2 * screenPixel,
      height / 2 * screenPixel,
      width * screenPixel,
      height * screenPixel,
      1f,
      1f,
      angle,
      0,
      0,
      width,
      height,
      false,
      false
    )
    batch.draw(
      base,
      x * screenPixel,
      y * screenPixel,
      width / 2 * screenPixel,
      height / 2 * screenPixel,
      width * screenPixel,
      height * screenPixel,
      1f,
      1f,
      orientation.degrees - 90,
      0,
      0,
      width,
      height,
      false,
      false
    )
  }
}

object Cannon {
  val History       = .9f
  val RotationLimit = 105f
  val RotationSpeed = 100f
  val ShotSpeed     = 90f
  val ShotRate      = (.3f, 1f)

  private def base    = AssetLoader.image("cannon.png")
  private def barrel  = AssetLoader.image("cannonBarrel.png")
  private def gunshot = AssetLoader.sound("gunshot.mp3")
}
