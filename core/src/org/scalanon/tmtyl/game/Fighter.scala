package org.scalanon.tmtyl.game

import com.badlogic.gdx.graphics.g2d.PolygonSpriteBatch
import org.scalanon.tmtyl.AssetLoader

class Fighter {
  import Fighter._

  private val width  = image.width / Frames
  private val height = image.height / Sheets

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
      image,
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

  private def image = AssetLoader.image("alma.png")
}

object Fighter {
  val Frames = 5
  val Sheets = 18

  val FrameRate = .1f

  object Sheet {
    val Idle  = 0
    val Shoot = 8
  }
}
