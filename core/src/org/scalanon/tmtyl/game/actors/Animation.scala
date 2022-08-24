package org.scalanon.tmtyl.game.actors

import com.badlogic.gdx.graphics.g2d.PolygonSpriteBatch
import org.scalanon.tmtyl.Actor
import org.scalanon.tmtyl.Tmtyl.screenPixel
import org.scalanon.tmtyl.util.TextureWrapper

// TODO: a SpriteSheet TextureWrapper with frames and frameRate and...

abstract class Animation(
    x: Float,
    y: Float,
    originX: Float,
    originY: Float,
    frames: Int,
    frameRate: Float,
    image: => TextureWrapper
) extends Actor {
  private val width  = image.width / frames
  private val height = image.height
  private var age    = 0f

  def update(delta: Float): List[Actor] = {
    age += delta
    if (age < frames * frameRate) List(this) else Nil
  }

  def draw(batch: PolygonSpriteBatch): Unit = {
    val fr = (age / frameRate).toInt min frames - 1
    batch.draw(
      image,
      (x - width * originX) * screenPixel,
      (y - height * originY) * screenPixel,
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
}
