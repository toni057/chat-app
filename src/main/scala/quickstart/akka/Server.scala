package quickstart.akka

import akka.actor.{Actor, ActorRef, ActorSystem, Props}
import akka.io.{IO, Tcp}
import akka.event.Logging
import quickstart.messages._

import java.net.InetSocketAddress
import scala.util.Try

final case class Put(user: User, ref: ActorRef)
final case class Remove(user: User)

//class UserList extends Actor {
//
//  def receive = {
//    case Put(user, ref) => {
//      sender() ! Try(users.addOne(user, ref)).toEither
//    }
//    case Remove(user) => {
//      sender() ! Try{ users.remove(user) }.toEither
//    }
//  }
//}

class SimplisticHandler extends Actor {

  import Tcp._
  import quickstart.akka.MessageUtils._

//  val userList = context.actorOf(Props[UserList])

  def receive = {
    case Received(data) => {
      Message.deserialize(data.toArray[Byte]) match {
        case RegisterUser(user) => println(s"Registering ${user.name}")
        case TextMessage(from, to, msg) => println(s"From: ${from.name}, to: ${to.name}: ${msg}")
        case UserGoingOffline(user, _) => println(s"Going offline: ${user.name}")
        case _ => println("...")
      }
    }
    case ConfirmedClose => println(s"Closing ")
    case PeerClosed => context.stop(self)
    case x => println(s"handler _ => ${x.toString}")
  }
}

class Server() extends Actor {
  import akka.io.Tcp._
  import context.system
  val log = Logging(context.system, this)

  val port = 5000
  IO(Tcp) ! Bind(self, new InetSocketAddress("localhost", port))

  val users = scala.collection.mutable.Map.empty[User, ActorRef]

  def receive = {
    case b @ Bound(localAddress) =>
      println(s"Bound: ${localAddress}")

    case CommandFailed(_: Bind) => context.stop(self)

    case c @ Connected(remote, local) =>
      val handler = context.actorOf(Props[SimplisticHandler]())
      val connection = sender()
      connection ! Register(handler)

    case Received(s) => println(s"received: s.utf8String")

    case x => println(s"server _ => ${x.toString}")
  }
}

object ServerApp extends App {
  val system = ActorSystem("Server")
  val port = 5000
  val swap = system.actorOf(Props[Server](), name = "ServerMain")
}
