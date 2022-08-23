package org.scalanon.tmtyl.game

import cats.implicits.toFunctorOps
import com.badlogic.gdx.Gdx
import io.circe.{Decoder, Json}
import io.circe.generic.auto._
import io.circe.parser._

import scala.reflect.ClassTag

object Levels {
  lazy val level1 = load("Level1.json")

  lazy val level2 = load("Level2.json")

  private def load(level: String): JsonLevel = {
    val json = Gdx.files.internal(s"ogmo/$level").readString("UTF-8")
    decode[JsonLevel](json).toTry.get
  }
  lazy val level2 = {
    val json = Gdx.files.internal("ogmo/Level2.json").readString("UTF-8")
    decode[JsonLevel](json).toTry.get
  }
}

final case class JsonLevel(
    ogmoVersion: String,
    width: Int,
    height: Int,
    offsetX: Int,
    offsetY: Int,
    layers: List[JsonLayer]
)

sealed trait JsonLayer {
  def option[A <: JsonLayer: ClassTag]: Option[A] =
    implicitly[ClassTag[A]].unapply(this)

  def list[A <: JsonLayer: ClassTag]: List[A] = option[A].toList
}

object JsonLayer {
  implicit val decodeJsonLayer: Decoder[JsonLayer] =
    List[Decoder[JsonLayer]](
      Decoder[JsonTileLayer].widen,
      Decoder[JsonEntityLayer].widen
    ).reduceLeft(_ or _)
}

final case class JsonTileLayer(
    _eid: String,
    name: String,
    offsetX: Int,
    offsetY: Int,
    gridCellWidth: Int,
    gridCellHeight: Int,
    gridCellsX: Int,
    gridCellsY: Int,
    tileset: String,
    data2D: List[List[Int]],
    exportMode: Int,
    arrayMode: Int // 1 => 2D
) extends JsonLayer

final case class JsonEntityLayer(
    _eid: String,
    name: String,
    offsetX: Int,
    offsetY: Int,
    gridCellWidth: Int,
    gridCellHeight: Int,
    gridCellsX: Int,
    gridCellsY: Int,
    entities: List[JsonEntity]
) extends JsonLayer

final case class JsonEntity(
    _eid: String,
    id: Int,
    name: String,
    x: Int,
    y: Int,
    width: Option[Int],
    height: Option[Int],
    originX: Int,
    originY: Int,
    values: Option[
      Map[String, Json]
    ] // technically it is to String | Number | Boolean I think
) {
  def value(name: String): Option[Json] =
    values.flatMap(_.get(name))
}
