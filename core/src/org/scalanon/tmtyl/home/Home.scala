package org.scalanon.tmtyl
package home

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.PolygonSpriteBatch
import org.scalanon.tmtyl.game.Game
import org.scalanon.tmtyl.{Scene, Tmtyl}

class Home(paused: Option[Game] = None) extends Scene with Playable {

  import Home._

  var state: State = HomeState
  var logoAlpha    = 0f
  var playAlpha    = 0f
  var ufoPos       = 0f
  var alienPos     = 0f
  var discard      = false
  var alien        = new Alien(new AlienAnimation.Dead)
  var ready        = false

  private val HomeWidth   =
    Geometry.WideScreen.fold(Geometry.ScreenWidth * 3 / 4, Geometry.ScreenWidth)
  private val HomePixel   = (Geometry.ScreenHeight * 2 / 3 / ufo.height).floor
  private val UfoWidth    = HomePixel * ufo.width
  private val UfoHeight   = HomePixel * ufo.height
  private val AlienWidth  = HomePixel * alien.width
  private val AlienHeight = HomePixel * alien.height

  private val IconSize = HomePixel * 11 / 2

  val icons: List[Icon] = List(
    playIcon,
    new PrefIcon(
      IconSize * 3 / 2,
      Geometry.ScreenHeight - IconSize * 3 / 2,
      IconSize,
      Prefs.MuteAudio,
      soundOff,
      soundOn
    )
  )

  def playIcon: Icon = {
    val widescreenX =
      (Geometry.ScreenWidth + (HomeWidth + UfoWidth + 6 * HomePixel) / 2) / 2
    val (x, y)      = Geometry.WideScreen.fold(
      (widescreenX, Geometry.ScreenHeight / 2),
      ((Geometry.ScreenWidth / 2 - 5 * HomePixel) / 2, 16 * HomePixel)
    )
    new PlayIcon(
      x,
      y,
      HomePixel,
      HomePixel * 9,
      this
    )
  }

  override def init(): HomeControl = {
    state = HomeState
    ScoreIO.loadScore()
    new HomeControl(this)
  }

  override def update(delta: Float): Option[Scene] = {
    alien.update(delta)
    if (state == HomeState) {
      logoAlpha = logoAlpha.alphaUp(delta, LogoFadeInSeconds)
      ufoPos = ufoPos.alphaUp(delta, UfoMoveInSeconds)
      if (ufoPos >= 1f)
        alienPos = alienPos.alphaUp(delta, AlienMoveInSeconds)
      if (alienPos >= 1f) {
        if (delta >= 10f) {
          alien.animation = new AlienAnimation.Idle
          ready = true
        } else {
          alien.animation match {
            case _: AlienAnimation.Dead                =>
              alien.animation = new AlienAnimation.Reanimate
            case r: AlienAnimation.Reanimate if r.done =>
              alien.animation = new AlienAnimation.Idle
            case i: AlienAnimation.Idle if i.frame > 0 =>
              ready = true
            case _                                     =>
          }
        }
        if (ready)
          playAlpha = playAlpha.alphaUp(delta, PlayFadeInSeconds)
      }
      None
    } else {
      logoAlpha = logoAlpha.alphaDown(delta, LogoFadeOutSeconds)
      playAlpha = playAlpha.alphaDown(delta, PlayFadeOutSeconds)
      (logoAlpha + playAlpha == 0f)
        .option(nextGame)
    }
  }

  private def nextGame: Game =
    paused.filterNot(_ => discard).getOrElse(new Game(0))

  override def render(batch: PolygonSpriteBatch): Unit = {
    drawLogo(batch)
    icons.foreach(_.draw(batch, playAlpha * playAlpha))
    Text.draw(
      batch,
      Text.tinyFont,
      Color.DARK_GRAY ⍺ (.25f * playAlpha),
      s"v${Tmtyl.version}",
      l =>
        (Geometry.ScreenWidth - l.width - Geometry.Dimension / 4) -> (l.height + Geometry.Dimension / 4)
    )
  }

  private def drawLogo(batch: PolygonSpriteBatch): Unit = {
    batch.setColor(1, 1, 1, logoAlpha * logoAlpha)
    val ufoY =
      Geometry.ScreenHeight * (8f - 3f * Math.sqrt(ufoPos).toFloat) / 7f
    if (alienPos > 0f) {
      alien.draw(
        (HomeWidth - AlienWidth) / 2,
        ufoY - AlienHeight / 2 - Math
          .sqrt(alienPos)
          .toFloat * AlienHeight * 3 / 2,
        HomePixel,
        batch
      )
    }
    batch.draw(
      ufo,
      (HomeWidth - UfoWidth) / 2,
      ufoY - UfoHeight / 2,
      UfoWidth,
      UfoHeight
    )
    if (ready) {
      val LogoWidth  = logo.width * HomePixel
      val LogoHeight = logo.height * HomePixel
      val extra      = alien.animation match {
        case i: AlienAnimation.Idle if i.frame % 2 == 1 && !i.blink => 3
        case _ => 2
      }
      batch.draw(
        logo,
        (HomeWidth - LogoWidth + AlienWidth * 3 / 4) / 2,
        (Geometry.ScreenHeight - LogoHeight) / 2 - extra * HomePixel,
        LogoWidth,
        LogoHeight
      )
    }
  }

  def play(): Unit = {
    state = PlayState
  }

  private def logo = AssetLoader.image("tmtyl.png")
  private def ufo  = AssetLoader.image("ufo.png")

  private def soundOff = AssetLoader.image("sound-off.png")
  private def soundOn  = AssetLoader.image("sound-on.png")
}

object Home {
  def apply(game: Game): Home = new Home(Some(game))

  val LogoFadeInSeconds  = 1f
  val PlayDelaySeconds   = 0.3f
  val PlayFadeInSeconds  = .3f
  val UfoMoveInSeconds   = 1.5f
  val AlienMoveInSeconds = 1.5f

  val LogoFadeOutSeconds = .5f
  val PlayFadeOutSeconds = .3f

  val Title = "TAKE ME TO YOUR LEADER!"

  val HighScoreColor = new Color(.7f, .7f, .7f, 1f)

  sealed trait State

  case object HomeState extends State
  case object PlayState extends State
}
