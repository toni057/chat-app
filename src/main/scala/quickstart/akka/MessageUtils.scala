package quickstart.akka

import akka.io.Tcp.Write
import akka.util.ByteString
import org.apache.commons.lang3.SerializationUtils
import quickstart.messages.Message


object MessageUtils {

  implicit class Utils(msg: Message) {
    def serialize: Array[Byte] = {
      SerializationUtils.serialize(msg.asInstanceOf[Serializable])
    }
    def toByteString = {
      ByteString(msg.serialize)
    }
    def writeByteString = {
      Write(msg.toByteString)
    }
  }

  object Message {
    def deserialize(bytes: Array[Byte]): Any = {
      SerializationUtils.deserialize(bytes)
    }
  }

}
