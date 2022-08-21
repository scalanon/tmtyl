package org.scalanon.tmtyl

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.PolygonSpriteBatch
import org.scalanon.tmtyl.Tmtyl._
import org.scalanon.tmtyl.game.Game

case class Projectile(game: Game, rot: Float, var loc: Vec2) extends Entity {
  var time = 0f
  var size = Vec2(.2f, .2f)
  def draw(batch: PolygonSpriteBatch): Unit = {
    batch.setColor(Color.CYAN)
    batch.draw(
      square,
      loc.x * screenUnit,
      loc.y * screenUnit,
      size.x * screenUnit,
      size.y * screenUnit
    )
  }
  def update(delta: Float): Unit = {
    time += delta
    loc.x += (Math.cos(rot) * delta * 20).toFloat
    loc.y += (Math.sin(rot) * delta * 20).toFloat
    if (
      time >= 5 || game.tiles.exists(t => {
        loc.x + size.x > t.loc.x && loc.x < t.loc.x + 1 &&
          loc.y + size.y > t.loc.y && loc.y < t.loc.y + 1
      })
    ) {
      game.projectiles = game.projectiles.filterNot(p => p eq this)
    }
  }
}
