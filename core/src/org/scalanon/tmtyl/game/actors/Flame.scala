package org.scalanon.tmtyl
package game.actors

import com.badlogic.gdx.graphics.g2d.PolygonSpriteBatch
import org.scalanon.tmtyl.Actor
import org.scalanon.tmtyl.game.{Game, Rect}
import org.scalanon.tmtyl.util.Orientation

class Flame(x: Float, y: Float, orientation: Orientation, game: Game)
    extends Actor {
  import Flame._

  private val flameWidth  = flame.width / FlameFrames
  private val flameHeight = flame.height

  private val fireWidth  = fire.width / FireFrames
  private val fireHeight = fire.height

  // is wider than a tile
  private val fireOffset  = (16 - fireWidth) / 2
  private val flameOffset = fireOffset + (fireWidth - flameWidth) / 2

  var age        = 0f
  var flaming    = false
  var flameFrame = 0

  override def update(delta: Float): List[Actor] = {
    age = age + delta
    // TODO: only do actor work if they're on screen.. drones excepted
    if (age >= flaming.fold(FireFrames * FrameRate, FlameInterval)) {
      flaming = !flaming
      age = 0
      if (flaming) {
        sound.play(x + game.translateX)
      }
    }
    flameFrame = (age / FrameRate).toInt min (FlameFrames - 1)
    val bigly    = flaming && flameFrame < DeathlyFrames
    val offset   = bigly.fold(fireOffset, flameOffset)
    val width    = bigly.fold(fireWidth, flameWidth)
    val height   =
      bigly.fold(fireHeight * 7 / 8 * (flameFrame + 1 min 4) / 4, flameHeight)
    val deathBox = orientation match {
      case Orientation.Up    => Rect(x + offset, y, width, height)
      case Orientation.Left  => Rect(x + 16 - height, y + offset, height, width)
      case Orientation.Right => Rect(x, y + offset, height, width)
    }
    if (game.player.hitRect().intersects(deathBox)) {
      game.player.die()
    }
    List(this)
  }

  val (flameX, flameY, fireX, fireY) = orientation match {
    case Orientation.Up    =>
      (x + flameOffset, y, x + fireOffset, y)
    case Orientation.Left  =>
      (x + 16, y + flameOffset, x + 16, y + fireOffset)
    case Orientation.Right =>
      (x, y + 16 - flameOffset, x, y + 16 - fireOffset)
  }
  val rotation                       = (orientation.degrees + 270) % 360

  override def draw(batch: PolygonSpriteBatch): Unit = {
    batch.draw(
      flame,
      flameX * Geometry.ScreenPixel,
      flameY * Geometry.ScreenPixel,
      0,
      0,
      flameWidth * Geometry.ScreenPixel,
      flameHeight * Geometry.ScreenPixel,
      1,
      1,
      rotation,
      flameWidth * flameFrame,
      0,
      flameWidth,
      flameHeight,
      false,
      false
    )
    if (flaming) {
      val fireFrame = (age / FrameRate).toInt min (FireFrames - 1)
      batch.draw(
        fire,
        fireX * Geometry.ScreenPixel,
        fireY * Geometry.ScreenPixel,
        0,
        0,
        fireWidth * Geometry.ScreenPixel,
        fireHeight * Geometry.ScreenPixel,
        1,
        1,
        rotation,
        fireWidth * fireFrame,
        0,
        fireWidth,
        fireHeight,
        false,
        false
      )
    }
  }
}

object Flame {
  val FlameFrames   = 4
  val FireFrames    = 14
  val DeathlyFrames = 10
  val FlameInterval = .5f
  val FrameRate     = .1f

  private def flame = AssetLoader.image("flame.png")
  private def fire  = AssetLoader.image("fire.png")
  private val sound = AssetLoader.sound("flame.mp3")
}
