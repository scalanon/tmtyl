package org.scalanon.tmtyl

import com.badlogic.gdx.Input.Keys
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.PolygonSpriteBatch
import org.scalanon.tmtyl.Tmtyl._
import org.scalanon.tmtyl.game.Game

case class Player(game: Game) extends Entity {
  var loc: Vec2 = Vec2(0, 0)
  var size: Vec2 = Vec2(1, 2)
  var vel: Vec2 = Vec2(0, 0)
  def draw(batch: PolygonSpriteBatch): Unit = {
    batch.setColor(Color.WHITE)
    batch.draw(
      square,
      loc.x * screenUnit,
      loc.y * screenUnit,
      size.x * screenUnit,
      size.y * screenUnit
    )
  }
  def moveLeft(delta: Float): Unit = {
    vel.x = -6
  }
  def moveRight(delta: Float): Unit = {
    vel.x = 6
  }
  def jump(delta: Float): Unit = {
    vel.y = 20
  }
  def update(delta: Float): Unit = {
    if (game.keysPressed.contains(Keys.A)) {
      moveLeft(delta)
    } else if (game.keysPressed.contains(Keys.D)) {
      moveRight(delta)
    } else {
      vel.x = 0
    }
    if (game.keysPressed.contains(Keys.W) && loc.y == 0) {
      jump(delta)
    }
    if (loc.y > 0) {
      vel.y -= delta
    }
    loc += (vel * delta)
    if (loc.y + vel.y <= 0) {
      vel.y = 0
      loc.y = 0
    }
  }
}
