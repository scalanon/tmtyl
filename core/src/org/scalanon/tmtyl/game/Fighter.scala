package org.scalanon.tmtyl.game

import com.badlogic.gdx.graphics.g2d.PolygonSpriteBatch
import org.scalanon.tmtyl.Tmtyl

class Fighter {
  var frame = 0
  var sheet = Fighter.Sheet.Idle

  var time = 0f

  def update(delta: Float): Unit = {
    time = time + delta
    frame = (time / Fighter.Rate).toInt
    sheet = (time / Fighter.Rate / 5).toInt % Fighter.Sheets
  }

  def draw(
      x: Float,
      y: Float,
      pixel: Float,
      batch: PolygonSpriteBatch
  ): Unit = {
    val fr = frame % Fighter.Frames
    batch.draw(
      Tmtyl.alma,
      x,
      y,
      pixel * Fighter.Width,
      pixel * Fighter.Height,
      fr * Fighter.Width,
      sheet * Fighter.Height,
      Fighter.Width,
      Fighter.Height,
      false,
      false
    )

  }

}

object Fighter {
  val Width = 48
  val Height = 48
  val Frames = 5
  val Sheets = 17

  val Rate = .1f

  object Sheet {
    val Idle = 0
    val Shoot = 8
  }
}
