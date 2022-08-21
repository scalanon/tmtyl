package org.scalanon.tmtyl

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.PolygonSpriteBatch
import org.scalanon.tmtyl.Tmtyl._

case class Tile(loc: Vec2, state: tileState) {
  def draw(batch: PolygonSpriteBatch): Unit = {
    state.draw(batch, this)
  }

  def xIn(player: Player): Boolean = {

    if (player.loc.x + player.size.x > loc.x && player.loc.x < loc.x + 1) {
      true
    } else false
  }
  def yIn(player: Player): Boolean = {
    if (player.loc.y + player.size.y > loc.y && player.loc.y < loc.y + 1) {
      true
    } else false

  }

}

sealed trait tileState { def draw(batch: PolygonSpriteBatch, tile: Tile): Unit }
object tileState {
  case object Metal extends tileState {
    def draw(batch: PolygonSpriteBatch, tile: Tile): Unit = {
      batch.setColor(Color.YELLOW)
      batch.draw(
        square,
        tile.loc.x * screenUnit,
        tile.loc.y * screenUnit,
        screenUnit,
        screenUnit
      )
    }
  }
}
