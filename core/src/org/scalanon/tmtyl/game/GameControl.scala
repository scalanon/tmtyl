package org.scalanon.tmtyl.game

import com.badlogic.gdx.Input.Keys
import com.badlogic.gdx.InputAdapter
import org.scalanon.tmtyl.{Geometry, Tmtyl, Vec2}

import scala.collection.mutable

class GameControl(game: Game) extends InputAdapter {

  private val down = mutable.Map.empty[Int, ((Int, Int), (Int, Int))]

  override def touchDown(
      screenX: Int,
      screenY: Int,
      pointer: Int,
      button: Int
  ): Boolean = {
    down.put(pointer, ((screenX, screenY), (screenX, screenY)))
    if (screenX > (Geometry.ScreenWidth * .8f)) {
      if (screenY > Geometry.ScreenHeight / 2) {
        game.player.interact = true
      } else {
        game.player.jump = true
      }
    }
    true

  }

  override def touchDragged(
      screenX: Int,
      screenY: Int,
      pointer: Int
  ): Boolean = {
    down
      .get(pointer)
      .foreach(t => down.put(pointer, (t._1, (screenX, screenY))))

    down.foreach(d => {
      if (d._2._1._1 <= Geometry.ScreenWidth * .8f) {
        if (d._2._2._1 - d._2._1._1 >= Geometry.ScreenPixel * 10) {
          game.player.mRight = true
        }

        if (d._2._2._1 - d._2._1._1 <= -Geometry.ScreenPixel * 10) {
          game.player.mLeft = true
        }
        if (d._2._2._2 - d._2._1._2 <= -Geometry.ScreenPixel * 10) {
          game.player.mUp = true
        }
        if (d._2._2._2 - d._2._1._2 >= Geometry.ScreenPixel * 10) {
          game.player.mDown = true
        }
      }
    })

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
