package quickstart.sockets

import grizzled.slf4j.Logging
import quickstart.messages._

import java.io._
import java.net.{ServerSocket, Socket}
import java.security.KeyStore
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.concurrent.{ExecutorService, Executors}
import javax.net.SocketFactory
import javax.net.ssl.{KeyManagerFactory, SSLServerSocket, SSLServerSocketFactory, SSLSocket, SSLSocketFactory}
import scala.jdk.CollectionConverters._
import scala.util.Try

class Server(host: String, port: Int) extends Logging{
  require(port >= 0 && port <= 65535, "Port must be in the range 0-65535, inclusive")

  val serverSocket = new ServerSocket(port)

//  val ks = KeyStore.getInstance("JKS");
//  val ksIs = new FileInputStream("...");
//  try {
//    ks.load(ksIs, "password".toCharArray());
//  } finally {
//    if (ksIs != null) {
//      ksIs.close()
//    }
//  }
//  val kmf = KeyManagerFactory.getInstance(KeyManagerFactory
//    .getDefaultAlgorithm());
//  kmf.init(ks, "keypassword".toCharArray());
//  val serverSocket = SSLServerSocketFactory.getDefault().createServerSocket(port).asInstanceOf[SSLServerSocket]//.createServerSocket(host, port)
//  serverSocket.setEnabledProtocols(Array("TLSv1", "TLSv1.1", "TLSv1.2", "SSLv3"))

  val pool: ExecutorService = Executors.newFixedThreadPool(10)
  val MAX_SIZE = 1000

  def newInputStream(socket: Socket) = new ObjectInputStream(socket.getInputStream)

  def newOutputStream(socket: Socket) = new ObjectOutputStream(socket.getOutputStream)

  var connections =
    new java.util.concurrent.ConcurrentHashMap[User, ClientConnection]().asScala

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
    connections.addOne(user, ClientConnection(socket, in, out))
  }

  def exit(user: User) = {
    connections = connections.subtractOne(user)
  }

  def text(from: User, to: User, message: String) = {
    sendMessage(to, TextMessage(from, to, message))
      .orElse(sendMessage(from, UserIsOffline(to)))
  }

  def sendMessage(to: User, message: Message) = {
    for {
      ClientConnection(_, _, out) <- connections.get(to)
      success <- Try(out.writeObject(message)).toOption
    } yield success
  }

  def stop(): Unit = {
    connections.view.mapValues(_.socket.close())
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
          case e: Throwable => {
            logger.info(e.getMessage)
          }
        }
      }
    }
  }

  protected case class ClientConnection(socket: Socket, in: ObjectInputStream, out: ObjectOutputStream)

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
