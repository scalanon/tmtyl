package org.scalanon.tmtyl

import com.badlogic.gdx.graphics.g2d.PolygonSpriteBatch

trait Entity {
  var loc: Vec2
  var size: Vec2
  def draw(batch: PolygonSpriteBatch): Unit
  def update(delta: Float): Unit
}
