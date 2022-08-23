package org.scalanon.tmtyl
package home

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.PolygonSpriteBatch
import org.scalanon.tmtyl.Geometry._
import org.scalanon.tmtyl.game.Game
import org.scalanon.tmtyl.{Scene, Tmtyl}

class Home(paused: Option[Game] = None) extends Scene {

  import Home._

  var state: State = HomeState
  var logoAlpha    = 0f
  var playAlpha    = 0f
  var ufoPos       = 0f
  var alienPos     = 0f
  var discard      = false
  var alien        = new Alien(new AlienAnimation.Dead)
  var ready        = false

  private val LogoPixel   = (Geometry.ScreenWidth * 2 / 3 / ufo.width).floor
  private val UfoWidth    = LogoPixel * ufo.width
  private val UfoHeight   = LogoPixel * ufo.height
  private val AlienWidth  = LogoPixel * alien.width
  private val AlienHeight = LogoPixel * alien.height

  private val IconSize     = LogoPixel * 11 / 2
  private val HighScorePos =
    Geometry.ScreenHeight - IconSize * 2

  private val baseIcons: List[Icon] = List(
    new PlayIcon(
      LogoPixel * 8,
      LogoPixel * 12,
      LogoPixel,
      LogoPixel * 9,
      this
    ),
    new PrefIcon(
      IconSize,
      Geometry.ScreenHeight - IconSize,
      IconSize,
      Prefs.MuteAudio,
      soundOff,
      soundOn
    ),
    new BasicIcon(
      Geometry.ScreenWidth - IconSize,
      Geometry.ScreenHeight - IconSize,
      IconSize,
      settings,
      () => {
        state = SettingsState
      }
    )
  )

  private val iconsWithDiscard = new BasicIcon(
    Geometry.ScreenWidth / 2 - Dimension * 9 / 4, // failure to get real dimensions
    HighScorePos - Text.smallFont.getAscent,
    IconSize / 2,
    Tmtyl.trash,
    () => {
      discard = true
    },
    HighScoreColor
  ) :: baseIcons

  def icons: List[Icon] =
    (paused.isDefined && !discard).fold(iconsWithDiscard, baseIcons)

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
        alien.animation match {
          case _: AlienAnimation.Dead                =>
            alien.animation = new AlienAnimation.Reanimate
          case r: AlienAnimation.Reanimate if r.done =>
            alien.animation = new AlienAnimation.Idle
          case i: AlienAnimation.Idle if i.frame > 0 =>
            ready = true
          case _                                     =>
        }
        if (ready)
          playAlpha = playAlpha.alphaUp(delta, PlayFadeInSeconds)
      }
      None
    } else {
      logoAlpha = logoAlpha.alphaDown(delta, LogoFadeOutSeconds)
      playAlpha = playAlpha.alphaDown(delta, PlayFadeOutSeconds)
      if (state == SettingsState) {
        (logoAlpha + playAlpha == 0f)
          .option(new Settings(this))
      } else /*if (state == PlayState)*/
        {
          (logoAlpha + playAlpha == 0f)
            .option(nextGame)
        }
    }
  }

  private def nextGame: Game =
    paused.filterNot(_ => discard).getOrElse(new Game)

  override def render(batch: PolygonSpriteBatch): Unit = {
    drawLogo(batch)
    icons.foreach(_.draw(batch, playAlpha * playAlpha))
    if (paused.isDefined && !discard) {
      drawPaused(batch)
    } else {
//      for {
//        score <- Prefs.HighScore.intValue
//      } drawHighScore(batch, score)
    }
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
        (Geometry.ScreenWidth - AlienWidth) / 2,
        ufoY - AlienHeight / 2 - Math
          .sqrt(alienPos)
          .toFloat * AlienHeight * 3 / 2,
        LogoPixel,
        batch
      )
    }
    batch.draw(
      ufo,
      (Geometry.ScreenWidth - UfoWidth) / 2,
      ufoY - UfoHeight / 2,
      UfoWidth,
      UfoHeight
    )
    if (ready) {
      val LogoWidth  = logo.width * LogoPixel
      val LogoHeight = logo.height * LogoPixel
      val extra      = alien.animation match {
        case i: AlienAnimation.Idle if i.frame % 2 == 1 && !i.blink => 3
        case _ => 2
      }
      batch.draw(
        logo,
        (Geometry.ScreenWidth - LogoWidth + AlienWidth * 3 / 4) / 2,
        (Geometry.ScreenHeight - LogoHeight) / 2 - extra * LogoPixel,
        LogoWidth,
        LogoHeight
      )
    }
  }

  private def drawPaused(
      batch: PolygonSpriteBatch
  ): Unit = {
    val color = HighScoreColor ⍺ (playAlpha * playAlpha)
    Text.draw(
      batch,
      Text.smallFont,
      color,
      "Game Paused",
      HighScorePos
    )
  }

  private def drawHighScore(
      batch: PolygonSpriteBatch,
      score: Int
  ): Unit = {
    val color = HighScoreColor ⍺ (playAlpha * playAlpha)
    Text.draw(
      batch,
      Text.smallFont,
      color,
      f"High Score: $score%,d",
      HighScorePos
    )
  }

  def play(): Unit = {
    state = PlayState
  }

  private def logo = AssetLoader.image("tmtyl.png")
  private def ufo  = AssetLoader.image("ufo.png")

  private def soundOff = AssetLoader.image("sound-off.png")
  private def soundOn  = AssetLoader.image("sound-on.png")
  private def settings = AssetLoader.image("settings.png")

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

  case object HomeState     extends State
  case object SettingsState extends State
  case object PlayState     extends State
}
