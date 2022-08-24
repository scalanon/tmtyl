package org.scalanon.tmtyl.game.actors

import com.badlogic.gdx.graphics.g2d.PolygonSpriteBatch
import com.badlogic.gdx.math.MathUtils
import org.scalanon.tmtyl.Tmtyl.screenPixel
import org.scalanon.tmtyl.game.Game
import org.scalanon.tmtyl.{Actor, AssetLoader, Geometry}

class Fighter(x: Float, y: Float, game: Game) extends Actor {
  import Fighter._

  private val width  = idle.width / Frames
  private val height = idle.height / Sheets

  var frame       = 0
  var sheet       = Sheet.Idle
  var time        = 0f
  var left        = false
  var firingDelay = MathUtils.random(FiringDelayMax, FiringDelayMax)

  def update(delta: Float): List[Actor] = {
    time = time + delta
    frame = (time / FrameRate).toInt
    sheet = (time / FrameRate / 5).toInt % Fighter.Sheets
    left = game.player.centerX <= x + width / 2
    firingDelay = firingDelay - delta
    var shoot = false
    if (firingDelay < 0) {
      shoot = game.player.bottom < y + height && game.player.top >= y &&
        (x + width.toFloat / 2 - game.player.centerX).abs * screenPixel < Geometry.ScreenWidth * .625f
      firingDelay = MathUtils.random(FiringDelayMax, FiringDelayMax)
    }
    if (shoot) {
      gunshot.play(x - game.player.centerX)
      List(
        this,
        new Bullet(
          if (left) x + BulletOffsetX else x + width - BulletOffsetY,
          y + BulletOffsetY,
          left,
          game
        )
      )
    } else {
      List(this)
    }
  }

  def draw(
      batch: PolygonSpriteBatch
  ): Unit = {
    val fr = frame % Frames
    batch.draw(
      idle,
      x * screenPixel,
      y * screenPixel,
      width * screenPixel,
      height * screenPixel,
      fr * width,
      sheet * height,
      width,
      height,
      left,
      false
    )
  }
}

object Fighter {
  val Frames        = 5
  val Sheets        = 1
  val BulletOffsetX = 4f
  val BulletOffsetY = 10f
  val FiringDelayMin = 1f
  val FiringDelayMax = 3f

  val FrameRate = .1f

  object Sheet {
    val Idle  = 0
    val Shoot = 8
  }

  private def idle = AssetLoader.image("Gunner_Blue_Idle.png")
  private def run  = AssetLoader.image("Gunner_Blue_Run.png")

  private def gunshot  = AssetLoader.sound("gunshot.mp3")
}
