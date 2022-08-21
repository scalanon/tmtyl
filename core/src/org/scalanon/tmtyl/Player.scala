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
  var left = false
  var stage = 0
  var behavior = 2
  def draw(batch: PolygonSpriteBatch): Unit = {
    batch.setColor(Color.WHITE)
    batch.draw(
      walkPlayer,
      loc.x * screenUnit,
      loc.y * screenUnit,
      0f,
      0f,
      size.x * screenUnit,
      size.y * screenUnit,
      1f,
      1f,
      0,
      stage * 22,
      behavior * 17,
      16,
      16,
      left,
      false
    )
    /* batch.setColor(Color.GRAY)
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
    )*/
  }
  var wTick = 0f
  def moveLeft(delta: Float): Unit = {
    vel.x = -6
    left = true
    behavior = 2
  }
  def moveRight(delta: Float): Unit = {

    behavior = 2
    vel.x = 6
    left = false
  }
  def jump(delta: Float): Unit = {
    vel.y = 14
    behavior = 3
  }
  def shoot(): Unit = {
    game.projectiles = Projectile(
      game,
      lookRot,
      Vec2(
        loc.x + .5f + (math.cos(lookRot).toFloat * .8f),
        loc.y + (math.sin(lookRot).toFloat * .8f) + 1f
      )
    ) :: game.projectiles
  }
  def update(delta: Float): Unit = {
    wTick += delta

    if (wTick >= .1f) {
      stage += 1
      if (stage == 5) {
        stage = 0
      }
      wTick = 0f
    }

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
      behavior = 0
    }
    if (loc.y + 2 < 0) {
      game.state = Game.QuitState
    }

    if (
      game.keysPressed.contains(Keys.W) && game.tiles.exists(t => {
        t.xIn(this) && loc.y == t.loc.y + 1
      })
    ) {
      jump(delta)
    }
    vel.y -= 1
    if (
      !game.tiles.exists(t => {
        t.xIn(this) &&
          loc.y + vel.y * delta <= t.loc.y + 1 && loc.y >= t.loc.y + 1
      })
    ) {
      behavior = 3
      if (stage == 4) {
        stage = 0
      }
    }

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
