package org.scalanon.tmtyl.game

import com.badlogic.gdx.Input.Keys
import com.badlogic.gdx.InputAdapter
import org.scalanon.tmtyl.{Geometry, Vec2}

import scala.collection.mutable

class GameControl(game: Game) extends InputAdapter {
  import Touch.DragDistance

  private val keysPressed = mutable.Set.empty[Int]
  private val touches     = mutable.Map.empty[Int, Touch]

  var swipe: Motion      = Motion.none
  var drag: Motion       = Motion.none
  var keyPressed: Motion = Motion.none
  var keyDown: Motion    = Motion.none

  def reset(): Unit = {
    keysPressed.clear()
    touches.clear()
    drag = Motion.none
    keyDown = Motion.none
    clean()
  }

  // called every frame to reset the new events that occur before the next update
  def clean(): Unit = {
    keyPressed = Motion.none
    swipe = Motion.none
  }

  override def touchDown(
      screenX: Int,
      screenY: Int,
      pointer: Int,
      button: Int
  ): Boolean = {
    touches.put(pointer, Touch(screenX, screenY))
    true
  }

  override def touchDragged(
      screenX: Int,
      screenY: Int,
      pointer: Int
  ): Boolean = {
    // we put unstarted drags into the map in case the user had left the touch down from a prior scene
    touches.put(
      pointer,
      touches.get(pointer).cata(_.at(screenX, screenY), Touch(screenX, screenY))
    )
    updateDrag()
    true
  }

  override def touchUp(
      screenX: Int,
      screenY: Int,
      pointer: Int,
      button: Int
  ): Boolean = {
    for {
      touch  <- touches.remove(pointer)
      release = touch.at(screenX, screenY)
      if release.isSwipe
    } {
      // This could consider angles so you can swipe up right too, but no
      swipe =
        if (release.dy.abs >= release.dx.abs)
          Motion(up = release.dy < 0, down = release.dy > 0)
        else
          Motion(right = release.dx > 0, left = release.dx < 0)
    }
    updateDrag()
    true
  }

  private def updateDrag(): Unit = touches.values.find(_.isDrag) match {
    case Some(touch) =>
      drag = Motion(
        left = touch.dx <= -DragDistance,
        right = touch.dx >= DragDistance,
        up = touch.dy <= -DragDistance,
        down = touch.dy >= DragDistance
      )
    case None        =>
      drag = Motion.none
  }

  override def keyDown(keycode: Int): Boolean = {
    keysPressed.add(keycode)
    keycode match {
      case Keys.ESCAPE         =>
        game.state = Game.QuitState
      case Keys.BACK           =>
        game.state = Game.PauseState
      case Keys.W | Keys.UP    =>
        keyPressed = keyPressed.copy(up = true)
      case Keys.S | Keys.DOWN  =>
        keyPressed = keyPressed.copy(down = true)
      case Keys.A | Keys.LEFT  =>
        keyPressed = keyPressed.copy(left = true)
      case Keys.D | Keys.RIGHT =>
        keyPressed = keyPressed.copy(right = true)
      case _                   =>
    }
    updateKeyDown()
    true
  }

  override def keyUp(keycode: Int): Boolean = {
    keysPressed.remove(keycode)
    updateKeyDown()
    true
  }

  private def updateKeyDown(): Unit = {
    keyDown = Motion(
      up = keysPressed.contains(Keys.W) || keysPressed.contains(Keys.UP),
      down = keysPressed.contains(Keys.S) || keysPressed.contains(Keys.DOWN),
      left = keysPressed.contains(Keys.A) || keysPressed.contains(Keys.LEFT),
      right = keysPressed.contains(Keys.D) || keysPressed.contains(Keys.RIGHT)
    )
  }

  override def mouseMoved(screenX: Int, screenY: Int): Boolean = {
    game.mouseLoc = Vec2(screenX, Geometry.ScreenHeight - screenY)
    true
  }
}

final case class Motion(
    up: Boolean = false,
    down: Boolean = false,
    left: Boolean = false,
    right: Boolean = false
)

object Motion {
  val none: Motion = Motion()
}

final case class Touch(
    originX: Int,
    originY: Int,
    x: Int,
    y: Int,
    start: Long
) {
  import Touch._

  def at(x: Int, y: Int): Touch = Touch(originX, originY, x, y, start)

  def dx: Int  = x - originX
  def dy: Int  = y - originY
  def dt: Long = now - start

  def isDrag: Boolean  = dt >= DragTime
  def isSwipe: Boolean =
    dt < DragTime && (dx * dx + dy * dy) >= SwipeDistance * SwipeDistance
}

object Touch {
  def apply(x: Int, y: Int): Touch =
    new Touch(x, y, x, y, now)

  private def now: Long     = System.currentTimeMillis
  // minimum distance to qualify as a swipe
  private val SwipeDistance = Geometry.ScreenWidth.toInt / 32
  // minimum time to qualify as a drag
  private val DragTime      = 250L
  // minimum distance to qualify as a drag
  val DragDistance          = Geometry.ScreenWidth.toInt / 32

}

object GameControl {}
