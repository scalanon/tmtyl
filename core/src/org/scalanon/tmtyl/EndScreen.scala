package org.scalanon.tmtyl

import com.badlogic.gdx.InputAdapter
import com.badlogic.gdx.graphics.g2d.PolygonSpriteBatch
import com.badlogic.gdx.graphics.{Color, Texture}
import org.scalanon.tmtyl.game.Game
import org.scalanon.tmtyl.home.Home

case class EndScreen(game: Game) extends Scene {
  import EndScreen._

  def init(): InputAdapter = {
    new EndScreenControl(this)
  }

  var quit = false

  def update(delta: Float): Option[Scene] = {
    if (quit) {
      Some(new Home)
    } else {
      None
    }
  }

  def render(batch: PolygonSpriteBatch): Unit = {
    batch.setColor(Color.WHITE)
    batch.draw(
      endScreen,
      0,
      0,
      Geometry.ScreenWidth,
      Geometry.ScreenHeight
    )

  }
}

object EndScreen {
  var stage = 0

  def endScreen = AssetLoader.image("whic.png")

}
