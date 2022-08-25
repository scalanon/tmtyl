package org.scalanon.tmtyl
package game.actors

import com.badlogic.gdx.graphics.g2d.PolygonSpriteBatch
import org.scalanon.tmtyl.Actor
import org.scalanon.tmtyl.Tmtyl.screenPixel
import org.scalanon.tmtyl.game.{Game, Rect}

class Flame(x: Float, y: Float, game: Game) extends Actor {
  import Flame._

  private val flameWidth  = flame.width / FlameFrames
  private val flameHeight = flame.height

  private val fireWidth  = fire.width / FireFrames
  private val fireHeight = fire.height

  // is wider than a tile
  private val fireOffset  = (16 - fireWidth) / 2
  private val flameOffset = fireOffset + (fireWidth - flameWidth) / 2

  var age     = 0f
  var flaming = false

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
    val deathBox =
      if (flaming && age < DeathlyFrames * FrameRate)
        Rect(x + fireOffset, y, fireWidth, fireHeight)
      else Rect(x + flameOffset, y, flameWidth, flameHeight)
    if (game.player.hitRect().intersects(deathBox)) {
      game.player.die()
    }
    List(this)
  }

  override def draw(batch: PolygonSpriteBatch): Unit = {
    val flameFrame = (age / FrameRate).toInt % FlameFrames
    batch.draw(
      flame,
      (x + flameOffset) * screenPixel,
      y * screenPixel,
      flameWidth * screenPixel,
      flameHeight * screenPixel,
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
        (x + fireOffset) * screenPixel,
        y * screenPixel,
        fireWidth * screenPixel,
        fireHeight * screenPixel,
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
