package org.scalanon.tmtyl

import com.badlogic.gdx.Input.Keys
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.PolygonSpriteBatch
import org.scalanon.tmtyl.Tmtyl._
import org.scalanon.tmtyl.game.{Entities, Game, Levels, Rect}

import scala.util.Random

case class Player(game: Game) extends Entity {
  val XMargin = 8

  var lookRot: Float = 0f
  var loc: Vec2      = game.entities.start.cata(s => Vec2(s.x, s.y), Vec2(0, 0))
  var size: Vec2     = Vec2(22, 17)
  var vel: Vec2      = Vec2(0, 0)
  var left           = false
  var stage          = 0
  var behavior       = 2

  def draw(batch: PolygonSpriteBatch): Unit = {
    batch.setColor(Color.WHITE)
    val maxStage = Stages(behavior)
    val st       = if (stage < maxStage) stage else maxStage * 2 - stage - 1
    batch.draw(
      walkPlayer,
      loc.x * screenPixel,
      loc.y * screenPixel,
      0f,
      0f,
      size.x * screenPixel,
      size.y * screenPixel,
      1f,
      1f,
      0,
      st * 22,
      behavior * 17,
      16,
      16,
      left,
      false
    )
  }

  var wTick = 0f

  private val SpeedX       = 6 * 16
  private val ClimbSpeed   = 6 * 16
  private val JumpSpeed    = 14 * 16
  private val Gravity      = 60 * 16
  private val DeathlySpeed = 300

  private val Stages = Map(
    0 -> 5,
    1 -> 6,
    2 -> 6,
    3 -> 4
  )

  def update(delta: Float): Unit = {
    wTick += delta

    if (wTick >= .1f) {
      stage += 1
      if (stage >= Stages(behavior) * 2 - 2) {
        stage = 0
      }
      wTick = 0f
    }

    lookRot = Math
      .atan2(
        (game.mouseLoc.y / screenPixel) - (loc.y + size.y),
        (loc.x + size.x / 2) - (game.mouseLoc.x / screenPixel)
      )
      .toFloat

    if (game.keyPressed(Keys.A, Keys.LEFT)) {
      if (vel.x >= 0) stage = 0
      vel.x = -SpeedX
      left = true
      behavior = 2
    } else if (game.keyPressed(Keys.D, Keys.RIGHT)) {
      if (vel.x < 0) stage = 0
      behavior = 2
      vel.x = SpeedX
      left = false
    } else {
      vel.x = 0
      if (vel.y == 0) behavior = 0
    }

    val playerRect = Rect(
      loc.x.toInt + XMargin,
      loc.y.toInt,
      size.x.toInt - 2 * XMargin,
      size.y.toInt
    )

    val onLadder = game.entities.ladders.find(playerRect.isOnTopOrIn)
    val onFloor  = game.entities.floors.find(playerRect.isOnTop)

    var warpLoc = Option.empty[Vec2]

    if (onLadder.isDefined) {
      vel.y = 0
    } else {
      vel.y -= Gravity * delta
    }
    if (game.keyPressed(Keys.W, Keys.UP)) {
      if (onLadder.isDefined) {
        vel.y = ClimbSpeed
        behavior = 0
      } else if (onFloor.isDefined) {
        vel.y = JumpSpeed
        behavior = 3
        stage = 0
      }
    } else if (game.keyPressed(Keys.S, Keys.DOWN)) {
      if (onLadder.isDefined) {
        vel.y = -ClimbSpeed
        behavior = 0
      } else if (game.newKeyPressed(Keys.S, Keys.DOWN)) {
        val onDoor = game.entities.doors.find(playerRect.isOnBottom)
        onDoor foreach { from =>
          if (from.doorway == "exit") {
            game.currentLevel += 1
          } else {
            val toDoors = game.entities.doors.filter(door =>
              door.doorway == from.doorway && (door ne from)
            )
            Random.shuffle(toDoors).headOption foreach { to =>
              warpLoc = Some(
                Vec2(
                  loc.x + to.x - from.x,
                  loc.y + to.y - from.y
                )
              )
            }
          }
        }
      }
    }

    loc = warpLoc getOrElse {
      val newLoc = loc + (vel * delta)

      val newRect = Rect(
        newLoc.x.toInt + XMargin,
        newLoc.y.toInt,
        size.x.toInt - 2 * XMargin,
        size.y.toInt
      )

      if (vel.y >= 0) { // no climbing off the top of a ladder
        onLadder foreach { ladder =>
          newLoc.y = newLoc.y min (ladder.y + ladder.height)
        }
      } else { // no falling through floor unless climbing down a ladder...
        val hitFloor = game.entities.floors.find(floor =>
          newRect.isWithinX(floor) &&
            newRect.y < floor.y + floor.height &&
            playerRect.y >= floor.y + floor.height &&
            !onLadder.exists(ladder => ladder.y < floor.y)
        )
        hitFloor foreach { floor =>
          if (vel.y < -DeathlySpeed) {
            game.state = Game.QuitState
          }
          newLoc.y = floor.y + floor.height
          vel.y = 0
        }
      }
      newLoc
    }

    if (loc.y + size.y < 0) {
      game.state = Game.QuitState
    }

  }
}
