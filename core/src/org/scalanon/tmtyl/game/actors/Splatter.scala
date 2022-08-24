package org.scalanon.tmtyl
package game.actors

final case class Splatter(x: Float, y: Float)
    extends Animation(
      x,
      y,
      originX = .5f,
      originY = .5f,
      frames = 13,
      frameRate = .033f,
      image = AssetLoader.image("blood.png")
    )
