package org.scalanon.tmtyl

import com.badlogic.gdx.InputAdapter
import com.badlogic.gdx.graphics.g2d.PolygonSpriteBatch
import com.badlogic.gdx.graphics.{Color, Texture}
import org.scalanon.tmtyl.Tmtyl.{screenPixel, walkPlayer}
import org.scalanon.tmtyl.game.Game
import org.scalanon.tmtyl.home.{AlienAnimation, Home}

case class EndScreen(game: Game) extends Scene {
  import EndScreen._

  def init(): InputAdapter = {
    new EndScreenControl(this)
  }
  
  val endPixel = screenPixel * 2

  var quit = false
  var time = 0f
  var alien = new home.Alien(new AlienAnimation.Idle)
  var dead = false
  var alienLoc = Vec2(30,71)
  var alienDst = Vec2(76, 25)

  val Speed = 15

  def update(delta: Float): Option[Scene] = {
    time = time + delta
    alien.update(delta)
    dead = (alienLoc.x == alienDst.x && alienLoc.y == alienDst.y)
    if (alienLoc.x < alienDst.x) alienLoc.x = (alienLoc.x + delta * Speed) min alienDst.x
    if (alienLoc.y > alienDst.y) alienLoc.y = (alienLoc.y - delta * Speed) max alienDst.y
    if (quit) {
      Some(new Home)
    } else {
      None
    }
  }

  def render(batch: PolygonSpriteBatch): Unit = {
    batch.setColor(Color.WHITE)
    batch.draw(
      endScreen,
      0,
      0,
      Geometry.ScreenWidth,
      Geometry.ScreenHeight
    )

    val frame = (time * 10).toInt % 5
    batch.draw(
      doofus,
      55 * endPixel,
      8 * endPixel,
      22 * endPixel,
      17 * endPixel,
      22 * frame,
      17 * 0,
      22,
      17,
      false,
      false
    )
    batch.draw(
      doorknob,
      76 * endPixel,
      8 * endPixel,
      12 * endPixel,
      33 * endPixel,
      12 * frame,
      if (dead) 33 else 0,
      12,
      33,
      false,
      false
    )
    val offset = if (!dead || frame == 3 || frame == 2) 1 else 0
    val extra      = alien.animation match {
      case i: AlienAnimation.Idle if i.frame % 2 == 1 && !i.blink => 1
      case _ => 0
    }
    alien.draw(alienLoc.x * endPixel, (alienLoc.y - offset) * endPixel, endPixel, batch)
    if (dead) {
      batch.draw(
        endo,
        (alienLoc.x + 1) * endPixel,
        (alienLoc.y + 6 - offset - extra) * endPixel,
        27 * endPixel,
        28 * endPixel,
      )
    }
  }
}

object EndScreen {
  var stage = 0

  def endScreen = AssetLoader.image("whic.png")
  def doofus = AssetLoader.image("tiny_adventurer_sheet.png")
  def doorknob = AssetLoader.image("president.png")
  def endo = AssetLoader.image("end.png")


}
