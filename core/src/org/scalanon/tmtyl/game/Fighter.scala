package org.scalanon.tmtyl.game

import com.badlogic.gdx.graphics.g2d.PolygonSpriteBatch
import org.scalanon.tmtyl.AssetLoader

class Fighter {
  import Fighter._

  private val width  = idle.width / Frames
  private val height = idle.height / Sheets

  var frame = 0
  var sheet = Sheet.Idle
  var time  = 0f

  def update(delta: Float): Unit = {
    time = time + delta
    frame = (time / FrameRate).toInt
    sheet = (time / FrameRate / 5).toInt % Fighter.Sheets
  }

  def draw(
      x: Float,
      y: Float,
      pixel: Float,
      batch: PolygonSpriteBatch
  ): Unit = {
    val fr = frame % Frames
    batch.draw(
      idle,
      x,
      y,
      pixel * width,
      pixel * height,
      fr * width,
      sheet * height,
      width,
      height,
      false,
      false
    )

  }

  private def idle = AssetLoader.image("Gunner_Blue_Idle.png")
  private def run  = AssetLoader.image("Gunner_Blue_Run.png")
}

object Fighter {
  val Frames = 5
  val Sheets = 1

  val FrameRate = .1f

  object Sheet {
    val Idle  = 0
    val Shoot = 8
  }
}
