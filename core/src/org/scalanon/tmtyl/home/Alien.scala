package org.scalanon.tmtyl.home

import com.badlogic.gdx.graphics.g2d.PolygonSpriteBatch
import org.scalanon.tmtyl.AssetLoader

class Alien(var animation: AlienAnimation) {
  import Alien._

  val width  = image.width / Frames
  val height = image.height / Stages

  def update(delta: Float): Unit = {
    animation.update(delta)
  }

  def draw(
      x: Float,
      y: Float,
      pixel: Float,
      batch: PolygonSpriteBatch
  ): Unit = {
    val frame = animation.frame
    val state = animation.state
    val frmod = frame % state.loopFrames
    val fr    = if (frmod < state.frames) frmod else (state.frames - 1) * 2 - frmod
    batch.draw(
      image,
      x,
      y,
      pixel * width,
      pixel * height,
      fr * width,
      state.position * height,
      width,
      height,
      false,
      false
    )

  }

  private def image = AssetLoader.image("alien.png")
}

object Alien {
  // many are blank
  val Frames = 6
  val Stages = 6
}

sealed abstract class AlienState(val position: Int, val frames: Int) {
  def loopFrames: Int = if (frames == 1) 1 else (frames - 1) * 2
}

object AlienState {
  case object Idle  extends AlienState(0, 2)
  case object Walk  extends AlienState(1, 3)
  case object Blink extends AlienState(2, 3)
  case object Dead  extends AlienState(3, 3)
}

sealed trait AlienAnimation {
  def state: AlienState
  def frame: Int
  def update(delta: Float): Unit
}

object AlienAnimation {
  class Dead extends AlienAnimation {
    val state: AlienState = AlienState.Dead
    val frame: Int        = AlienState.Dead.frames - 1

    override def update(delta: Float): Unit = {}
  }

  class Reanimate extends AlienAnimation {
    val state: AlienState = AlienState.Dead
    var frame: Int        = state.frames - 1

    var time: Float = 0f

    override def update(delta: Float): Unit = {
      time = time + delta
      frame =
        (state.frames - 1 + (time / Reanimate.ReanimateRate).toInt) min state.loopFrames
    }

    def done: Boolean = frame >= state.loopFrames
  }

  object Reanimate {
    val ReanimateRate = .6f
  }

  class Idle extends AlienAnimation {
    var state: AlienState = AlienState.Idle
    var frame             = 0

    var time: Float    = 0f
    var blink: Boolean = false

    override def update(delta: Float): Unit = {
      if (blink) {
        time = time + delta
        frame = (time / Idle.BlinkRate).toInt
        if (frame >= state.loopFrames) {
          state = AlienState.Idle
          frame = 0
          blink = false
        }
      } else if (frame % state.loopFrames == 0 && time >= Idle.BlinkPeriod) {
        state = AlienState.Blink
        frame = 0
        time = 0f
        blink = true
      } else {
        time = time + delta
        frame = (time / Idle.IdleRate).toInt
      }
    }
  }

  object Idle {
    final val IdleRate    = 1f
    final val BlinkPeriod = 5f
    final val BlinkRate   = .1f
  }
}
