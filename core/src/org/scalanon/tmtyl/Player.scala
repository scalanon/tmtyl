package org.scalanon.tmtyl

import com.badlogic.gdx.Input.Keys
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.PolygonSpriteBatch
import org.scalanon.tmtyl.Tmtyl._
import org.scalanon.tmtyl.game.{Game, Rect}
import org.scalanon.tmtyl.util.SoundWrapper

import scala.util.Random

final case class Player(game: Game) {
  import Player._

  var lookRot: Float = 0f
  var loc: Vec2      =
    game.entities.start.cata(s => Vec2(s.x.toFloat, s.y.toFloat), Vec2(0, 0))
  var size: Vec2     = Vec2(22, 17)
  var vel: Vec2      = Vec2(0, 0)
  var dead           = false
  var facingLeft     = false
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
      facingLeft,
      false
    )
  }

  var wTick     = 0f
  var deadTimer = 0f

  def update(delta: Float): Unit = {
    if (game.keysPressed.contains(Keys.I)) {
      Player.Immortal = !Player.Immortal
    }
    if (dead) {
      deadTimer += delta
      if (deadTimer >= 1f) game.state = Game.QuitState
    }

    wTick += delta
    if (wTick >= .1f) {
      stage += 1
      val stageMax = Stages(behavior)
      if (behavior == DeadBehaviour) {
        stage = stage min stageMax
      } else if (stage >= stageMax * 2 - 2) {
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

    if (game.keyPressed(Keys.A, Keys.LEFT) && !dead) {
      if (vel.x >= 0) stage = 0
      vel.x = -SpeedX
      facingLeft = true
      behavior = 2
    } else if (game.keyPressed(Keys.D, Keys.RIGHT) && !dead) {
      if (vel.x < 0) stage = 0
      behavior = 2
      vel.x = SpeedX
      facingLeft = false
    } else if (!dead) {
      vel.x = 0
      if (vel.y == 0) behavior = 0
    }

    val playerRect = hitRect()

    val onLadder = game.entities.ladders.find(playerRect.isOnTopOrIn)
    val onFloor  = game.entities.floors.find(playerRect.isOnTop)

    var warpLoc  = Option.empty[Vec2]
    var climbing = false

    if (onLadder.isDefined && !dead) {
      behavior = 7
      vel.y = 0
    } else {
      vel.y -= Gravity * delta
    }
    if (game.keyPressed(Keys.W, Keys.UP) && !dead) {
      if (onLadder.isDefined) {
        vel.y = ClimbSpeed
        behavior = 7
        climbing = true
      } else if (onFloor.isDefined) {
        vel.y = JumpSpeed
        behavior = 3
        stage = 0
      }
    } else if (game.keyPressed(Keys.S, Keys.DOWN) && !dead) {
      if (onLadder.isDefined) {
        vel.y = -ClimbSpeed
        behavior = 7
        climbing = true
      } else if (game.newKeyPressed(Keys.S, Keys.DOWN)) {
        val onDoor = game.entities.doors.find(playerRect.isOnBottom)
        onDoor foreach { from =>
          if (from.doorway == "exit") {
            game.nextLevel()
          } else if (
            !game.entities.switches
              .exists(sw => sw.key == from.key) || game.entities.switches
              .exists(sw => sw.key == from.key && sw.used)
          ) {
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
        game.entities.switches
          .find(playerRect.isOnBottom)
          .foreach(s => s.used = !s.used)
      }
    }

    if (!climbing && onLadder.isDefined && !dead) {
      stage = 0
    }

    loc = warpLoc getOrElse {
      val newLoc = loc + (vel * delta)

      val newRect = hitRect(newLoc)

      if (vel.y >= 0) { // no climbing off the top of a ladder
        onLadder foreach { ladder =>
          newLoc.y = newLoc.y min (ladder.y + ladder.height).toFloat
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
            die(splat)
          }
          newLoc.y = (floor.y + floor.height).toFloat
          vel.y = 0
        }
      }
      newLoc
    }

    if (loc.y + size.y < 0) die(wilhelm, permanent = true)
  }

  def die(sound: SoundWrapper = scream, permanent: Boolean = false): Unit = {
    if (!dead) {
      if (permanent || !Immortal) {
        dead = true
      }
      behavior = DeadBehaviour
      stage = 0
      vel.x = 0
      sound.play()
    }
  }

  def hitRect(location: Vec2 = loc): Rect = Rect(
    location.x.toInt + XMargin,
    location.y.toInt,
    size.x.toInt - 2 * XMargin,
    size.y.toInt
  )

  def centerX: Float = loc.x + size.x / 2f
  def left: Float    = loc.x
  def right: Float   = loc.x + size.x
  def bottom: Float  = loc.y
  def top: Float     = loc.y + size.y
}

object Player {
  var Immortal = true

  private val XMargin = 8

  private val SpeedX       = 6 * 16f
  private val ClimbSpeed   = 6 * 16f
  private val JumpSpeed    = 14 * 16f
  private val Gravity      = 60 * 16f
  private val DeathlySpeed = 300f

  private val DeadBehaviour = 6
  private val Stages        = Map(
    0             -> 5,
    1             -> 6,
    2             -> 6,
    3             -> 4,
    DeadBehaviour -> 6,
    7             -> 5
  )

  private def wilhelm = AssetLoader.sound("scream.mp3")
  private def splat   = AssetLoader.sound("splat.mp3")
  private def scream  = AssetLoader.sound("screamQuick.mp3")
}
