package quickstart.sockets

import scala.util.Try

trait DataBase {

  def getFriends(user: User): Iterable[User]
  def newUser(user: User): Try[Boolean]
  def addFriend(user: User, newFried: User): Try[Boolean]

}

class InMemoryDataBase(var friendList: Map[User, Set[User]]) extends DataBase {

  def getFriends(user: User) = {
    friendList.get(user).getOrElse(Set.empty[User])
  }

  def newUser(user: User): Try[Boolean] = {
    Try {
      friendList = friendList + (user -> Set.empty[User])
      true
    }
  }

  def addFriend(user: User, newFriend: User) = {
    Try {
      friendList = friendList + (user -> (friendList(user) + newFriend))
      true
    }
  }

}
