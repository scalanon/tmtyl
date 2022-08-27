package org.scalanon.tmtyl
package game.actors

import com.badlogic.gdx.graphics.g2d.PolygonSpriteBatch
import com.badlogic.gdx.math.MathUtils
import org.scalanon.tmtyl.Actor
import org.scalanon.tmtyl.Tmtyl.screenPixel
import org.scalanon.tmtyl.game.Game

class Mortar(x: Float, y: Float, game: Game) extends Actor {
  import Mortar._
  import Shell.Gravity

  private val width  = base.width
  private val height = base.height

  private var playerVelocityX =
    0f // player horizontal velocity smoothed over time
  private var launchVelocityY =
    MathUtils.random(LaunchVelocityY._1, Mortar.LaunchVelocityY._2)
  private var rotation        = 0f

  override def update(delta: Float): List[Actor] = {
    playerVelocityX =
      playerVelocityX * History + game.player.vel.x * (1f - History)
    val shellOpt = for {
      floor           <- game.player.aboveFloor
      if game.player.right >= x - LaunchRange && game.player.left < x + LaunchRange
      launchVelocityX <- computeVelocityX(floor.top.toFloat)
      _                = adjustRotation(launchVelocityX)
      if MathUtils.randomBoolean(FireChance * delta)

    } yield Shell.fire(
      x + width / 2,
      y + height / 2,
      launchVelocityX,
      launchVelocityY,
      game
    )
    shellOpt.cata(shell => List(this, shell), List(this))
  }

  private def adjustRotation(launchVelocityX: Float): Unit = {
    rotation =
      MathUtils.atan2(launchVelocityY, launchVelocityX) * MathUtils.radDeg - 90f
  }

  // Compute X velocity to hit a target at specified Y position
  private def computeVelocityX(targetY: Float): Option[Float] = {
    val deltaY    = targetY - y
    val quadratic = launchVelocityY * launchVelocityY - 2f * Gravity * deltaY
    (quadratic > 0).option({
      // When will we hit the player's Y
      val t             = (launchVelocityY + Math.sqrt(quadratic)).toFloat / Gravity
      // Where will the player be
      val futurePlayerX = game.player.centerX + playerVelocityX * t
      // What horizontal velocity will get us there, within limits
      ((futurePlayerX - x) / t).clamp(-launchVelocityY / 2, launchVelocityY / 2)
    })
  }

  override def draw(batch: PolygonSpriteBatch): Unit = {
    batch.draw( 
      base,
      x * screenPixel,
      y * screenPixel,
      width * screenPixel,
      height * screenPixel
    )
    batch.draw(
      barrel,
      x * screenPixel,
      y * screenPixel,
      width / 2 * screenPixel,
      BarrelBreadth / 2 * screenPixel,
      width * screenPixel,
      height * screenPixel,
      1f,
      1f,
      rotation,
      0,
      0,
      width,
      height,
      false,
      false
    )
  }
}

object Mortar {
  val FireChance      = 1f
  val History         = .9f
  val LaunchRange     = 5 * Geometry.ScreenWidth / screenPixel
  val LaunchVelocityY = (250f, 350f)
  val BarrelBreadth   = 8f

  private def base   = AssetLoader.image("mortar.png")
  private def barrel = AssetLoader.image("mortarBarrel.png")
}
