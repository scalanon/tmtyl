package org.scalanon.tmtyl.util

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.audio.Sound
import com.badlogic.gdx.utils.Disposable
import org.scalanon.tmtyl.Prefs

class SoundWrapper(val sound: Sound) extends Disposable {
  // TODO: implicit to sound conversion that returns a dummy sound if muted, so no need to proxy all methods
  def play(): Unit =
    if (!Prefs.MuteAudio.isTrue) sound.play()

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
