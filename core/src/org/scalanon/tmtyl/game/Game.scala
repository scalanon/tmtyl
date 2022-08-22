package org.scalanon.tmtyl
package game

import com.badlogic.gdx.graphics.g2d.PolygonSpriteBatch
import com.badlogic.gdx.math.Matrix4
import org.scalanon.tmtyl.Scene
import org.scalanon.tmtyl.Tmtyl._
import org.scalanon.tmtyl.home.Home

import scala.collection.mutable

class Game extends Scene {
  import Game._

  var state: State = PlayingState
  var mouseLoc: Vec2 = Vec2(0, 0)

  val score: Score = new Score
  val matrix = new Matrix4()
  var alien: Alien = Alien(this)
  var projectiles: List[Projectile] = List.empty

  val fighter = new Fighter

  var player: Player = Player(this)
  var tiles: List[Tile] = List(
    Tile(Vec2(0, 0), tileState.Metal),
    Tile(Vec2(1, 0), tileState.Metal),
    Tile(Vec2(2, 0), tileState.Metal),
    Tile(Vec2(2, 1), tileState.Metal),
    Tile(Vec2(3, 0), tileState.Metal),
    Tile(Vec2(4, 0), tileState.Metal),
    Tile(Vec2(5, 0), tileState.Metal),
    Tile(Vec2(6, 0), tileState.Metal),
    Tile(Vec2(7, 0), tileState.Metal),
    Tile(Vec2(8, 0), tileState.Metal),
    Tile(Vec2(9, 0), tileState.Metal),
    Tile(Vec2(10, 0), tileState.Metal),
    Tile(Vec2(11, 0), tileState.Metal)
  )

  val keysPressed = mutable.Set.empty[Int]

  def keyPressed(as: Int*): Boolean = as.exists(keysPressed.contains)

  override def init(): GameControl = {
    state = PlayingState
    new GameControl(this)
  }

  override def update(delta: Float): Option[Scene] = {
    score.update(delta)
    player.update(delta)
    alien.update(delta)
    fighter.update(delta)
    projectiles.foreach(p => p.update(delta))
    PartialFunction.condOpt(state) {
      case QuitState  => new Home
      case PauseState => Home(this)
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
    alien.draw(batch)

    fighter.draw(screenUnit * 7, screenUnit, screenUnit / 16, batch)

    projectiles.foreach(p => p.draw(batch))

    batch.setTransformMatrix(matrix.idt())
    score.draw(batch)

  }
}

object Game {
  sealed trait State
  case object PlayingState extends State
  case object LostState extends State
  case object QuitState extends State
  case object PauseState extends State
}
