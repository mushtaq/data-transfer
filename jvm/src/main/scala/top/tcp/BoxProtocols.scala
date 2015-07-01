package top.tcp

import akka.stream.scaladsl.{BidiFlow, Flow}
import akka.util.ByteString
import top.common.{BoxConversions, Box}

object BoxProtocols {
  val codec = BidiFlow(BoxConversions.toByteString _, BoxConversions.fromByteString _)
  val stack = codec.atop(Framer.codec)
  val outbound = Flow[Box].map(BoxConversions.toByteString).via(Framer.encoder)
  val inbound = Flow[ByteString].via(Framer.decoder).map(BoxConversions.fromByteString)
}
