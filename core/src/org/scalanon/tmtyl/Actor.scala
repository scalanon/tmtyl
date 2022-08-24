package org.scalanon.tmtyl

import com.badlogic.gdx.graphics.g2d.PolygonSpriteBatch

trait Actor {
  def update(delta: Float): List[Actor]
  def draw(batch: PolygonSpriteBatch): Unit
}
