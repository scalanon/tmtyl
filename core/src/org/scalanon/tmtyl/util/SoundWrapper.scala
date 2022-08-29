package org.scalanon.tmtyl
package util

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.audio.Sound
import com.badlogic.gdx.utils.Disposable
import org.scalanon.tmtyl.Prefs

class SoundWrapper(val sound: Sound) extends Disposable {
  def play(): Unit = {
    if (!Prefs.MuteAudio.isTrue) sound.play()
  }

  /* offset is in virtual pixels, on screen being [0..ScreenWidth/ScreenPixel) */
  def play(offset: Float): Unit = {
    if (!Prefs.MuteAudio.isTrue) {
      // normalized position on screen [-1..1)
      val normalized = (offset * Geometry.ScreenPixel * 2) / Geometry.ScreenWidth - 1f
      // on screen full volume, 1 screen away 2/3-volume
      val vol        = ((3 - normalized.abs) / 2f).clamp(.3f, 1f)
      val pan        =
        if (normalized < 1) (normalized + 1f).clamp(-1f, 0)
        else if (normalized > 1) (normalized - 1f).clamp(0, 1f)
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
