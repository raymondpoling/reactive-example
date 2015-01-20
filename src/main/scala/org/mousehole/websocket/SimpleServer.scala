package org.mousehole.websocket

import _root_.spray.can.websocket.FrameCommandFailed
import _root_.spray.can.websocket.frame.{BinaryFrame, TextFrame}
import _root_.spray.can.{Http, websocket}
import _root_.spray.http.HttpRequest
import _root_.spray.json.JsObject
import _root_.spray.routing.HttpServiceActor
import akka.actor._
import com.typesafe.scalalogging.LazyLogging
import org.mousehole.Register

/**
 * Created by ruguer on 1/18/15.
 */
object SimpleServer extends App {

  final case class Push(msg: String)

  object WebSocketServer {
    def props() = Props(classOf[WebSocketServer])
  }
  class WebSocketServer extends Actor with ActorLogging {
    def receive = {
      // when a new connection comes in we register a WebSocketConnection actor as the per connection handler
      case Http.Connected(remoteAddress, localAddress) =>
        val serverConnection = sender()
        val conn = context.actorOf(WebSocketWorker.props(serverConnection))
        serverConnection ! Http.Register(conn)
    }
  }

  object WebSocketWorker {
    def props(serverConnection: ActorRef) = Props(classOf[WebSocketWorker], serverConnection)
  }
  class WebSocketWorker(val serverConnection: ActorRef) extends HttpServiceActor with websocket.WebSocketServerWorker with LazyLogging {
    override def receive = handshaking orElse businessLogicNoUpgrade orElse closeLogic

    println(s"GRRRRR ${self.toString()}")

    def businessLogic: Receive = {
      // just bounce frames back for Autobahn testsuite
      case x @ (_: BinaryFrame | _: TextFrame) =>
        logger.info(s"Got message from ${sender()} and I am ${self.toString()}")
        context.actorSelection("/user/recorder*").tell(Register(self),self)
//        sender() ! x

      case js : JsObject =>
        logger.info(s"Recieved message to push: ${js.toString}")
        send(TextFrame(js.toString()))

      case Push(msg) => send(TextFrame(msg))

      case x: FrameCommandFailed =>
        log.error("frame command failed", x)

      case x: HttpRequest => // do something

      case t => logger.info(s"Got message ${t.getClass} of $t that I don't know how to handle")
    }

    def businessLogicNoUpgrade: Receive = {
      implicit val refFactory: ActorRefFactory = context
      runRoute {
        getFromResourceDirectory("webapp")
      }
    }
  }

//  def doMain() {
//    implicit val system = ActorSystem()
//
//
//    readLine("Hit ENTER to exit ...\n")
//    system.shutdown()
//    system.awaitTermination()
//  }
//
//  // because otherwise we get an ambiguous implicit if doMain is inlined
//  doMain()
}
