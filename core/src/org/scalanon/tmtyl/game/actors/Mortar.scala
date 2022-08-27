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

  private val width   = base.width
  private val height  = base.height
  private val launchX = x + width / 2
  private val launchY = y + height / 2

  // player horizontal velocity smoothed over time
  private var playerVelocityX = 0f
  private var launchVelocityY = random(Mortar.LaunchVelocityY)
  private var firingClock     = random(FiringRate)
  private var angle           = 0f

  override def update(delta: Float): List[Actor] = {
    firingClock -= delta
    playerVelocityX =
      playerVelocityX * History + game.player.vel.x * (1f - History)
    val shellOpt = for {
      floor           <- game.player.aboveFloor
      if game.player.right >= x - LaunchRange && game.player.left < x + LaunchRange
      launchVelocityX <- computeVelocityX(floor.top.toFloat)
      _                = adjustAngle(launchVelocityX)
      if firingClock < 0
    } yield {
      fire.play(launchX + game.translateX)
      firingClock = random(FiringRate)
      val velY = launchVelocityY
      launchVelocityY = random(Mortar.LaunchVelocityY)
      new Shell(
        launchX,
        launchY,
        launchVelocityX,
        velY,
        game
      )
    }
    shellOpt.cata(shell => List(shell, this), List(this))
  }

  private def adjustAngle(launchVelocityX: Float): Unit = {
    angle =
      MathUtils.atan2(launchVelocityY, launchVelocityX) * MathUtils.radDeg - 90f
  }

  // Compute X velocity to hit a target at specified Y position
  private def computeVelocityX(targetY: Float): Option[Float] = {
    val deltaY    = targetY - launchY
    val quadratic = launchVelocityY * launchVelocityY - 2f * Gravity * deltaY
    (quadratic > 0).option({
      // When will we hit the player's Y
      val t             = (launchVelocityY + Math.sqrt(quadratic)).toFloat / Gravity
      // Where will the player be
      val futurePlayerX = game.player.centerX + playerVelocityX * t
      // What horizontal velocity will get us there, within limits
      ((futurePlayerX - launchX) / t)
        .clamp(-launchVelocityY / 2, launchVelocityY / 2)
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
      angle,
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
  val History         = .9f
  val FiringRate      = (.25f, .75f)
  val LaunchRange     = 5 * Geometry.ScreenWidth / screenPixel
  val LaunchVelocityY = (250f, 350f)
  val BarrelBreadth   = 8f

  private def base   = AssetLoader.image("mortar.png")
  private def barrel = AssetLoader.image("mortarBarrel.png")
  private def fire   = AssetLoader.sound("mortar.mp3")
}
