package org.scalanon.tmtyl

import com.badlogic.gdx.Application.ApplicationType
import com.badlogic.gdx.graphics.g2d.PolygonSpriteBatch
import com.badlogic.gdx.utils.ScreenUtils
import com.badlogic.gdx.{ApplicationAdapter, Gdx, Input}
import org.scalanon.tmtyl.game.Tilesets
import org.scalanon.tmtyl.home.Home
import org.scalanon.tmtyl.util.GarbageCan

import java.util.Properties

class Tmtyl extends ApplicationAdapter {
  import Tmtyl._

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

    Tmtyl.tilesets = Tilesets.load()

    Text.loadFonts()

    setScene(new Home)
  }

  override def render(): Unit = {
    val delta = Gdx.graphics.getDeltaTime
    scene.update(delta min MaxDeltaTime) foreach setScene
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

  // limit slowest effective frame rate to 20fps
  private val MaxDeltaTime = .05f

  var version: String = _
  var key: String     = _
  var globalHigh: Int = _

  var tilesets: Tilesets = _

  def mobile: Boolean = isMobile(Gdx.app.getType)

  private def isMobile(tpe: ApplicationType) =
    tpe == ApplicationType.Android || tpe == ApplicationType.iOS
}
