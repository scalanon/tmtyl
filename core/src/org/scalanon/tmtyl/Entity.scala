package org.scalanon.tmtyl

import com.badlogic.gdx.graphics.g2d.PolygonSpriteBatch

trait Entity {
  def update(delta: Float): List[Entity]
  def draw(batch: PolygonSpriteBatch): Unit
}
