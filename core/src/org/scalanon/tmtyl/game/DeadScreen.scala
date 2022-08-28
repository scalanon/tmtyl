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
      0,
      0,
      Geometry.ScreenWidth,
      Geometry.ScreenHeight,
      1 * 22,
      6 * 17,
      22,
      17,
      false,
      false
    )
  }
}

object DeadScreen {
  def doofus = AssetLoader.image("tiny_adventurer_sheet.png")
}
