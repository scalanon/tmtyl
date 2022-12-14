package org.scalanon.tmtyl
package game.actors

import com.badlogic.gdx.graphics.g2d.PolygonSpriteBatch
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
  var firingClock = random(FiringRate)

  def update(delta: Float): List[Actor] = {
    time = time + delta
    frame = (time / FrameRate).toInt
    sheet = (time / FrameRate / 5).toInt % Fighter.Sheets
    left = game.player.centerX <= x + width / 2
    firingClock = firingClock - delta
    var shoot = false
    if (firingClock < 0) {
      shoot = game.player.bottom < y + height && game.player.top >= y &&
        (x + width.toFloat / 2 - game.player.centerX).abs * Geometry.ScreenPixel < Geometry.ScreenWidth * .625f
      firingClock = random(FiringRate)
    }
    if (shoot) {
      gunshot.play(x + game.translateX)
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
      x * Geometry.ScreenPixel,
      y * Geometry.ScreenPixel,
      width * Geometry.ScreenPixel,
      height * Geometry.ScreenPixel,
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
  val FiringRate    = (1f, 3f)

  val FrameRate = .1f

  object Sheet {
    val Idle  = 0
    val Shoot = 8
  }

  private def idle = AssetLoader.image("Gunner_Blue_Idle.png")
  private def run  = AssetLoader.image("Gunner_Blue_Run.png")

  private def gunshot = AssetLoader.sound("gunshot.mp3")
}
