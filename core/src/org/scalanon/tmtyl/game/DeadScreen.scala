package org.scalanon.tmtyl
package game

import com.badlogic.gdx.InputAdapter
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.PolygonSpriteBatch
import org.scalanon.tmtyl.home.Home

case class DeadScreen(game: Game) extends Scene {
  import DeadScreen._

  var quit  = false
  var retry = false

  def init(): InputAdapter = {
    new DeadScreenControl(this)
  }

  def update(delta: Float): Option[Scene] = {
    if (quit) {
      Some(new Home)
    } else if (retry) {
      Some(new Game(game.currentLevel))
    } else {
      None
    }
  }

  def render(batch: PolygonSpriteBatch): Unit = {
    batch.setColor(Color.WHITE)
    batch.draw(
      doofus,
      (Geometry.ScreenWidth - Geometry.ScreenHeight) / 2,
      0,
      Geometry.ScreenHeight,
      Geometry.ScreenHeight
    )
  }
}

object DeadScreen {
  def doofus = AssetLoader.image("Rip.png")
}
