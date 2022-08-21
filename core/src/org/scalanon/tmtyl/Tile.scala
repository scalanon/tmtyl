package org.scalanon.tmtyl

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.PolygonSpriteBatch

case class Tile(loc: Vec2, state: tileState) {
  def draw(batch: PolygonSpriteBatch): Unit = {
    state.draw(batch, this)
  }

  def xIn(entity: Entity): Boolean = {

    if (entity.loc.x + entity.size.x > loc.x && entity.loc.x < loc.x + 1) {
      true
    } else false
  }
  def yIn(entity: Entity): Boolean = {
    if (entity.loc.y + entity.size.y > loc.y && entity.loc.y < loc.y + 1) {
      true
    } else false

  }

}

sealed trait tileState { def draw(batch: PolygonSpriteBatch, tile: Tile): Unit }
object tileState {
  case object Metal extends tileState {
    def draw(batch: PolygonSpriteBatch, tile: Tile): Unit = {}
  }
  case object Lava extends tileState {

    def draw(batch: PolygonSpriteBatch, tile: Tile): Unit = {}
  }

  case object Hologram extends tileState {

    def draw(batch: PolygonSpriteBatch, tile: Tile): Unit = {}
  }
  case object Jump extends tileState {

    def draw(batch: PolygonSpriteBatch, tile: Tile): Unit = {}
  }
}
