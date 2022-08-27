package org.scalanon.tmtyl.map

import com.badlogic.gdx.Input.Keys
import com.badlogic.gdx.InputAdapter

class WorldmapControl(worldmap: Worldmap) extends InputAdapter {
  override def touchDown(
      screenX: Int,
      screenY: Int,
      pointer: Int,
      button: Int
  ): Boolean = {
    worldmap.done = true
    true
  }

  override def keyDown(keycode: Int): Boolean = {
    if (keycode == Keys.ESCAPE || keycode == Keys.BACK) {
      worldmap.quit = true
    }
    true
  }

  override def keyUp(keycode: Int): Boolean = {
    if (keycode == Keys.SPACE || keycode == Keys.ENTER) {
      worldmap.done = true
    }
    true
  }
}
