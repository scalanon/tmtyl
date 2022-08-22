package org.scalanon.tmtyl.game

import com.badlogic.gdx.Gdx
import io.circe.generic.auto._
import io.circe.parser._

object Levels {
  lazy val level1 = {
    val json = Gdx.files.internal("ogmo/Level1.json").readString("UTF-8")
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

final case class JsonLayer(
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
)
