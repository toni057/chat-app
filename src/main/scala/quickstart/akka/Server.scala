package quickstart.akka

import akka.actor.{Actor, Props}
import akka.io.{IO, Tcp}

import java.net.InetSocketAddress


class SimplisticHandler extends Actor {

  import Tcp._

  def receive = {
    case Received(data) => sender() ! Write(data)
    case PeerClosed => context.stop(self)
  }
}

class Server extends Actor {

  import akka.io.Tcp._
  import context.system

  IO(Tcp) ! Bind(self, new InetSocketAddress("localhost", 0))

  def receive = {
    case b @ Bound(localAddress) =>
      context.parent ! b

    case CommandFailed(_: Bind) => context.stop(self)

    case c @ Connected(remote, local) =>
      val handler = context.actorOf(Props[SimplisticHandler]())
      val connection = sender()
      connection ! Register(handler)
  }

}

//#main-class
object AkkaQuickstart extends App {
}
