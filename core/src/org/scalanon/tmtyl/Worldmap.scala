package org.scalanon.tmtyl
import com.badlogic.gdx.InputAdapter
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.PolygonSpriteBatch
import org.scalanon.tmtyl.Tmtyl._
import org.scalanon.tmtyl.game.Game

case class Worldmap(currentLevel: Int, game: Game) extends Scene {
  var locs: List[Vec2] = List(
    Vec2(9, 34),
    Vec2(23, 42),
    Vec2(41, 43),
    Vec2(50, 37),
    Vec2(63, 43),
    Vec2(80, 50),
    Vec2(89, 46)
  )
  var playerLoc: Vec2  = locs(currentLevel - 1)

  def init(): InputAdapter = {
    new WorldmapControl(this)
  }
  var tick  = 0f
  var eTick = 0f
  def update(delta: Float): Option[Scene] = {
    tick += delta
    if (tick >= .2f) {
      if (playerLoc.x < locs(currentLevel).x) playerLoc.x += 1;
      tick = 0f

      if (playerLoc.x > locs(currentLevel).x) playerLoc.x -= 1;
      tick = 0f

      if (playerLoc.y < locs(currentLevel).y) playerLoc.y += 1;
      tick = 0f

      if (playerLoc.y > locs(currentLevel).y) playerLoc.y -= 1;
      tick = 0f
    }
    if (playerLoc == locs(currentLevel)) {
      eTick += delta
    }
    if (playerLoc == locs(currentLevel) && eTick >= .2f) {
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
