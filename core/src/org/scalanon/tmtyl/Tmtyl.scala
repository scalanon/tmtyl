package org.scalanon.tmtyl

import com.badlogic.gdx.Application.ApplicationType
import com.badlogic.gdx.graphics.g2d.PolygonSpriteBatch
import com.badlogic.gdx.utils.ScreenUtils
import com.badlogic.gdx.{ApplicationAdapter, Gdx, Input}
import org.scalanon.tmtyl.game.{Game, Levels, Tilesets}
import org.scalanon.tmtyl.util.{GarbageCan, TextureWrapper}

import java.util.Properties

class Tmtyl extends ApplicationAdapter {
  import Tmtyl.garbage

  private var batch: PolygonSpriteBatch = _
  private var scene: Scene              = _

  override def create(): Unit = {

    Gdx.input.setCatchKey(Input.Keys.BACK, true)

    Prefs.loadPreferences()

    batch = garbage.add(new PolygonSpriteBatch())

    val properties = new Properties
    val is         = Tmtyl.getClass.getResourceAsStream("/app.properties")
    if (is ne null) {
      properties.load(is)
      is.close()
    }
    Tmtyl.version = properties.getProperty("version", "Unknown")
    Tmtyl.key = properties.getProperty("key", "unset")

    Tmtyl.logo = TextureWrapper.load("tmtyl.png")
    Tmtyl.play = TextureWrapper.load("play.png")
    Tmtyl.ufo = TextureWrapper.load("ufo.png")
    Tmtyl.alien = TextureWrapper.load("alien.png")

    Tmtyl.soundOff = TextureWrapper.load("sound-off.png")
    Tmtyl.soundOn = TextureWrapper.load("sound-on.png")
    Tmtyl.settings = TextureWrapper.load("settings.png")
    Tmtyl.help = TextureWrapper.load("help.png")
    Tmtyl.close = TextureWrapper.load("close.png")
    Tmtyl.checkOn = TextureWrapper.load("check-on.png")
    Tmtyl.checkOff = TextureWrapper.load("check-off.png")
    Tmtyl.trash = TextureWrapper.load("trash.png")
    Tmtyl.walkPlayer = TextureWrapper.load("tiny_adventurer_sheet.png")

    Tmtyl.tilesets = Tilesets.load()

    Text.loadFonts()

    setScene(new Game) //setScene(new Home)
  }

  override def render(): Unit = {
    val delta = Gdx.graphics.getDeltaTime
    scene.update(delta) foreach setScene
    ScreenUtils.clear(0, 0, 0, 1)
    batch.begin()
    scene.render(batch)
    batch.end()
  }

  override def dispose(): Unit = {
    garbage.dispose()
    AssetLoader.clear()
  }

  private def setScene(newScene: Scene): Unit = {
    scene = newScene
    Gdx.input.setInputProcessor(scene.init())
  }

}

object Tmtyl {
  implicit val garbage: GarbageCan = new GarbageCan

  var version: String = _
  var key: String     = _

  val screenPixel =
    ((Geometry.ScreenWidth min Geometry.ScreenHeight) / 320).floor

  var levelList    = List(Levels.level1, Levels.level2, Levels.level3)
  var currentLevel = 0

  var logo: TextureWrapper  = _
  var play: TextureWrapper  = _
  var ufo: TextureWrapper   = _
  var alien: TextureWrapper = _

  var soundOff: TextureWrapper   = _
  var soundOn: TextureWrapper    = _
  var help: TextureWrapper       = _
  var settings: TextureWrapper   = _
  var close: TextureWrapper      = _
  var checkOn: TextureWrapper    = _
  var checkOff: TextureWrapper   = _
  var trash: TextureWrapper      = _
  var walkPlayer: TextureWrapper = _

  var tilesets: Tilesets = _

  var globalHigh: Int = _

  def mobile: Boolean = isMobile(Gdx.app.getType)

  private def isMobile(tpe: ApplicationType) =
    tpe == ApplicationType.Android || tpe == ApplicationType.iOS
}
