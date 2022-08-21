package org.scalanon.tmtyl
package home

import com.badlogic.gdx.graphics.g2d.PolygonSpriteBatch
import org.scalanon.tmtyl.Geometry.Dimension
import org.scalanon.tmtyl.Scene

// I don't like that this fades in after home fades out, but modeling this as a separate scene
// makes life so much easier.
class Settings(home: Home) extends Scene {
  import Settings._

  var alpha: Float = 0f
  var done: Boolean = false

  private val IconSize = (Dimension * 3 / 4).floor
  private val IconTop = Geometry.ScreenHeight - IconSize * 5
  private val IconSpacing = IconSize * 3

  val icons: List[Icon] = List(
    new BasicIcon(
      Geometry.ScreenWidth - IconSize * 2,
      Geometry.ScreenHeight - IconSize * 2,
      IconSize,
      Tmtyl.close,
      () => {
        done = true
      }
    )
  )

  override def init(): SettingsControl =
    new SettingsControl(this)

  override def update(delta: Float): Option[Scene] = {
    if (!done) {
      alpha = alpha.alphaUp(delta, SettingsFadeInSeconds)
      None
    } else {
      alpha = alpha.alphaDown(delta, SettingsFadeOutSeconds)
      (alpha == 0f)
        .option(home)
    }
  }

  override def render(batch: PolygonSpriteBatch): Unit = {
    icons.foreach(_.draw(batch, alpha * alpha))
  }

}

object Settings {
  val SettingsFadeInSeconds = .3f
  val SettingsFadeOutSeconds = .3f
}
