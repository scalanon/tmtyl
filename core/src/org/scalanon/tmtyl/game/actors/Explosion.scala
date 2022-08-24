package org.scalanon.tmtyl
package game.actors

final case class Explosion(x: Float, y: Float)
    extends Animation(
      x,
      y,
      originX = .5f,
      originY = 0,
      frames = 10,
      frameRate = .066f,
      image = AssetLoader.image("explosion.png")
    )
