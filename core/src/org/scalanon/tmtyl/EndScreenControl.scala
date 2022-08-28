package org.scalanon.tmtyl

import com.badlogic.gdx.Input.Keys
import com.badlogic.gdx.InputAdapter

class EndScreenControl(end: EndScreen) extends InputAdapter {
  override def touchDown(
      screenX: Int,
      screenY: Int,
      pointer: Int,
      button: Int
  ): Boolean = {
    true
  }

  override def keyDown(keycode: Int): Boolean = {
    if (keycode == Keys.ESCAPE || keycode == Keys.BACK) {
      end.quit = true
    }

    true

  }

  override def keyUp(keycode: Int): Boolean = {
    true
  }
}
