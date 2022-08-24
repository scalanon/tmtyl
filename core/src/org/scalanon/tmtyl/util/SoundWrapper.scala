package org.scalanon.tmtyl
package util

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.audio.Sound
import com.badlogic.gdx.utils.Disposable
import org.scalanon.tmtyl.Prefs
import org.scalanon.tmtyl.Tmtyl.screenPixel

class SoundWrapper(val sound: Sound) extends Disposable {
  def play(): Unit = {
    if (!Prefs.MuteAudio.isTrue) sound.play()
  }

  /* offset is in virtual pixels, on screen being [0..ScreenWidth/screenPixel] */
  def play(offset: Float): Unit = {
    if (!Prefs.MuteAudio.isTrue) {
      // -1 left, 1 right
      val relative = (offset * screenPixel * 2) / Geometry.ScreenWidth - 1f
      // on screen full volume, 1 screen away 2/3-volume
      val vol      = ((3 - relative.abs) / 2f).clamp(.3f, 1f)
      val pan      =
        if (relative < 1) (relative + 1f).clamp(-1f, 0)
        else if (relative > 1) (relative - 1f).clamp(0, 1f)
        else 0f
      sound.play(vol, 1f, pan)
    }
  }

  override def dispose(): Unit = {
    sound.dispose()
  }
}

object SoundWrapper {
  def load(path: String)(implicit garbageCan: GarbageCan): SoundWrapper = {
    val fileHandle = Gdx.files.internal(path)
    val sound      = Gdx.audio.newSound(fileHandle)
    garbageCan.add(new SoundWrapper(sound))
  }

}
