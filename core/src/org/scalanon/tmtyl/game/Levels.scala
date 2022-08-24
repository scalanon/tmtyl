package org.scalanon.tmtyl.game

import cats.implicits.toFunctorOps
import com.badlogic.gdx.Gdx
import io.circe.generic.auto._
import io.circe.parser._
import io.circe.{Decoder, Json}

import scala.reflect.ClassTag

object Levels {
  lazy val level1 = load("Level1.json")

  lazy val newMexico = load("NewMexico.json")

  lazy val level2 = load("Level2.json")

  lazy val level3 = load("Level3.json")

  private def load(level: String): JsonLevel = {
    val json = Gdx.files.internal(s"ogmo/$level").readString("UTF-8")
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
    data2D: Array[Array[Int]],
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
    ]
) {
  def boolean(name: String): Option[Boolean] = value[Boolean](name)
  def string(name: String): Option[String]   = value[String](name)
  def int(name: String): Option[Int]         = value[Int](name)

  def value[A: JsonValue](name: String): Option[A] =
    values.flatMap(_.get(name)).flatMap(JsonValue[A].value)
}

trait JsonValue[A] {
  def value(json: Json): Option[A]
}

object JsonValue {
  def apply[A: JsonValue]: JsonValue[A] = implicitly

  implicit val JsonBooloanValue: JsonValue[Boolean] = (json: Json) =>
    json.asBoolean
  implicit val JsonStringValue: JsonValue[String]   = (json: Json) =>
    json.asString
  implicit val JsonIntValue: JsonValue[Int]         = (json: Json) =>
    json.asNumber.flatMap(_.toInt)

}
