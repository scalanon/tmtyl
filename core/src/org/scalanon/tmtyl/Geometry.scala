package org.scalanon.tmtyl

import com.badlogic.gdx.Gdx

object Geometry {
  val ScreenWidth: Float = Gdx.graphics.getWidth.toFloat
  val ScreenHeight: Float = Gdx.graphics.getHeight.toFloat
  // dimension of one block
  val Dimension: Float =
    ((ScreenWidth * 2 / (20 * 2 + 1)) min (ScreenHeight * 2 / (20 * 2 + 5))).floor

}
