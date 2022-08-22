package org.scalanon.tmtyl.game

import com.badlogic.gdx.Gdx
import io.circe.parser._

object Ogmo {
  private def readOgmo(): String =
    Gdx.files.internal("ogmo/Tmtyl.ogmo").readString("UTF-8")

  lazy val json = parse(readOgmo()).toTry.get
}
