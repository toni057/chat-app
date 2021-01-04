package quickstart.messages

import java.text.SimpleDateFormat

sealed case class User(name: String)

sealed trait Message extends Serializable {
  val user: User
  val timestamp = new java.sql.Timestamp(System.currentTimeMillis())
  def dateTimeFormat =
    if (System.currentTimeMillis() - timestamp.getTime <= 86400L * 1000L) "h:mm aa"
    else "dd/MM/yyyy HH:mm"
  def ts: String = new SimpleDateFormat(dateTimeFormat).format(timestamp)
}

sealed case class RegisterUser(user: User) extends Message

sealed case class UserGoingOffline(user: User, friends: Seq[User]) extends Message

sealed case class TextMessage(user: User, to: User, contents: String) extends Message {
  override def toString: String = s"(${ts}) ${user.name}: $contents"
}

sealed case class UserIsOffline(user: User) extends Message {
  override def toString: String = s"(${ts}) ${user.name} is offline."
}

sealed case class UserHasGoneOffline(user: User) extends Message {
  override def toString: String = s"(${ts}) ${user.name} has gone offline."
}

//sealed abstract class HeartBeat(user: User) extends Message
//sealed case class HeartBeatCall(user: User) extends HeartBeat(user)
//sealed case class HeartBeatRespond(user: User) extends HeartBeat(user)
