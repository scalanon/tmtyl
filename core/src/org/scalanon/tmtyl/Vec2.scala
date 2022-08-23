package org.scalanon.tmtyl

final case class Vec2(var x: Float, var y: Float) {
  def +(vec22: Vec2): Vec2 = {
    Vec2(
      this.x + vec22.x,
      this.y + vec22.y
    )
  }
  def -(vec22: Vec2): Vec2 = {
    Vec2(
      this.x - vec22.x,
      this.y - vec22.y
    )
  }
  def *(float: Float): Vec2 = {
    Vec2(
      this.x * float,
      this.y * float
    )
  }
  def /(float: Float): Vec2 = {
    Vec2(
      this.x / float,
      this.y / float
    )
  }

  def add(dx: Float, dy: Float): Unit = {
    x += dx
    y += dy
  }

  def mulAdd(dx: Float, dy: Float, mul: Float): Unit = {
    x += dx * mul
    y += dy * mul
  }

  def mulAdd(v: Vec2, mul: Float): Unit = {
    x += v.x * mul
    y += v.y * mul
  }
}

object Vec2 {
  def zero: Vec2 = new Vec2(0, 0)
}
