package org.scalanon.tmtyl.game

import com.badlogic.gdx.Input.Keys
import com.badlogic.gdx.InputAdapter

class DeadScreenControl(dead: DeadScreen) extends InputAdapter {
  override def touchDown(
      screenX: Int,
      screenY: Int,
      pointer: Int,
      button: Int
  ): Boolean = {
    dead.retry = true
    true
  }

  override def keyDown(keycode: Int): Boolean = {
    if (keycode == Keys.ESCAPE || keycode == Keys.BACK) {
      dead.quit = true
    } else if (keycode == Keys.SPACE || keycode == Keys.ENTER) {
      dead.retry = true
    }
    true
  }

  override def keyUp(keycode: Int): Boolean = {
    true
  }
}
