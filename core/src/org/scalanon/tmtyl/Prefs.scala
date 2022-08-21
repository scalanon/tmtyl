package org.scalanon.tmtyl

import com.badlogic.gdx.{Gdx, Preferences}

import java.util.UUID

class Pref(key: String) {
  import Prefs.preferences

  def isDefined: Boolean = preferences.contains(key)
  def intValue: Option[Int] =
    preferences.contains(key).option(preferences.getInteger(key))
  def longValue: Option[Long] =
    preferences.contains(key).option(preferences.getLong(key))
  def booleanValue: Option[Boolean] =
    preferences.contains(key).option(preferences.getBoolean(key))
  def stringValue: Option[String] =
    preferences.contains(key).option(preferences.getString(key))
  def set(value: Int): Unit = {
    preferences.putInteger(key, value)
    preferences.flush()
  }
  def set(value: Long): Unit = {
    preferences.putLong(key, value)
    preferences.flush()
  }
  def set(value: Boolean): Unit = {
    preferences.putBoolean(key, value)
    preferences.flush()
  }
  def set(value: String): Unit = {
    preferences.putString(key, value)
    preferences.flush()
  }
  def fold[A](ifTrue: => A, ifFalse: => A): A =
    if (booleanValue.isTrue) ifTrue else ifFalse

  def isTrue: Boolean = booleanValue.isTrue
}

object Prefs {
  var preferences: Preferences = _

  def loadPreferences(): Unit = {
    preferences = Gdx.app.getPreferences("tertis")
    if (!UniqueIdentifier.isDefined)
      UniqueIdentifier.set(UUID.randomUUID.toString)
  }

  final val UniqueIdentifier = new Pref("uuid")
  final val HighScore = new Pref("highScore")
  final val Instructed = new Pref("instructed")
  final val MuteAudio = new Pref("muteAudio")
}
