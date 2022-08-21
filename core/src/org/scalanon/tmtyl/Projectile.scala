package org.scalanon.tmtyl

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.PolygonSpriteBatch
import org.scalanon.tmtyl.Tmtyl._
import org.scalanon.tmtyl.game.Game

case class Projectile(game: Game, rot: Float, var loc: Vec2) extends Entity {
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
    loc.x += (Math.cos(rot) * delta * 20).toFloat
    loc.y += (Math.sin(rot) * delta * 20).toFloat
  }
}
