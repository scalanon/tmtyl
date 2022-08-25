package org.scalanon.tmtyl.game

// TOD: unify all the rectangles and make them float based
final case class Rect(x: Float, y: Float, width: Float, height: Float) {
  @inline def left: Float   = x
  @inline def right: Float  = x + width
  @inline def bottom: Float = y
  @inline def top: Float    = y + height

  def intersects(ent: Entity): Boolean =
    right > ent.left && left < ent.right && top > ent.bottom && bottom < ent.top

  def intersects(rect: Rect): Boolean =
    right > rect.left && left < rect.right && top > rect.bottom && bottom < rect.top

  def isWithinX(ent: Entity): Boolean =
    right > ent.left && left < ent.right

  def isOnTop(ent: Entity): Boolean =
    right > ent.left && left < ent.right && bottom == ent.top

  def isOnTopOrIn(ent: Entity): Boolean =
    right > ent.left && left < ent.right && bottom >= ent.bottom && bottom <= ent.top

  def isOnBottom(ent: Entity): Boolean =
    right > ent.left && left < ent.right && bottom == ent.bottom

  def isOnOrAbove(ent: Entity): Boolean =
    right > ent.x && left < ent.right && bottom >= ent.top
}
