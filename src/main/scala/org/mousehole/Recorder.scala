package org.mousehole

import java.util.concurrent.TimeUnit

import akka.actor.{Actor, ActorRef}
import akka.util.Timeout
import com.typesafe.scalalogging.LazyLogging
import spray.json._

import scala.concurrent.duration.Duration

/**
 * Created by ruguer
 * 1/18/15.
 */
class Recorder extends Actor with LazyLogging {

  logger.info(s"Starting actor: ${self.toString()}")

  implicit val to = Timeout(Duration(10,TimeUnit.SECONDS))

  var actors = List[ActorRef]()

  override def receive: Receive = {
    case Register(sendTo:ActorRef) =>
      logger.info(s"Registering ${sendTo.toString()}")
      actors = sendTo :: actors

    case r : RecorderMessage =>
      logger.info(s"Message to send $r to ${actors.length} actors")
      actors.foreach(t => logger.info(s"To actor ${t.toString()}"))
        actors.foreach(_.tell(r.toJson(RecorderMessageJson.recordMessageJson), self))
  }
}

object RecorderMessageJson extends DefaultJsonProtocol {
  implicit val recordMessageJson = jsonFormat3(RecorderMessage)
}

case class RecorderMessage(name : String, previous : String, value : String)
case class Register(actor:ActorRef)
