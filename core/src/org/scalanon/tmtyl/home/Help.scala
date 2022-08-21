package org.scalanon.tmtyl
package home

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.PolygonSpriteBatch
import org.scalanon.tmtyl.Geometry._
import org.scalanon.tmtyl.Scene
import org.scalanon.tmtyl.game.Game
import org.scalanon.tmtyl.util.TextureWrapper

class Help(home: Home, game: Option[Game] = None) extends Scene {

  import Help._

  var state: State = HelpState
  var alpha: Float = 0f
  var instructed: Float = 0f

  private val IconSize = (Dimension * 3 / 4).floor

  val closeIcon = List(
    new BasicIcon(
      Geometry.ScreenWidth - IconSize * 2,
      Geometry.ScreenHeight - IconSize * 2,
      IconSize,
      Tmtyl.close,
      () => {
        state = ExitState
      }
    )
  )

  def icons: List[Icon] = game.isDefined.fold(Nil, closeIcon)

  override def init(): HelpControl =
    new HelpControl(this)

  override def update(delta: Float): Option[Scene] = {
    if (state == HelpState) {
      alpha = alpha.alphaUp(delta, InstructionsFadeInSeconds)
      if (game.isDefined) {
        instructed = instructed + delta
        if (instructed >= AutoInstructionsSeconds) continue()
      }
      None
    } else {
      alpha = alpha.alphaDown(delta, InstructionsFadeOutSeconds)
      (alpha == 0f)
        .option(game.filter(_ => state == ContinueState).getOrElse(home))
    }
  }

  override def render(batch: PolygonSpriteBatch): Unit = {
    desktopIcons.foreach(_.draw(batch, alpha * alpha))
    icons.foreach(_.draw(batch, alpha * alpha))
  }

  private val DesktopIconLeft = IconSize * 2
  private val DesktopIconInterval = IconSize * 3
  private val DesktopIconsTop = Geometry.ScreenHeight - IconSize * 5

  val desktopIcons: List[Icon] = List(
//    new KeyIcon(
//      DesktopIconLeft,
//      DesktopIconsTop,
//      IconSize,
//      Tmtyl.arrowKey,
//      0f,
//      "Right",
//      "Right arrow key"
//    ),
  )


  def exit(): Unit = {
    state = ExitState
  }

  def continue(): Unit = {
    state = ContinueState
  }
}

object Help {
  val InstructionsFadeInSeconds = .3f
  val InstructionsFadeOutSeconds = .3f
  val AutoInstructionsSeconds = 5f

  val Red = new Color(.855f, .075f, .102f, 1f)
  val Yellow = new Color(1f, .937f, 0f, 1f)
  val White = new Color(.7f, .7f, .7f, 1f)

  sealed trait State
  case object HelpState extends State
  case object ExitState extends State
  case object ContinueState extends State

  private final case class MobileHelp(
      icon: TextureWrapper,
      label: String,
      desc: String,
      x: Int,
      y: Int
  )
}
