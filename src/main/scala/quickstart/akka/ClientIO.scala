package quickstart.akka

import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.Behaviors
import quickstart.messages._
import akka.actor.{Actor, ActorRef, ActorSystem, Props, Terminated}
import akka.io.Tcp.{Connected, Received}
import akka.io.{IO, Tcp}
import akka.util.{ByteString, CompactByteString}
import quickstart.akka.MessageUtils.Utils

import java.net.InetSocketAddress

object ClientIO {
  def props(remote: InetSocketAddress, userName: String) =
    Props(classOf[ClientIO], remote, userName)
}

class ClientIO(remote: InetSocketAddress, val userName: String) extends Actor {

  import akka.io.Tcp._
  import context.system
  import MessageUtils._

  def connectToServer() = {
    IO(Tcp) ! Connect(remote)
  }

  val stdin = context.actorOf(StdIn.props, name = "stdinRef")
  val listener = context.actorOf(Listener.props, name = "listenerRef")
  val user = User(userName)

  connectToServer()

  def receive = {
    case CommandFailed(_: Connect) =>
      listener ! "connect failed"
      context.stop(self)

    case c @ Connected(remote, local) =>
      listener ! c
      val connection = sender()
      connection ! Register(self)
      connection ! RegisterUser(user).writeByteString

      context.become {
//        case data: ByteString =>
//          connection ! Write(data)
        case CommandFailed(w: Write) =>
          // O/S buffer was full
          listener ! "write failed"
        case Received(data) =>
          // this is a message from the server
          listener ! data
        case "close" =>
          connection ! UserGoingOffline(user, Seq.empty[User]).writeByteString
          connection ! Close
        case s: String =>
          // this is a message from the listener
          connection ! TextMessage(user, User("sd"), s).writeByteString
          listener ! s

        case _: ConnectionClosed =>
          listener ! "connection closed"
          context.stop(listener)
          context.stop(stdin)
          context.stop(self)
      }
  }
}

class Listener extends Actor {
  override def receive: Receive = {
    case Connected(remote, local) =>
      println(s"Successfully connected to $remote from $local")
    case data: ByteString => println(s"ByteString: ${data.utf8String}")
    case s: String => println(s"String: $s")
  }
}

object Listener {
  def props =
    Props(classOf[Listener])
}

class StdIn extends Actor {
  val listener = context.parent
  while (true) {
    val stdin = scala.io.StdIn.readLine()
    listener ! stdin
  }

  override def receive: Receive = {
    case _ => {
      println("....")
    }
  }
}


object StdIn {
  def props = Props(classOf[StdIn])
}

object ClientApp {

  def main(args: Array[String]): Unit = {
    val system = ActorSystem("Client")
    val port = 5000
    val clientIO = system.actorOf(
      ClientIO.props(
        new InetSocketAddress("localhost", port),
        "user1",
      ),
      name = "ClientMainRef"
    )
//    val stdin = system.actorOf(StdIn.props(clientIO), name = "stdinRef")

  }

  //  def apply(host: String, port: Int) = {
  //      Behaviors.setup { context: ActorContext[_] =>
  //        val listenerRef = context.spawn(Listener(), "listener")
  //        val clientRef = context.spawn(Client(new InetSocketAddress("localhost", 5000), listenerRef), "client")
  //        val stdinRef = context.spawn(StdIn(clientRef), "stdin")
  //
  //        context.watch(stdinRef)
  //        context.watch(listenerRef)
  //        context.watch(clientRef)
  //
  //        system.actorOf(StdIn.props(swap), name = "stdin")
  //
  //        chatRoom ! ChatRoom.GetSession("ol’ Gabbler", gabblerRef)
  //
  //        Behaviors.receiveSignal {
  //          case (_, Terminated(_)) =>
  //            Behaviors.stopped
  //        }
  //      }
  //  }

}

