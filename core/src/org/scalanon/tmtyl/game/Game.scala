package org.scalanon.tmtyl
package game

import com.badlogic.gdx.graphics.g2d.PolygonSpriteBatch
import com.badlogic.gdx.math.Matrix4
import org.scalanon.tmtyl.Scene
import org.scalanon.tmtyl.Tmtyl._
import org.scalanon.tmtyl.home.Home

class Game extends Scene {
  import Game._

  var state: State = PlayingState

  val score: Score = new Score
  val matrix = new Matrix4()

  var player: Player = Player(this)
  var tiles: List[Tile] = List(
    Tile(Vec2(0, 0), tileState.Metal),
    Tile(Vec2(1, 0), tileState.Metal),
    Tile(Vec2(2, 0), tileState.Metal),
    Tile(Vec2(2, 1), tileState.Metal),
    Tile(Vec2(3, 0), tileState.Metal)
  )
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
    val translationX =
      Geometry.ScreenWidth / 2 - ((player.loc.x + (player.size.x / 2)) * screenUnit)
    batch.setTransformMatrix(
      matrix.setToTranslation(translationX, 0, 0)
    )

    tiles.foreach(t => t.draw(batch))
    player.draw(batch)

    batch.setTransformMatrix(matrix.idt())
    score.draw(batch)

  }
}

object Game {
  sealed trait State
  case object PlayingState extends State
  case object LostState extends State
  case object QuitState extends State
}
