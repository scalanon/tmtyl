package org.scalanon.tmtyl
package game

class Entities(val entities: List[Ent]) {
  val start                 = entities.collectType[Start].headOption
  val floors: List[Floor]   = entities.collectType[Floor]
  val waters: List[Water]   = entities.collectType[Water]
  val ladders: List[Ladder] = entities.collectType[Ladder]
  val doors: List[Door]     = entities.collectType[Door]
}

object Entities {
  def fromLevel(level: JsonLevel): Entities = {
    val entities = for {
      jsonLayer <- level.layers
      layer     <- jsonLayer.list[JsonEntityLayer]
      entity    <- layer.entities
    } yield Ent.fromJson(entity, layer)
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

  def fromJson(entity: JsonEntity, layer: JsonEntityLayer): Ent = {
    val width  = entity.width | layer.gridCellWidth
    val height = entity.height | layer.gridCellHeight
    val x      = entity.x
    val y      = layer.gridCellsY * layer.gridCellHeight - entity.y - height
    entity.name match {
      case "Start"  =>
        Start(
          x,
          y,
          width,
          height
        )
      case "Floor"  =>
        Floor(
          x,
          y,
          width,
          height,
          entity.value("Solid").flatMap(_.asBoolean).isTrue
        )
      case "Water"  =>
        Water(
          x,
          y,
          width,
          height
        )
      case "Ladder" =>
        Ladder(
          x,
          y,
          width,
          height
        )
      case "Door"   =>
        Door(
          x,
          y,
          width,
          height,
          entity.value("Doorway").flatMap(_.asString) | ""
        )
    }
  }
}

final case class Start(
    x: Int,
    y: Int,
    width: Int,
    height: Int
) extends Ent

final case class Floor(
    x: Int,
    y: Int,
    width: Int,
    height: Int,
    solid: Boolean
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
