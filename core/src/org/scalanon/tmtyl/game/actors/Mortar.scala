package org.scalanon.tmtyl
package game.actors

import com.badlogic.gdx.graphics.g2d.PolygonSpriteBatch
import com.badlogic.gdx.math.MathUtils
import org.scalanon.tmtyl.Actor
import org.scalanon.tmtyl.Tmtyl.screenPixel
import org.scalanon.tmtyl.game.Game

class Mortar(x: Float, y: Float, game: Game) extends Actor {
  import Mortar._

  // TODO: the mortar should be a tube rendered at an angle that gradually rotates
  // to target angle etc.

  private val width = image.width
  private val height = image.height

  override def update(delta: Float): List[Actor] = {
    val launchRange = 5 * Geometry.ScreenWidth / screenPixel
    val doLaunch    =
      game.player.right >= x - launchRange && game.player.left < x + launchRange && MathUtils
        .randomBoolean(FireChance * delta)
    doLaunch
      .flatOption(Shell.fire(x + width / 2, y + height / 2, game))
      .cata(shell => List(shell, this), List(this))
  }

  override def draw(batch: PolygonSpriteBatch): Unit = {
    batch.draw(image, x * screenPixel, y * screenPixel, width * screenPixel, height * screenPixel)
  }
}

object Mortar {
  val FireChance = 1f

  private def image = AssetLoader.image("mortar.png")
}
