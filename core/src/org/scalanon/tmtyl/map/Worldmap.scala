package org.scalanon.tmtyl
package map

import com.badlogic.gdx.InputAdapter
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.PolygonSpriteBatch
import org.scalanon.tmtyl.game.Game
import org.scalanon.tmtyl.home.Home
import org.scalanon.tmtyl.{Scene, Vec2}

case class Worldmap(game: Game) extends Scene {
  import Worldmap._

  var playerLoc: Vec2 = Locations((game.currentLevel - 1) max 0)

  def init(): InputAdapter = {
    new WorldmapControl(this)
  }

  var tick  = 0f
  var eTick = 0f
  var done  = false
  var quit  = false

  def update(delta: Float): Option[Scene] = {
    tick += delta
    if (tick >= .2f) {
      val targetX = Locations(game.currentLevel).x
      val targetY = Locations(game.currentLevel).y
      if (playerLoc.x < targetX) playerLoc.x = playerLoc.x + 1 min targetX;
      if (playerLoc.x > targetX) playerLoc.x = playerLoc.x - 1 max targetX;
      if (playerLoc.y < targetY) playerLoc.y = playerLoc.y + 1 min targetY;
      if (playerLoc.y > targetY) playerLoc.y = playerLoc.y - 1 max targetY;
      tick = 0f
    }
    if (playerLoc == Locations(game.currentLevel)) {
      eTick += delta
    }
    done = done || playerLoc == Locations(game.currentLevel) && eTick >= .2f
    if (done) {
      game.levelUp()
      Some(game)
    } else if (quit) {
      Some(new Home)
    } else {
      None
    }
  }

  def render(batch: PolygonSpriteBatch): Unit = {
    batch.setColor(Color.WHITE)
    batch.draw(
      worldMap,
      0,
      -(Geometry.ScreenWidth - Geometry.ScreenHeight) / 2,
      Geometry.ScreenWidth,
      Geometry.ScreenWidth
    )
    batch.draw(
      worldmapPlayer,
      (playerLoc.x * Geometry.ScreenWidth / 100) - (Geometry.ScreenWidth / 20),
      (playerLoc.y * Geometry.ScreenHeight / 100) - (Geometry.ScreenHeight / 20),
      Geometry.ScreenWidth / 10,
      Geometry.ScreenWidth / 10
    )
  }
}

object Worldmap {
  val Locations: List[Vec2] = List(
    Vec2(9, 34),
    Vec2(23, 42),
    Vec2(41, 43),
    Vec2(50, 37),
    Vec2(63, 43),
    Vec2(80, 50),
    Vec2(89, 54)
  )

  def worldMap       = AssetLoader.image("worldmap.png")
  def worldmapPlayer = AssetLoader.image("globalUFO.png")

}
