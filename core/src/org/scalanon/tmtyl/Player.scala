package org.scalanon.tmtyl

import com.badlogic.gdx.Input.Keys
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.PolygonSpriteBatch
import org.scalanon.tmtyl.Tmtyl._
import org.scalanon.tmtyl.game.Game

case class Player(game: Game) extends Entity {
  var lookRot: Float = 0f
  var loc: Vec2 = Vec2(0, 3)
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
    batch.setColor(Color.GRAY)
    batch.draw(
      square,
      (loc.x + .5f) * screenUnit,
      (loc.y + 1f) * screenUnit,
      .1f * screenUnit,
      0f * screenUnit,
      .2f * screenUnit,
      .8f * screenUnit,
      1f,
      1f,
      lookRot.toDegrees - 90,
      0,
      0,
      1,
      1,
      false,
      false
    )
  }
  def moveLeft(delta: Float): Unit = {
    vel.x = -6
  }
  def moveRight(delta: Float): Unit = {
    vel.x = 6
  }
  def jump(delta: Float): Unit = {
    vel.y = 14
  }
  def update(delta: Float): Unit = {
    lookRot = (
      Math
        .atan2(
          ((game.mouseLoc.y / screenUnit) - (loc.y + 1.5)),
          ((game.mouseLoc.x / screenUnit) - (Geometry.ScreenWidth / 2 - ((loc.x + (size.x / 2)) * screenUnit)) / screenUnit - (loc.x + .5))
        )
        .toFloat
    )
    if (game.keysPressed.contains(Keys.A)) {
      moveLeft(delta)
    } else if (game.keysPressed.contains(Keys.D)) {
      moveRight(delta)
    } else {
      vel.x = 0
    }

    if (
      game.keysPressed.contains(Keys.W) && game.tiles.exists(t => {
        t.xIn(this) && loc.y == t.loc.y + 1
      })
    ) {
      jump(delta)
    }
    vel.y -= 1

    game.tiles.foreach(t => {
      if (
        t.yIn(
          this
        ) && loc.x + size.x + vel.x * delta > t.loc.x && loc.x <= t.loc.x
      ) {
        loc.x = t.loc.x - size.x
        vel.x = 0
      }
      if (
        t.yIn(
          this
        ) && loc.x + vel.x * delta < t.loc.x + 1 && loc.x >= t.loc.x + 1
      ) {
        loc.x = t.loc.x + 1
        vel.x = 0
      }
      if (
        t.xIn(this) &&
        loc.y + vel.y * delta <= t.loc.y + 1 && loc.y >= t.loc.y + 1
      ) {
        vel.y = 0
        loc.y = t.loc.y + 1
      }
    })
    loc += (vel * delta)

  }
}
