package quickstart.sockets

import java.io.{ObjectInputStream, ObjectOutputStream}
import java.net.Socket

import sun.misc.Signal
import sun.misc.SignalHandler

import scala.sys.exit
import scala.util.Try


class Client(name: String, friendName: String, ip: String, port: Int) {

  private var socket = new Socket(ip, port)
  private var out: ObjectOutputStream = new ObjectOutputStream(socket.getOutputStream)
  private var in: ObjectInputStream = new ObjectInputStream(socket.getInputStream)

  private val user = User(name)
  private val friends = Seq(User(friendName))
  var consoleBuffer = new java.util.concurrent.ConcurrentLinkedDeque[String]()

  registerUser()
  val clientHandlerInput = new ClientHandlerInput(socket)
  val clientHandlerOutput = new ClientHandlerOutput(socket)

  clientHandlerInput.start()
  clientHandlerOutput.start()

  class ClientHandlerInput(socket: Socket) extends Thread {
    override def run(): Unit = {
      while (!socket.isClosed) {
        try {
          println(in.readObject().asInstanceOf[Message])
          //        in.readObject().asInstanceOf[Message] match {
          //          case t @ TextMessage(sender, _, contents) => println(t.toString)
          //          case m @ UserHasGoneOffline(friend: User) => println(m.toString)
          //          case m @ UserIsOffline(User(toni))
          //        }
          Thread.sleep(100)
        } catch {
          case _ : Throwable => {}
        }
      }
    }
  }
  class ClientHandlerOutput(socket: Socket) extends Thread {
    override def run(): Unit = {
      while (!socket.isClosed) {
        try {
          if (consoleBuffer.size() > 0) {
            val friend = friends(0)
            sendMessage(consoleBuffer.pop(), friend)
          }
          Thread.sleep(100)
        } catch {
          case _ : Throwable => {}
        }
      }
    }
  }

  def readFromConsole() =
    while (true) {
      val stdin = scala.io.StdIn.readLine()
      consoleBuffer.add(stdin)
    }

  def sendMessage(msg: String, to: User): Unit =
    sendMessage(TextMessage(user, to, msg))

  def registerUser() =
    sendMessage(RegisterUser(user))

  private def sendMessage(msg: Message): Unit = {
    out.writeObject(msg)
  }

  def closeConnection(): Unit = {
    sendMessage(UserGoingOffline(user, friends))
    Try { clientHandlerInput.interrupt() }
    Try { clientHandlerOutput.interrupt() }
    in.close()
    out.close()
    socket.close()
  }
}

object ClientMain {
  def main(args: Array[String]) = {

    val name = args(0)
    val friend = args(1)
    val ip = args.lift(2).getOrElse("localhost")
    val port = args.lift(3).map(_.toInt).getOrElse(6666)

    println("Initiating client")
    val client = new Client(name, friend, ip, port)

    val start = System.nanoTime()
    Signal.handle(new Signal("INT"), new SignalHandler() {
      def handle(sig: Signal) {
        println(f"\nProgram execution took ${(System.nanoTime() - start) / 1e9f}%f seconds\n")
        client.closeConnection()
        exit(0)
      }
    })

    client.readFromConsole()
  }
}
