package org.scalanon.tmtyl

import com.badlogic.gdx.graphics.g2d.PolygonSpriteBatch

trait Entity {
  def draw(batch: PolygonSpriteBatch): Unit
  def update(delta: Float): Unit
}
