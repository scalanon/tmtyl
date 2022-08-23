package org.scalanon.tmtyl

import org.scalanon.tmtyl.util.{SoundWrapper, TextureWrapper}

import scala.collection.mutable

object AssetLoader {
  import Tmtyl.garbage

  private val images = mutable.Map.empty[String, TextureWrapper]
  private val sounds = mutable.Map.empty[String, SoundWrapper]

  def image(path: String): TextureWrapper =
    images.getOrElseUpdate(path, TextureWrapper.load(path))

  def sound(path: String): SoundWrapper =
    sounds.getOrElseUpdate(path, SoundWrapper.load(path))

  def clear(): Unit = {
    images.clear()
    sounds.clear()
  }
}
