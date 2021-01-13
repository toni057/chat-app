package quickstart.sockets

import grizzled.slf4j.Logging
import quickstart.messages._

import java.io.{ObjectInputStream, ObjectOutputStream}
import java.net.Socket
import javax.net.ssl.{SSLSocket, SSLSocketFactory}
import sun.misc.Signal
import sun.misc.SignalHandler

import scala.sys.exit
import scala.util.Try


class Client(name: String, friendName: String, host: String, port: Int) {

  private var socket = new Socket(host, port)
//  private var socket = SSLSocketFactory.getDefault().createSocket(host, port)
  private var out: ObjectOutputStream = new ObjectOutputStream(socket.getOutputStream)
  private var in: ObjectInputStream = new ObjectInputStream(socket.getInputStream)

  private val user = User(name)
  private val friends = Seq(User(friendName))
  var consoleBuffer = new java.util.concurrent.ConcurrentLinkedDeque[String]()

  registerUser()
  val clientHandlerInput = new ClientHandlerInput(socket)
  val clientHandlerOutput = new ClientHandlerOutput(socket)

  val messageHistory = scala.collection.mutable.ArrayBuffer

  clientHandlerInput.start()
  clientHandlerOutput.start()

  class ClientHandlerInput(socket: Socket) extends Thread with Logging {
    override def run(): Unit = {
      while (!socket.isClosed) {
        try {
          println(in.readObject().asInstanceOf[Message])
          Thread.sleep(100)
        } catch {
          case e : Throwable => {
            logger.info(e.getMessage)
          }
        }
      }
    }
  }
  class ClientHandlerOutput(socket: Socket) extends Thread with Logging {
    override def run(): Unit = {
      while (!socket.isClosed) {
        try {
          if (consoleBuffer.size() > 0) {
            val friend = friends(0)
            sendMessage(consoleBuffer.pop(), friend)
          }
          Thread.sleep(100)
        } catch {
          case e : Throwable => {
            logger.info(e.getMessage)
          }
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
