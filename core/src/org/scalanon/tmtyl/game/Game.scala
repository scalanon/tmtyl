package org.scalanon.tmtyl
package game

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.PolygonSpriteBatch
import com.badlogic.gdx.math.{MathUtils, Matrix4}
import org.scalanon.tmtyl.Scene
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

  var currentLevel: Int  = startLevel
  var level: JsonLevel   = _
  var entities: Entities = _
  val activated          = mutable.Set.empty[Int]

  // assets are loaded on frame zero and so the latency delta is
  // super high, so always ignore time delta on the next frame
  var frameZero: Boolean = _

  var player: Player = Player(this)
  var alien: Alien   = Alien(this)
  var actors         = List.empty[Actor]
  var translateX     = 0f
  var translateY     = 0f

  val control = new GameControl(this)

  setup()

  override def init(): GameControl = {
    state = PlayingState
    frameZero = true
    control
  }

  private def setup(): Unit = {
    level = LevelList(currentLevel)
    entities = Entities.fromLevel(level)
    actors = List.empty[Actor]
    activated.clear()
    control.reset()
    player.reset(calculateStartLoc)
    translateX = computeTranslateX
    translateY = computeTranslateY
    alien.loc = player.loc + Vec2(-80, 80)
  }

  def levelUp(): Unit = {
    currentLevel = currentLevel + 1
    setup()
  }

  private def calculateStartLoc: Vec2 =
    entities.start.cata(s => Vec2(s.x.toFloat, s.y.toFloat), Vec2(0, 0))

  override def update(actualDelta: Float): Option[Scene] = {
    val delta   = frameZero.fold(0f, actualDelta)
    frameZero = false
    score.update(delta)
    player.update(delta)
    alien.update(delta)
    val minX    =
      player.loc.x + player.size.x / 2 + Geometry.ScreenWidth / 2 / Geometry.ScreenPixel
    val enemies = entities.enemies
      .filter(enemy => enemy.x <= minX && activated.add(enemy.id))
      .map(enemy => Enemies.spawn(enemy, this))
    actors = enemies ::: actors.flatMap(_.update(delta))
    control.clean()
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
      case NextState  =>
        score.score += 1
        Worldmap(this)
      case EndState   =>
        ScoreIO.saveScore(score)
        EndScreen(this)
    }
  }

  def computeTranslateX: Float =
    Geometry.ScreenWidth / Geometry.ScreenPixel / 2 - player.centerX min 0f

  def computeTranslateY: Float =
    (Geometry.ScreenHeight / Geometry.ScreenPixel / 2 - player.centerY)
      .clamp(
        (Geometry.ScreenHeight / Geometry.ScreenPixel - level.height) min 0f,
        0f
      )

  override def render(batch: PolygonSpriteBatch): Unit = {
    batch.setTransformMatrix(
      matrix.setToTranslation(
        (translateX * Geometry.ScreenPixel).floor,
        translateY * Geometry.ScreenPixel,
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
        ((Geometry.ScreenWidth / Geometry.ScreenPixel - translateX) / cellWidth).ceil.toInt min layer.gridCellsX
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
        x * cellWidth * Geometry.ScreenPixel,
        yy * cellHeight * Geometry.ScreenPixel,
        cellWidth * Geometry.ScreenPixel,
        cellHeight * Geometry.ScreenPixel,
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

  val LevelList =
    List(
      Levels.newMexico,
      Levels.texas,
      Levels.oklahoma,
      Levels.arkansas,
      Levels.tennessee,
      Levels.virginia,
      Levels.dc
    )

  sealed trait State
  case object PlayingState extends State
  case object LostState    extends State
  case object QuitState    extends State
  case object PauseState   extends State
  case object NextState    extends State
  case object EndState     extends State

}
