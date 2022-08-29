package org.scalanon.tmtyl

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.PolygonSpriteBatch
import org.scalanon.tmtyl.game.Game
import org.scalanon.tmtyl.home.AlienAnimation

case class Alien(game: Game) extends Actor {
  var alien = new home.Alien(new AlienAnimation.Idle)

  var loc  = game.player.loc + Vec2(-80, 80)
  val size = Vec2(16, 16)

  val Speed   = 64
  val MaxDist = 16

  def update(delta: Float): List[Actor] = {
    alien.update(delta)
    if (loc.x + size.x < game.player.left - MaxDist) {
      loc.x = (loc.x + delta * Speed) min game.player.loc.x - MaxDist
    } else if (loc.x > game.player.right + MaxDist) {
      loc.x = (loc.x - delta * Speed) max game.player.loc.x + MaxDist
    }

    if (loc.y < game.player.bottom + MaxDist) {
      loc.y = (loc.y + delta * Speed) min game.player.bottom + MaxDist
    } else if (loc.y > game.player.loc.y + MaxDist) {
      loc.y = (loc.y - delta * Speed) max game.player.bottom + MaxDist
    }
    List(this)
  }

  def draw(batch: PolygonSpriteBatch): Unit = {
    batch.setColor(Color.WHITE)
    alien.draw(
      loc.x * Geometry.ScreenPixel,
      loc.y * Geometry.ScreenPixel,
      Geometry.ScreenPixel,
      batch
    )
  }
}
