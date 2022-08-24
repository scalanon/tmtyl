package org.scalanon.tmtyl
package game.actors

import com.badlogic.gdx.graphics.g2d.PolygonSpriteBatch

final case class Explosion(x: Float, y: Float) extends Actor {
  import Explosion._
  import Tmtyl.screenPixel

  private val width  = image.width / Frames
  private val height = image.height
  private var age    = 0f

  def update(delta: Float): List[Actor] = {
    age += delta
    if (age < Frames * FrameRate) List(this) else Nil
  }

  def draw(batch: PolygonSpriteBatch): Unit = {
    val fr = (age / FrameRate).toInt min Frames - 1
    batch.draw(
      image,
      (x - width / 2) * screenPixel,
      y * screenPixel,
      width * screenPixel,
      height * screenPixel,
      fr * width,
      0,
      width,
      height,
      false,
      false
    )
  }

  private def image = AssetLoader.image("explosion.png")
}

object Explosion {
  val Frames    = 10
  val FrameRate = .066f

}
