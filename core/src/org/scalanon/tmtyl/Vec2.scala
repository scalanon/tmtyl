package org.scalanon.tmtyl

case class Vec2(var x: Float, var y: Float) {
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
}
