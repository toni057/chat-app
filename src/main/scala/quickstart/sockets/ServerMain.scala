package quickstart.sockets

import java.io._
import java.net.{ServerSocket, Socket}
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.concurrent.{ExecutorService, Executors}

import scala.jdk.CollectionConverters._
import scala.util.Try

class Server(host: String, port: Int) {
  require(port >= 0 && port <= 65535, "Port must be in the range 0-65535, inclusive")

  val pool: ExecutorService = Executors.newFixedThreadPool(10)

  val MAX_SIZE = 1000
  val serverSocket = new ServerSocket(port)

  def newInputStream(socket: Socket) = new ObjectInputStream(socket.getInputStream)

  def newOutputStream(socket: Socket) = new ObjectOutputStream(socket.getOutputStream)

  //  var registeredUsers = Set.empty[User]
  var connections =
    new java.util.concurrent.ConcurrentHashMap[User, (Socket, ObjectInputStream, ObjectOutputStream)]().asScala

  def start() = {
    try {
      while (true) {
        pool.execute(new ClientHandler(serverSocket.accept()))
      }
    } finally {
      pool.shutdown()
    }
  }

  def register(user: User, socket: Socket, in: ObjectInputStream, out: ObjectOutputStream): Unit = {
    println(s"Registering ${user.name}")
    //    registeredUsers = registeredUsers + user
    connections.addOne(user, (socket, in, out))
  }

  def exit(user: User) = {
    /** todo * */
    // remove from registered users
    // notify online friends that the user is now offline

    //    registeredUsers = registeredUsers - user
    connections = connections.subtractOne(user)
  }

  def text(from: User, to: User, message: String) = {
    // verify to exists
    // if exists send message
    //    connections
    //      .get(to)
    //      .map { case (_, _, out) => (out, TextMessage(from, to, message)) }
    //      .orElse{
    //        connections
    //          .get(from)
    //          .map { case(_, _, out) => (out, UserIsOffline(to)) }
    //      }
    //      .foreach{ case(conn, message) => sendMessage(conn, message) }
    sendMessage(to, TextMessage(from, to, message))
      .orElse(sendMessage(from, UserIsOffline(to)))
  }

  def sendMessage(to: User, message: Message) = {
    for {
      (_, _, out) <- connections.get(to)
      success <- Try(out.writeObject(message)).toOption
    } yield success
  }

  def stop(): Unit = {
    connections.view.mapValues(_._1.close())
    serverSocket.close()
  }

  class ClientHandler(socket: Socket) extends Runnable {
    val in = newInputStream(socket)
    val out = newOutputStream(socket)

    override def run(): Unit = {
      while (!socket.isClosed) {
        try {
          in.readObject().asInstanceOf[Message] match {
            case RegisterUser(sender) => register(sender, socket, in, out)
            case UserGoingOffline(sender, friends) => {
              friends.foreach(f => sendMessage(f, UserHasGoneOffline(sender)))
              exit(sender)
            }
            case TextMessage(sender, to, contents) => {
              println(s"${sender.name} to ${to.name}: $contents")
              text(sender, to, contents)
            }
            case _ => println("skipping")
          }
          Thread.sleep(100)
        } catch {
          case _: Throwable => {}
        }
      }
    }
  }

  //  class HeartBeat extends Runnable {
  //    override def run(): Unit = {
  //      connections =
  //    }
  //  }

}

object Server {
  def apply(host: String, port: Int): Server = new Server(host, port)

  def apply(port: Int): Server = new Server("localhost", port)
}


object ServerMain {
  def main(args: Array[String]): Unit = {
    val ip = args.lift(0).getOrElse("localhost")
    val port = args.lift(1).map(_.toInt).getOrElse(6666)
    val server = Server(ip, port)
    server.start()
  }
}
