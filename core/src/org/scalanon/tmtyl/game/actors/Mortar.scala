package org.scalanon.tmtyl
package game.actors

import com.badlogic.gdx.graphics.g2d.PolygonSpriteBatch
import com.badlogic.gdx.math.MathUtils
import org.scalanon.tmtyl.Actor
import org.scalanon.tmtyl.Tmtyl.screenPixel
import org.scalanon.tmtyl.game.Game

class Mortar(x: Float, y: Float, alg: String, val game: Game) extends Actor {
  import Mortar._

  private val width   = base.width
  private val height  = base.height
  private val launchX = x + width / 2
  private val launchY = y + height / 2

  private val algorithm   =
    if (alg == "Spread")
      new SpreadTargeting(this)
    else new PredictiveTargeting(this)
  private var firingClock = random(algorithm.firingRate)
  private var angle       = 0f

  override def update(delta: Float): List[Actor] = {
    algorithm.update(delta)
    val shellOpt = if (inRange) {
      firingClock -= delta
      for {
        velocity <- algorithm.target()
        _         = adjustAngle(velocity)
        if firingClock < 0
      } yield {
        fire.play(launchX + game.translateX)
        firingClock = random(algorithm.firingRate)
        algorithm.reset()
        new Shell(
          launchX,
          launchY,
          velocity.x,
          velocity.y,
          game
        )
      }
    } else {
      None
    }
    shellOpt.cata(shell => List(shell, this), List(this))
  }

  private def inRange: Boolean =
    game.player.right >= launchX - LaunchRange && game.player.left < launchX + LaunchRange

  private def adjustAngle(velocity: Vec2): Unit = {
    angle = MathUtils.atan2(velocity.y, velocity.x) * MathUtils.radDeg - 90f
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
  val LaunchRange    = 6 * Geometry.ScreenWidth / screenPixel
  val LaunchVelocity = (250f, 350f)
  val BarrelBreadth  = 8f

  private def base   = AssetLoader.image("mortar.png")
  private def barrel = AssetLoader.image("mortarBarrel.png")
  private def fire   = AssetLoader.sound("mortar.mp3")

  trait TargetingAlgorithm {
    val firingRate: (Float, Float)
    def update(delta: Float): Unit
    // return the target launch vector
    def target(): Option[Vec2]
    // reset after firing
    def reset(): Unit
  }

  class PredictiveTargeting(mortar: Mortar) extends TargetingAlgorithm {
    import Shell.Gravity

    // decay of smoothed player velocity
    private final val History = .9f

    // player horizontal velocity smoothed over time
    private var playerVelocityX = 0f
    private var launchVelocityY = random(Mortar.LaunchVelocity)

    override val firingRate = (.25f, .75f)

    override def update(delta: Float): Unit = {
      playerVelocityX =
        playerVelocityX * History + mortar.game.player.vel.x * (1f - History)
    }

    // bogus because this launches at a fixed velocity Y rather than a total velocity within the desired ranch
    override def target(): Option[Vec2] =
      for {
        floor    <- mortar.game.player.aboveFloor
        targetY   = floor.top.toFloat
        deltaY    = targetY - mortar.launchY
        quadratic = launchVelocityY * launchVelocityY - 2f * Gravity * deltaY
        if quadratic >= 0
      } yield {
        // When will we hit the player's Y
        val t               = (launchVelocityY + Math.sqrt(quadratic)).toFloat / Gravity
        // Where will the player be
        val futurePlayerX   = mortar.game.player.centerX + playerVelocityX * t
        // What horizontal velocity will get us there, within limits
        val launchVelocityX = ((futurePlayerX - mortar.launchX) / t)
          .clamp(-launchVelocityY / 2, launchVelocityY / 2)
        Vec2(launchVelocityX, launchVelocityY)
      }

    override def reset(): Unit = {
      launchVelocityY = random(Mortar.LaunchVelocity)
    }
  }

  class SpreadTargeting(mortar: Mortar) extends TargetingAlgorithm {
    private final val RotateRate = 60f
    private final val AngleLimit = 15f

    private var time           = 0f
    private val launchVelocity =
      (Mortar.LaunchVelocity._1 + Mortar.LaunchVelocity._2) / 2

    override val firingRate = (2f, 3f)

    override def update(delta: Float): Unit = {
      time = time + delta
    }

    override def target(): Option[Vec2] = {
      val angle = 90f + AngleLimit * MathUtils.sinDeg(-time * RotateRate)
      Some(
        Vec2(
          launchVelocity * MathUtils.cosDeg(angle),
          launchVelocity * MathUtils.sinDeg(angle)
        )
      )
    }

    override def reset(): Unit = {}
  }
}
