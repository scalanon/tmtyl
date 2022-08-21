package org.scalanon.tmtyl

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.PolygonSpriteBatch
import org.scalanon.tmtyl.Tmtyl._
import org.scalanon.tmtyl.game.Game

case class Alien(game: Game) extends Entity {
  var loc = Vec2(-5, 5)
  var size = Vec2(1, 1)
  def update(delta: Float): Unit = {
    if (loc.x + size.x < game.player.loc.x - 1) {
      loc.x = (loc.x + delta * 4) min game.player.loc.x - 1
    } else if (loc.x > game.player.loc.x + game.player.size.x + 1) {
      loc.x = (loc.x - delta * 4) max game.player.loc.x + 1
    }

    if (loc.y < game.player.loc.y + 1) {
      loc.y = (loc.y + delta * 4) min game.player.loc.y + 1
    } else if (loc.y > game.player.loc.y + 1) {
      loc.y = (loc.y - delta * 4) max game.player.loc.y + 1
    }
  }
  def draw(batch: PolygonSpriteBatch): Unit = {
    batch.setColor(Color.GREEN)
    batch.draw(
      square,
      loc.x * screenUnit,
      loc.y * screenUnit,
      size.x * screenUnit,
      size.y * screenUnit
    )
  }
}
