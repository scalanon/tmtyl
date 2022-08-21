package org.scalanon.tmtyl.game

import com.badlogic.gdx.Input.Keys
import com.badlogic.gdx.InputAdapter

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
    if (keycode == Keys.ESCAPE) {
      game.state = Game.QuitState
    } else if (keycode == Keys.BACK) {
        game.state = Game.PauseState
    }
    game.keysPressed = keycode :: game.keysPressed

    true
  }

  override def keyUp(keycode: Int): Boolean = {
    game.keysPressed = game.keysPressed.filterNot(key => key == keycode)

    true
  }

}

object GameControl {}
