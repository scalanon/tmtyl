package org.scalanon.tmtyl
package game

import com.badlogic.gdx.graphics.g2d.{GlyphLayout, PolygonSpriteBatch}
import org.scalanon.tmtyl.Geometry._
import org.scalanon.tmtyl.Prefs

class Score {
  var alpha: Float = 0f
  var score: Int = 0
  var highScore: Boolean = false

  def recordHighScore(): Unit = {
    if (score > 0 && Prefs.HighScore.intValue.forall(_ < score)) {
      highScore = true
      Prefs.HighScore.set(score)
    }
  }

  def update(delta: Float): Unit = {
    alpha = (alpha + delta / FadeInSeconds) min 1f
  }

  def draw(batch: PolygonSpriteBatch): Unit = {
    Text.smallFont.setColor(1, 1, 1, alpha * alpha)
    Text.mediumFont.setColor(1, 1, 1, alpha * alpha)
    val scoreLabel = new GlyphLayout(Text.smallFont, f"SCORE:")
    val scoreValue =
      new GlyphLayout(Text.mediumFont, f" $score%,d")
    val xOffset = OffsetX + Dimension / 4
    val baseline = OffsetY + Dimension * Rows + Dimension / 2
    Text.smallFont.draw(
      batch,
      scoreLabel,
      xOffset,
      baseline + Text.smallFont.getCapHeight
    )
    Text.mediumFont.draw(
      batch,
      scoreValue,
      xOffset + scoreLabel.width,
      baseline + Text.mediumFont.getCapHeight
    )
  }

  val FadeInSeconds = 1f
}
