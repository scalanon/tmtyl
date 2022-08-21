package org.scalanon.tmtyl
package game

import com.badlogic.gdx.graphics.g2d.PolygonSpriteBatch
import org.scalanon.tmtyl.Scene
import org.scalanon.tmtyl.home.Home

class Game extends Scene {
  import Game._

  var state: State = PlayingState

  val score: Score = new Score
  var player: Player = Player(this)
  var keysPressed = List.empty[Int]

  override def init(): GameControl = {
    state = PlayingState
    new GameControl(this)
  }

  override def update(delta: Float): Option[Scene] = {
    score.update(delta)
    player.update(delta)
    PartialFunction.condOpt(state) { case QuitState =>
      Home(this)
    }
  }

  override def render(batch: PolygonSpriteBatch): Unit = {
    score.draw(batch)
    player.draw(batch)
  }
}

object Game {
  sealed trait State
  case object PlayingState extends State
  case object LostState extends State
  case object QuitState extends State
}
