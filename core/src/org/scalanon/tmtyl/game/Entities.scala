package org.scalanon.tmtyl
package game

class Entities(entities: List[Ent]) {
  val floors: List[Floor] = entities.collectType
  val waters: List[Water] = entities.collectType
  val ladders: List[Ladder] = entities.collectType
  val doors: List[Door] = entities.collectType
}

object Entities {
  def fromLevel(level: JsonLevel): Entities = {
    val entities = for {
      jsonLayer <- level.layers
      layer <- jsonLayer.list[JsonEntityLayer]
      entity <- layer.entities
    } yield Ent(entity)
    new Entities(entities)
  }
}

sealed trait Ent {
  val x: Int
  val y: Int
  val width: Int
  val height: Int
}

object Ent {
  val DefaultSize = 16

  def apply(entity: JsonEntity): Ent = entity.name match {
    case "Floor" =>
      Floor(
        entity.x,
        entity.y,
        entity.width | DefaultSize,
        entity.height | DefaultSize
      )
    case "Water" =>
      Water(
        entity.x,
        entity.y,
        entity.width | DefaultSize,
        entity.height | DefaultSize
      )
    case "Ladder" =>
      Ladder(
        entity.x,
        entity.y,
        entity.width | DefaultSize,
        entity.height | DefaultSize
      )
    case "Door" =>
      Door(
        entity.x,
        entity.y,
        entity.width | DefaultSize,
        entity.height | DefaultSize,
        entity.value("Doorway") | ""
      )
  }
}

final case class Floor(
    x: Int,
    y: Int,
    width: Int,
    height: Int
) extends Ent

final case class Water(
    x: Int,
    y: Int,
    width: Int,
    height: Int
) extends Ent

final case class Ladder(
    x: Int,
    y: Int,
    width: Int,
    height: Int
) extends Ent

final case class Door(
    x: Int,
    y: Int,
    width: Int,
    height: Int,
    doorway: String
) extends Ent
