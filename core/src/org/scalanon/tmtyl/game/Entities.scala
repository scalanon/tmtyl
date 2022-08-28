package org.scalanon.tmtyl
package game

// TODO: partition horizontally
class Entities(val entities: List[Entity]) {
  val start                              = entities.collectType[Start].headOption
  val floors: List[Floor]                = entities.collectType[Floor]
  val waters: List[Water]                = entities.collectType[Water]
  val ladders: List[Ladder]              = entities.collectType[Ladder]
  val doors: List[Door]                  = entities.collectType[Door]
  val enemies: List[Enemy]               = entities.collectType[Enemy]
  val switches: List[Switch]             = entities.collectType[Switch]
  val switchMap: Map[(Int, Int), Switch] =
    switches.map(s => (s.x, s.y) -> s).toMap
}

object Entities {
  def fromLevel(level: JsonLevel): Entities = {
    val entities = for {
      jsonLayer <- level.layers
      layer     <- jsonLayer.list[JsonEntityLayer]
      entity    <- layer.entities
    } yield Entity.fromJson(entity, layer)
    new Entities(entities)
  }
}

sealed trait Entity {
  val x: Int
  val y: Int
  val width: Int
  val height: Int

  final def left   = x
  final def right  = x + width
  final def bottom = y
  final def top    = y + height
}

object Entity {

  def fromJson(entity: JsonEntity, layer: JsonEntityLayer): Entity = {
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
      case "Switch" =>
        Switch(
          x,
          y,
          width,
          height,
          entity.string("Key") | "",
          used = false
        )
      case "Floor"  =>
        Floor(
          x,
          y,
          width,
          height,
          entity.boolean("Solid").isTrue
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
          entity.string("Doorway") | "",
          entity.string("Key") | ""
        )
      case "Enemy"  =>
        Enemy(
          x,
          y,
          width,
          height,
          entity.string("Kind") | "Fighter",
          entity.string("Algorithm") | "",
          entity.id
        )
    }
  }
}

final case class Start(
    x: Int,
    y: Int,
    width: Int,
    height: Int
) extends Entity

final case class Floor(
    x: Int,
    y: Int,
    width: Int,
    height: Int,
    solid: Boolean
) extends Entity

final case class Water(
    x: Int,
    y: Int,
    width: Int,
    height: Int
) extends Entity

final case class Ladder(
    x: Int,
    y: Int,
    width: Int,
    height: Int
) extends Entity

final case class Switch(
    x: Int,
    y: Int,
    width: Int,
    height: Int,
    key: String,
    var used: Boolean
) extends Entity

final case class Door(
    x: Int,
    y: Int,
    width: Int,
    height: Int,
    doorway: String,
    key: String
) extends Entity

final case class Enemy(
    x: Int,
    y: Int,
    width: Int,
    height: Int,
    kind: String,
    algorithm: String,
    id: Int,
) extends Entity
