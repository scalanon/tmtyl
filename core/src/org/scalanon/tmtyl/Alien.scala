package org.scalanon.tmtyl

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.PolygonSpriteBatch
import org.scalanon.tmtyl.Tmtyl._
import org.scalanon.tmtyl.game.Game
import org.scalanon.tmtyl.home.AlienAnimation

case class Alien(game: Game) extends Entity {
  var alien = new home.Alien(new AlienAnimation.Idle)

  var loc = game.player.loc + Vec2(-80, 80)
  val size = Vec2(16, 16)

  val Speed = 64
  val MaxDist = 16

  def update(delta: Float): Unit = {
    alien.update(delta)
    if (loc.x + size.x < game.player.loc.x - MaxDist) {
      loc.x = (loc.x + delta * Speed) min game.player.loc.x - MaxDist
    } else if (loc.x > game.player.loc.x + game.player.size.x + MaxDist) {
      loc.x = (loc.x - delta * Speed) max game.player.loc.x + MaxDist
    }

    if (loc.y < game.player.loc.y + MaxDist) {
      loc.y = (loc.y + delta * Speed) min game.player.loc.y + MaxDist
    } else if (loc.y > game.player.loc.y + MaxDist) {
      loc.y = (loc.y - delta * Speed) max game.player.loc.y + MaxDist
    }
  }

  def draw(batch: PolygonSpriteBatch): Unit = {
    batch.setColor(Color.WHITE)
    alien.draw(
      loc.x * screenPixel,
      loc.y * screenPixel,
      screenPixel,
      batch
    )
  }
}
