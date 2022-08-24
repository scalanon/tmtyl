package org.scalanon.tmtyl
package game

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.PolygonSpriteBatch
import com.badlogic.gdx.math.{MathUtils, Matrix4}
import org.scalanon.tmtyl.Scene
import org.scalanon.tmtyl.Tmtyl._
import org.scalanon.tmtyl.game.actors.{Fighter, Missile}
import org.scalanon.tmtyl.home.Home

import scala.collection.mutable

class Game extends Scene {
  import Game._

  var state: State   = PlayingState
  var mouseLoc: Vec2 = Vec2(0, 0)

  val score: Score = new Score
  val matrix       = new Matrix4()

  var levelList    =
    List(Levels.newMexico, Levels.level1, Levels.level2, Levels.level3)
  var currentLevel = 0
  var level        = levelList(1)
  var entities     = Entities.fromLevel(level)
  var activated    = mutable.Set.empty[Int]
  var timer        = 0f

  var player: Player = Player(this)
  var alien: Alien   = Alien(this)
  var actors         = List.empty[Actor]
  var translateX     = Geometry.ScreenWidth / screenPixel / 2 - player.centerX

  val keysPressed    = mutable.Set.empty[Int]
  val newKeysPressed = mutable.Set.empty[Int]

  def keyPressed(as: Int*): Boolean    = as.exists(keysPressed.contains)
  def newKeyPressed(as: Int*): Boolean = as.exists(newKeysPressed.contains)

  override def init(): GameControl = {
    state = PlayingState
    new GameControl(this)
  }

  def nextLevel(): Unit = {
    score.score = ((100 / timer) + (10 / timer)).toInt
    timer = 0f
    currentLevel = currentLevel + 1
    level = levelList(currentLevel)
    entities = Entities.fromLevel(level)
    actors = List.empty[Actor]
    activated.clear()
    player.loc =
      entities.start.cata(s => Vec2(s.x.toFloat, s.y.toFloat), Vec2(0, 0))
    player.vel = Vec2(0, 0)
    keysPressed.clear()
    alien.loc = player.loc + Vec2(-80, 80)
    translateX = Geometry.ScreenWidth / screenPixel / 2 - player.centerX
    state = MapState
  }

  override def update(delta: Float): Option[Scene] = {
    timer += delta
    score.update(delta)
    player.update(delta)
    alien.update(delta)
    val minX    =
      player.loc.x + player.size.x / 2 + Geometry.ScreenWidth / 2 / screenPixel
    val enemies = entities.enemies
      .filter(enemy => enemy.x <= minX && activated.add(enemy.id))
      .map(enemy => new Fighter(enemy.x.toFloat, enemy.y.toFloat, this))
    actors = enemies ::: actors.flatMap(_.update(delta))
    if (MathUtils.randomBoolean(.01f))
      actors = Missile.launch(this).cata(_ :: actors, actors)
    newKeysPressed.clear()
    val targetX =
      Geometry.ScreenWidth / screenPixel / 2 - player.centerX
    translateX =
      if (targetX > translateX)
        (translateX + TranslateSpeed * delta) min targetX
      else (translateX - TranslateSpeed * delta) max targetX
    PartialFunction.condOpt(state) {
      case QuitState  => new Home
      case PauseState => Home(this)
      case MapState   => Worldmap(currentLevel, this)
    }
  }

  override def render(batch: PolygonSpriteBatch): Unit = {
    batch.setTransformMatrix(
      matrix.setToTranslation((translateX * screenPixel).floor, 0, 0)
    )

    drawLevel(batch)

    player.draw(batch)
    alien.draw(batch)
    actors.foreach(_.draw(batch))

    batch.setTransformMatrix(matrix.idt())
    score.draw(batch)

  }

  private def drawLevel(
      batch: PolygonSpriteBatch
  ): Unit = {
    batch.setColor(Color.WHITE)
    for {
      jsonLayer <- level.layers.reverse
      layer     <- jsonLayer.option[JsonTileLayer]
      tileset    = Tmtyl.tilesets(layer.tileset)
      cellWidth  = layer.gridCellWidth
      cellHeight = layer.gridCellHeight
      minX       = ((0 - translateX) / cellWidth).floor.toInt max 0
      maxX       =
        ((Geometry.ScreenWidth / screenPixel - translateX) / cellWidth).ceil.toInt min layer.gridCellsX
      y         <- 0 until layer.gridCellsY
      yy         = layer.gridCellsY - y - 1
      row        = layer.data2D(y)
      x         <- minX until maxX
      tile      <- tileset.tile(row(x))
    } {
      val switch = entities.switchMap.get(x * cellWidth -> yy * cellHeight)
      if (switch.exists(s => !s.used) && layer.name == "Second Layer")
        batch.setColor(0.5f, 0.5f, 0.5f, 0.5f)
      else batch.setColor(Color.WHITE)
      batch.draw(
        tileset.texture,
        x * cellWidth * screenPixel,
        yy * cellHeight * screenPixel,
        cellWidth * screenPixel,
        cellHeight * screenPixel,
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
  val TranslateSpeed = 300f

  sealed trait State
  case object PlayingState extends State
  case object LostState    extends State
  case object QuitState    extends State
  case object PauseState   extends State
  case object MapState     extends State
}
