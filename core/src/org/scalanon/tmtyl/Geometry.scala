package org.scalanon.tmtyl

import com.badlogic.gdx.Gdx

object Geometry {
  val ScreenWidth: Float  = Gdx.graphics.getWidth.toFloat
  val ScreenHeight: Float = Gdx.graphics.getHeight.toFloat
  val ScreenPixel: Float  = (Geometry.ScreenWidth / 320).floor
  val Dimension: Float    = ScreenPixel * 16
  val WideScreen: Boolean = ScreenWidth >= Geometry.ScreenHeight * 3f / 2f
}
