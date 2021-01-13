package quickstart.akka

import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.{ActorContext, Behaviors}
import akka.actor.{Actor, ActorRef, ActorSystem, Props}
import akka.io.{IO, Tcp}
import akka.util.ByteString
import org.postgresql.core.{ConnectionFactory, QueryExecutor}
import org.postgresql.core.v3.ConnectionFactoryImpl
import org.postgresql.util.HostSpec

import java.net.InetSocketAddress
import java.sql.{DriverManager, PreparedStatement}

object Client {
  def props(remote: InetSocketAddress, replies: ActorRef) =
    Props(classOf[Client], remote, replies)
}
class Client(remote: InetSocketAddress, listener: ActorRef) extends Actor {

  import akka.io.Tcp._
  import context.system

  IO(Tcp) ! Connect(remote)

  def receive = {
    case CommandFailed(_: Connect) =>
      listener ! "connect failed"
      context.stop(self)

    case c@Connected(remote, local) =>
      listener ! c
      val connection = sender()
      connection ! Register(self)
      context.become {
        case data: ByteString =>
          connection ! Write(data)
        case CommandFailed(w: Write) =>
          // O/S buffer was full
          listener ! "write failed"
        case Received(data) =>
          listener ! data
        case "close" =>
          connection ! Close
        case _: ConnectionClosed =>
          listener ! "connection closed"
          context.stop(self)
      }
  }
}


/**
 * the client actor can receive a message from :
 *    - stdin
 *    - the socket connection
 *    - main function
 * */
//object Client {
//
//  import akka.io.Tcp._
//
//
//  def apply(remote: InetSocketAddress): Behavior[_] = {
//
//    import context.system
//
//    IO(Tcp) ! Connect(remote)
//
//    Behaviors.setup{ context: ActorContext[_] =>
//      Behaviors.receiveMessage {
//        case _ => println("received")
//      }
//    }
//  }
//
//  def receive = {
//    case CommandFailed(_: Connect) =>
//      listener ! "connect failed"
//      context.stop(self)
//
//    case c@Connected(remote, local) =>
//      listener ! c
//      val connection = sender()
//      connection ! Register(self)
//      context.become {
//        case data: ByteString =>
//          connection ! Write(data)
//        case CommandFailed(w: Write) =>
//          // O/S buffer was full
//          listener ! "write failed"
//        case Received(data) =>
//          listener ! data
//        case "close" =>
//          connection ! Close
//        case _: ConnectionClosed =>
//          listener ! "connection closed"
//          context.stop(self)
//      }
//  }
//}

class Listener extends Actor {
  override def receive: Receive = {
    case data => println(data.toString)
  }
}

object Listener {
  def props =
    Props(classOf[Listener])
}

class StdIn(listener: ActorRef) extends Actor {
  while (true) {
    val stdin = scala.io.StdIn.readLine()
    listener ! stdin
  }

  override def receive: Receive = {
    case _ => println("....")
  }
}

object StdIn {
  def props(listener: ActorRef) = Props(classOf[StdIn], listener)
}

object ClientApp {

  def main(args: Array[String]): Unit = {
    val system = ActorSystem("Client")
    val port = 5000
    val swap = system.actorOf(
      Client.props(
        new InetSocketAddress("localhost", port),
        system.actorOf(Listener.props)
      ),
      name = "ClientMain"
    )
    system.actorOf(StdIn.props(swap), name = "stdin")
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