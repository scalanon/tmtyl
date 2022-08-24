package org.scalanon.tmtyl
package game

import com.badlogic.gdx.graphics.Pixmap
import io.circe.generic.auto._
import org.scalanon.tmtyl.util.TextureWrapper

import java.util.Base64

class Tilesets(tilesets: Map[String, Tileset]) {
  def apply(label: String): Tileset = tilesets
    .getOrElse(label, throw new Exception(s"Unknown tileset $label"))
}

object Tilesets {
  def load(): Tilesets = {
    val tilesets =
      Ogmo.json.hcursor.downField("tilesets").as[List[JsonTileset]].toTry.get
    new Tilesets(tilesets.map(set => set.label -> set.load()).toMap)
  }
}

final case class Tileset(
    tileWidth: Int,
    tileHeight: Int,
    tileSeparationX: Int,
    tileSeparationY: Int,
    tileMarginX: Int,
    tileMarginY: Int,
    texture: TextureWrapper
) {
  private val columns =
    (texture.width - 2 * tileMarginX + tileSeparationX) / (tileWidth + tileSeparationX)

  def tile(index: Int): Option[Tile] =
    (index >= 0) option {
      val col = index % columns
      val row = index / columns
      Tile(
        tileMarginX + col * (tileWidth + tileSeparationX),
        tileMarginY + row * (tileHeight + tileSeparationY),
        tileWidth,
        tileHeight
      )
    }

}

final case class Tile(x: Int, y: Int, width: Int, height: Int)

final case class JsonTileset(
    label: String,
    path: String,
    image: String,
    tileWidth: Int,
    tileHeight: Int,
    tileSeparationX: Int,
    tileSeparationY: Int,
    tileMarginX: Int,
    tileMarginY: Int
) {
  def load(): Tileset = {
    val base64 = image.stripPrefix("data:image/png;base64,")
    val png    = Base64.getDecoder.decode(base64)
    val pixmap = new Pixmap(png, 0, png.length)
    Tileset(
      tileWidth,
      tileHeight,
      tileSeparationX,
      tileSeparationY,
      tileMarginX,
      tileMarginY,
      new TextureWrapper(pixmap)
    )
  }
}
