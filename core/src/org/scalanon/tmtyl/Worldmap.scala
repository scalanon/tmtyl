package org.scalanon.tmtyl
import com.badlogic.gdx.InputAdapter
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.PolygonSpriteBatch
import org.scalanon.tmtyl.Tmtyl._
import org.scalanon.tmtyl.game.Game

case class Worldmap(game: Game) extends Scene {
  var locs: List[Vec2] = List(
    Vec2(9, 34),
    Vec2(23, 42),
    Vec2(41, 43),
    Vec2(50, 37),
    Vec2(63, 43),
    Vec2(80, 50),
    Vec2(89, 54)
  )
  var playerLoc: Vec2  = locs((game.currentLevel - 1) max 0)

  def init(): InputAdapter = {
    new WorldmapControl(this)
  }
  var tick  = 0f
  var eTick = 0f
  def update(delta: Float): Option[Scene] = {
    tick += delta
    if (tick >= .2f) {
      val targetX = locs(game.currentLevel).x
      val targetY = locs(game.currentLevel).y
      if (playerLoc.x < targetX) playerLoc.x = playerLoc.x + 1 min targetX;
      if (playerLoc.x > targetX) playerLoc.x = playerLoc.x - 1 max targetX;
      if (playerLoc.y < targetY) playerLoc.y = playerLoc.y + 1 min targetY;
      if (playerLoc.y > targetY) playerLoc.y = playerLoc.y - 1 max targetY;
      tick = 0f
    }
    if (playerLoc == locs(game.currentLevel)) {
      eTick += delta
    }
    if (playerLoc == locs(game.currentLevel) && eTick >= .2f) {
      Some(game)
    } else {
      None
    }
  }

  def render(batch: PolygonSpriteBatch): Unit = {
    batch.setColor(Color.WHITE)
    batch.draw(worldMap, 0, 0, Geometry.ScreenWidth, Geometry.ScreenHeight)
    batch.draw(
      worldmapPlayer,
      (playerLoc.x * Geometry.ScreenWidth / 100) - (Geometry.ScreenWidth / 20),
      (playerLoc.y * Geometry.ScreenHeight / 100) - (Geometry.ScreenHeight / 20),
      Geometry.ScreenWidth / 10,
      Geometry.ScreenHeight / 10
    )

  }
}
