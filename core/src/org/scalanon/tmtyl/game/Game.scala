package org.scalanon.tmtyl
package game

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.PolygonSpriteBatch
import com.badlogic.gdx.math.{MathUtils, Matrix4}
import org.scalanon.tmtyl.Scene
import org.scalanon.tmtyl.Tmtyl._
import org.scalanon.tmtyl.game.entities.Missile
import org.scalanon.tmtyl.home.Home

import scala.collection.mutable

class Game extends Scene {
  import Game._

  var state: State   = PlayingState
  var mouseLoc: Vec2 = Vec2(0, 0)

  val score: Score = new Score
  val matrix       = new Matrix4()

  val fighter  = new Fighter
  var level    = Levels.level1
  var entities = Entities.fromLevel(level)

  var player: Player = Player(this)
  var alien: Alien   = Alien(this)
  var fx             = List.empty[Entity]

  val keysPressed    = mutable.Set.empty[Int]
  val newKeysPressed = mutable.Set.empty[Int]

  def keyPressed(as: Int*): Boolean    = as.exists(keysPressed.contains)
  def newKeyPressed(as: Int*): Boolean = as.exists(newKeysPressed.contains)

  override def init(): GameControl = {
    state = PlayingState
    new GameControl(this)
  }

  override def update(delta: Float): Option[Scene] = {
    score.update(delta)
    player.update(delta)
    alien.update(delta)
    fighter.update(delta)
    fx = fx.flatMap(_.update(delta))
    if (MathUtils.randomBoolean(.01f))
      fx = Missile.launch(this).cata(_ :: fx, fx)
    newKeysPressed.clear()
    PartialFunction.condOpt(state) {
      case QuitState  => new Home
      case PauseState => Home(this)
    }
  }

  override def render(batch: PolygonSpriteBatch): Unit = {
    val translationX =
      (Geometry.ScreenWidth / 2 - ((player.loc.x + (player.size.x / 2)) * screenPixel)).floor
    batch.setTransformMatrix(
      matrix.setToTranslation(translationX, 0, 0)
    )

    drawLevel(batch)

    player.draw(batch)
    alien.draw(batch)

    fighter.draw(
      screenPixel * 64 * 16,
      screenPixel * 3 * 16,
      screenPixel,
      batch
    )
    fx.foreach(_.draw(batch))

    batch.setTransformMatrix(matrix.idt())
    score.draw(batch)

  }

  private def drawLevel(batch: PolygonSpriteBatch): Unit = {
    batch.setColor(Color.WHITE)
    for {
      jsonLayer <- level.layers.reverse
      layer     <- jsonLayer.option[JsonTileLayer]
      tileset    = Tmtyl.tilesets(layer.tileset)
      xUnit      = layer.gridCellWidth * screenPixel
      yUnit      = layer.gridCellHeight * screenPixel
      y         <- 0 until layer.gridCellsY
      row        = layer.data2D(y)
      x         <- 0 until layer.gridCellsX
      tile      <- tileset.tile(row(x))
    } {
      batch.draw(
        tileset.texture,
        x * xUnit,
        (layer.gridCellsY - y - 1) * yUnit,
        xUnit,
        yUnit,
        tile.x,
        tile.y,
        tile.width,
        tile.height,
        false,
        false
      )
    }
  }
}

object Game {
  sealed trait State
  case object PlayingState extends State
  case object LostState    extends State
  case object QuitState    extends State
  case object PauseState   extends State
}
