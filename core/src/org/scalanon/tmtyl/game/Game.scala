package org.scalanon.tmtyl
package game

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.PolygonSpriteBatch
import com.badlogic.gdx.math.{MathUtils, Matrix4}
import org.scalanon.tmtyl.Scene
import org.scalanon.tmtyl.Tmtyl._
import org.scalanon.tmtyl.game.actors.Enemies
import org.scalanon.tmtyl.home.Home
import org.scalanon.tmtyl.map.Worldmap

import scala.collection.mutable

class Game(startLevel: Int) extends Scene {
  import Game._

  var state: State   = PlayingState
  var mouseLoc: Vec2 = Vec2(0, 0)

  val score: Score = new Score
  val matrix       = new Matrix4()

  var levelList    =
    List(
      Levels.newMexico,
      Levels.texas,
      Levels.oklahoma,
      Levels.arkansas,
      Levels.tennessee,
      Levels.virginia,
      Levels.dc
    )
  var currentLevel = startLevel
  var level        = levelList(currentLevel)
  var entities     = Entities.fromLevel(level)
  var activated    = mutable.Set.empty[Int]
  var timer        = 0f

  var player: Player = Player(this)
  var alien: Alien   = Alien(this)
  var actors         = List.empty[Actor]
  var translateX     = computeTranslateX
  var translateY     = computeTranslateY

  val keysPressed    = mutable.Set.empty[Int]
  val newKeysPressed = mutable.Set.empty[Int]

  def keyPressed(as: Int*): Boolean    = as.exists(keysPressed.contains)
  def newKeyPressed(as: Int*): Boolean = as.exists(newKeysPressed.contains)

  override def init(): GameControl = {
    state = PlayingState
    new GameControl(this)
  }

  def switchToLevel(nlev: Int): Unit = {
    score.score += 1
    timer = 0f
    currentLevel = nlev
    if (currentLevel == 7) {
      state = EndState
    } else {
      level = levelList(currentLevel)
      entities = Entities.fromLevel(level)
      actors = List.empty[Actor]
      activated.clear()
      player.loc =
        entities.start.cata(s => Vec2(s.x.toFloat, s.y.toFloat), Vec2(0, 0))
      player.vel = Vec2(0, 0)
      keysPressed.clear()
      alien.loc = player.loc + Vec2(-80, 80)
      translateX = computeTranslateX
      translateY = computeTranslateY

      state = MapState
    }
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
      .map(enemy => Enemies.spawn(enemy, this))
    actors = enemies ::: actors.flatMap(_.update(delta))
    newKeysPressed.clear()
    val targetX = computeTranslateX
    val targetY = computeTranslateY
    if (targetX != translateX || targetY != translateY) {
      val translateA =
        MathUtils.atan2(targetY - translateY, targetX - translateX)
      val dX         = delta * TranslateSpeed * MathUtils.cos(translateA)
      val dY         = delta * TranslateSpeed * MathUtils.sin(translateA)
      translateX =
        if (targetX > translateX)
          (translateX + dX) min targetX
        else (translateX + dX) max targetX
      translateY =
        if (targetY > translateY)
          (translateY + dY) min targetY
        else (translateY + dY) max targetY
    }
    PartialFunction.condOpt(state) {
      case QuitState  => new Home
      case LostState  =>
        ScoreIO.saveScore(score)
        new DeadScreen(this)
      case PauseState => Home(this)
      case MapState   => Worldmap(this)
      case EndState   =>
        ScoreIO.saveScore(score)
        EndScreen(this)
    }
  }

  def computeTranslateX: Float =
    Geometry.ScreenWidth / screenPixel / 2 - player.centerX

  def computeTranslateY: Float =
    (Geometry.ScreenHeight / screenPixel / 2 - player.centerY)
      .clamp((Geometry.ScreenHeight / screenPixel - level.height) min 0, 0f)

  override def render(batch: PolygonSpriteBatch): Unit = {
    batch.setTransformMatrix(
      matrix.setToTranslation(
        (translateX * screenPixel).floor,
        translateY * screenPixel,
        0
      )
    )

    drawLevel(batch)

    player.draw(batch)
    alien.draw(batch)
    actors.foreach(_.draw(batch))

    batch.setTransformMatrix(matrix.idt())
    // score.draw(batch)
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
  case object EndState     extends State

}
