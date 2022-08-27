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

  private var angle = 0f

  override def update(delta: Float): List[Actor] = {
    val deltaX               = game.player.centerX - launchX
    val deltaY               = game.player.headY - launchY
    val targetAngle          = MathUtils.atan2(deltaY, deltaX).degrees
    val deltaFromOrientation =
      (targetAngle - orientation.degrees.toFloat).onCircle
    val clampCorrection      =
      if (deltaFromOrientation < -RotationLimit)
        -RotationLimit - deltaFromOrientation
      else if (deltaFromOrientation > RotationLimit)
        RotationLimit - deltaFromOrientation
      else 0f
    val desiredAngle         = targetAngle + clampCorrection
    val a1 = (angle + orientation.degrees + 180).onCircle
    val a2 = (desiredAngle + orientation.degrees + 180).onCircle

    angle = angle + (desiredAngle - angle).clamp(
      -RotationSpeed * delta,
      RotationSpeed * delta
    )
    val shotOpt              = MathUtils
      .randomBoolean(delta * FireChance)
      .option(
        new Shot(
          launchX,
          launchY,
          ShotSpeed * MathUtils.cosDeg(angle),
          ShotSpeed * MathUtils.sinDeg(angle),
          game
        )
      )
    shotOpt.cata(shot => List(shot, this), List(this))
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
  val FireChance    = 1f
  val History       = .9f
  val RotationLimit = 105f
  val RotationSpeed = 100f
  val ShotSpeed     = 90f

  private def base   = AssetLoader.image("cannon.png")
  private def barrel = AssetLoader.image("cannonBarrel.png")
}
