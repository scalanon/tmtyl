package org.scalanon.tmtyl

import com.badlogic.gdx.Input.Keys
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.PolygonSpriteBatch
import org.scalanon.tmtyl.Tmtyl._
import org.scalanon.tmtyl.game.{Floor, Game, Rect}
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
  var aboveFloor     = Option.empty[Floor]

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

  var mLeft  = false
  var mRight = false
  var mUp    = false
  var mDown  = false
  def update(delta: Float): Unit = {

    if (dead) {
      deadTimer += delta
      if (deadTimer >= 1f) game.state = Game.LostState
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

    if ((game.keyPressed(Keys.A, Keys.LEFT) || mLeft) && !dead) {
      if (vel.x >= 0) stage = 0
      vel.x = -SpeedX
      facingLeft = true
      behavior = 2
    } else if ((game.keyPressed(Keys.D, Keys.RIGHT) || mRight) && !dead) {
      if (vel.x < 0) stage = 0
      behavior = 2
      vel.x = SpeedX
      facingLeft = false
    } else if (!dead) {
      vel.x = 0
      if (vel.y == 0) behavior = 0
    }

    val oldRect = hitRect()

    aboveFloor =
      game.entities.floors.filter(oldRect.isOnOrAbove).maxByOption(_.top)

    val onLadder = game.entities.ladders.find(oldRect.isOnTopOrWithin)
    val onFloor  = aboveFloor.filter(oldRect.isOnTop)

    var warpLoc  = Option.empty[Vec2]
    var climbing = false

    if (onLadder.isDefined && !dead) {
      behavior = 7
      vel.y = 0
    } else {
      vel.y -= Gravity * delta
    }
    if ((game.keyPressed(Keys.W, Keys.UP) || mUp) && !dead) {
      if (onLadder.isDefined) {
        vel.y = ClimbSpeed
        behavior = 7
        climbing = true
      } else if (onFloor.isDefined) {
        vel.y = JumpSpeed
        behavior = 3
        stage = 0
      }
    } else if ((game.keyPressed(Keys.S, Keys.DOWN) || (mDown)) && !dead) {
      if (onLadder.isDefined) {
        vel.y = -ClimbSpeed
        behavior = 7
        climbing = true
      } else if (game.newKeyPressed(Keys.S, Keys.DOWN) || (mDown)) {
        val onDoor = game.entities.doors.find(oldRect.isOnBottom)
        onDoor foreach { from =>
          if (from.doorway == "exit") {
            game.switchToLevel(game.currentLevel + 1)
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
                  to.x + (to.width - size.x) / 2,
                  to.y
                )
              )
            }
          }

        }
        game.entities.switches
          .find(oldRect.isOnBottom)
          .foreach(s => s.used = !s.used)
      }
    }

    if (!climbing && onLadder.isDefined && !dead) {
      stage = 0
    }

    loc = warpLoc getOrElse {
      val newLoc = loc + (vel * delta)
      if (vel.x != 0) {
        val xRect   = hitRect(newLoc.x, loc.y)
        val hitSide = game.entities.floors.find(floor =>
          floor.solid && xRect.intersects(floor) && !oldRect.intersects(floor)
        )
        hitSide foreach { floor =>
          newLoc.x += (vel.x > 0)
            .fold(floor.left - xRect.right, floor.right - xRect.left)
        }
      }

      if (vel.y >= 0) { // no climbing off the top of a ladder
        onLadder foreach { ladder =>
          newLoc.y = newLoc.y min (ladder.y + ladder.height).toFloat
        }
      } else { // no falling through floor unless climbing down a ladder...
        val newRect  = hitRect(newLoc)
        val hitFloor = game.entities.floors.find(floor =>
          newRect.intersectsX(floor) &&
            newRect.y < floor.y + floor.height &&
            oldRect.y >= floor.y + floor.height &&
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

    if (loc.y + size.y < 0) {
      die(wilhelm)

    }
    mUp = false
    mLeft = false
    mDown = false
    mRight = false
  }

  def die(sound: SoundWrapper = scream): Unit = {
    if (!dead && behavior != DeadBehaviour) {
      dead = true
      behavior = DeadBehaviour
      game.score.score = 0
      stage = 0
      vel.x = 0
      sound.play()
    }
  }

  // TODO: store this on the player rather than creating a hit rect for every caller
  def hitRect(location: Vec2 = loc): Rect = hitRect(location.x, location.y)

  def hitRect(x: Float, y: Float): Rect =
    Rect(x + XMargin, y, size.x - 2 * XMargin, size.y)

  def centerX: Float = loc.x + size.x * .5f
  def centerY: Float = loc.y + size.y * .5f

  def headY: Float  = loc.y + size.y * .75f
  def left: Float   = loc.x
  def right: Float  = loc.x + size.x
  def bottom: Float = loc.y
  def top: Float    = loc.y + size.y
}

object Player {

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
