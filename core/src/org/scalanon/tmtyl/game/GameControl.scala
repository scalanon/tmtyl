package org.scalanon.tmtyl.game

import com.badlogic.gdx.Input.Keys
import com.badlogic.gdx.InputAdapter
import org.scalanon.tmtyl.{Geometry, Vec2}

import scala.collection.mutable

class GameControl(game: Game) extends InputAdapter {

  private val down = mutable.Map.empty[Int, (Int, Int)]

  override def touchDown(
      screenX: Int,
      screenY: Int,
      pointer: Int,
      button: Int
  ): Boolean = {
    down.put(pointer, (screenX, screenY))
    true
  }

  override def touchDragged(
      screenX: Int,
      screenY: Int,
      pointer: Int
  ): Boolean = {

    if (screenY <= Geometry.ScreenHeight / 2) { game.player.mUp = true }
    if (screenX >= (Geometry.ScreenWidth / 3) * 2) { game.player.mRight = true }
    else if (screenX <= (Geometry.ScreenWidth / 3)) {
      game.player.mLeft = true
    } else { game.player.mDown = true }

    true
  }

  override def touchUp(
      screenX: Int,
      screenY: Int,
      pointer: Int,
      button: Int
  ): Boolean = {
    down.remove(pointer) foreach { case (oldX, oldY) =>
    }
    true
  }

  override def keyDown(keycode: Int): Boolean = {
    keycode match {
      case Keys.ESCAPE =>
        game.state = Game.QuitState
      case Keys.BACK   =>
        game.state = Game.PauseState
      case o           =>
        game.keysPressed.add(o)
        game.newKeysPressed.add(o)
    }
    true
  }

  override def keyUp(keycode: Int): Boolean = {
    game.keysPressed.remove(keycode)

    true
  }

  override def mouseMoved(screenX: Int, screenY: Int): Boolean = {
    game.mouseLoc = Vec2(screenX, Geometry.ScreenHeight - screenY)
    true
  }
}

object GameControl {}
