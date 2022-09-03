package org.scalanon.tmtyl

import com.badlogic.gdx.Input.Keys
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.PolygonSpriteBatch
import org.scalanon.tmtyl.Tmtyl._
import org.scalanon.tmtyl.game.{Floor, Game, Rect}
import org.scalanon.tmtyl.util.{SoundWrapper, TextureWrapper}

import scala.util.Random

final case class Player(game: Game) {
  import Player._

  var loc        = Vec2(0, 0)
  val size       = Vec2(22, 17)
  var vel        = Vec2(0, 0)
  var maxY       = 0f
  var dead       = false
  var wTick      = 0f
  var deadTimer  = 0f
  var facingLeft = false
  var stage      = 0
  var behavior   = 2
  var aboveFloor = Option.empty[Floor]

  def draw(batch: PolygonSpriteBatch): Unit = {
    batch.setColor(Color.WHITE)
    val maxStage = Stages(behavior)
    val st       = if (stage < maxStage) stage else maxStage * 2 - stage - 1
    batch.draw(
      doofus,
      loc.x * Geometry.ScreenPixel,
      loc.y * Geometry.ScreenPixel,
      0f,
      0f,
      size.x * Geometry.ScreenPixel,
      size.y * Geometry.ScreenPixel,
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

  def reset(newLoc: Vec2): Unit = {
    loc = newLoc
    vel = Vec2(0, 0)
    maxY = loc.y
    facingLeft = false
    stage = 0
    behavior = 2
    aboveFloor = None
  }

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

    if (dead) {
      vel.x = 0f
    } else {
      if (game.control.keyDown.left || game.control.drag.left) {
        if (vel.x >= 0) stage = 0
        vel.x = (vel.x - delta * AccelX) max -SpeedX
        facingLeft = true
        behavior = 2
      } else if (game.control.keyDown.right || game.control.drag.right) {
        if (vel.x < 0) stage = 0
        behavior = 2
        vel.x = (vel.x + delta * AccelX) min SpeedX
        facingLeft = false
      } else if (!dead) {
        vel.x = 0
        if (vel.y == 0) behavior = 0
      }
    }

    val oldRect = hitRect()

    aboveFloor =
      game.entities.floors.filter(oldRect.isOnOrAbove).maxByOption(_.top)

    val onLadder = game.entities.ladders.find(oldRect.isOnTopOrWithin)
    val onFloor  = aboveFloor.filter(oldRect.isOnTop)

    var warpLoc = Option.empty[Vec2]

    if (dead) {
      vel.y -= Gravity * delta
    } else if (onLadder.isDefined) {
      behavior = 7
      if (game.control.keyDown.up || game.control.drag.up) {
        vel.y = ClimbSpeed
        if (onLadder.exists(loc.y >= _.top)) stage = 0
      } else if (game.control.keyDown.down || game.control.drag.down) {
        vel.y = -ClimbSpeed
        if (onLadder.exists(loc.y >= _.bottom)) stage = 0
      } else {
        vel.y = 0
        stage = 0
      }
    } else if (onFloor.isDefined) {
      if (game.control.keyDown.up || game.control.swipe.up) {
        vel.y = JumpSpeed
        behavior = 3
        stage = 0
      } else if (
        game.control.keyPressed.down || game.control.swipe.down
      ) {
        game.entities.doors.find(oldRect.isOnBottom).foreach { from =>
          if (from.doorway == "exit") {
            game.state = Game.NextState
          } else if (
            !game.entities.switches
              .exists(sw => sw.key == from.key) || game.entities.switches
              .exists(sw => sw.key == from.key && sw.used)
          ) {
            val toDoors = game.entities.doors
              .filter(door => door.doorway == from.doorway && (door ne from))
            Random.shuffle(toDoors).headOption foreach { to =>
              warpLoc = Some(
                Vec2(
                  to.x.toFloat + (to.width.toFloat - size.x) / 2f,
                  to.y.toFloat
                )
              )
            }
          }

        }
        game.entities.switches
          .find(oldRect.isOnBottom)
          .foreach(s => s.used = !s.used)
      } else {
        vel.y = 0
      }
    } else {
      vel.y -= Gravity * delta
    }

    loc = warpLoc getOrElse {
      maxY =
        if (onFloor.isDefined || onLadder.isDefined) loc.y else maxY max loc.y
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
          newLoc.y = floor.top.toFloat
          vel.y = 0
          if (maxY - newLoc.y >= DeathlyFall) {
            die(splat)
          }
        }
      }
      newLoc
    }

    if (loc.y + size.y < 0) {
      die(wilhelm)
    }
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

  private val SpeedX      = 6 * 16f
  private val AccelX      = SpeedX * 60 / 5
  private val ClimbSpeed  = 6 * 16f
  private val JumpSpeed   = 14 * 16f
  private val Gravity     = 60 * 16f
  // just < 3 blocks so you can jump from one block high but not fall 3
  private val DeathlyFall = 47f

  private val DeadBehaviour = 6
  private val Stages        = Map(
    0             -> 5,
    1             -> 6,
    2             -> 6,
    3             -> 4,
    DeadBehaviour -> 6,
    7             -> 5
  )

  private def doofus  = TextureWrapper.load("tiny_adventurer_sheet.png")
  private def wilhelm = AssetLoader.sound("scream.mp3")
  private def splat   = AssetLoader.sound("splat.mp3")
  private def scream  = AssetLoader.sound("screamQuick.mp3")
}
