package org.mousehole

import akka.actor.{ActorRef, ActorSystem, Props}
import akka.io.IO
import com.typesafe.scalalogging.LazyLogging
import org.mousehole.websocket.SimpleServer.WebSocketServer
import spray.can.Http
import spray.can.server.UHttp

/**
 * Created by ruguer on 1/18/15.
 */
abstract class Instrumentation[T] extends LazyLogging {
  self =>

  val system : ActorSystem

//  val value : T

  val recorder : ActorRef

  def instrument[B](name : String, previous : String, extractor : T => String,f : T => B) : T => B = {
    def internal(input : T) : T = {
      recorder ! RecorderMessage(name,previous,extractor(input))
      input
    }
    def sleeper(t:T) : T = {Thread.sleep(1000);t}
    internal _ andThen sleeper andThen f
  }

  def instrument[B](name : String, extractor : T => String,f : T => B) : T => B =
    instrument(name,"",extractor,f)

//  def map[B](f : T => B) : Instrumentation[B] = {
//    new Instrumentation[B] {
//      override val system = self.system
////      override val value = f(self.value)
//      override val recorder = self.recorder
//    }
//  }
//
//  def flatMap[B](f : T => Instrumentation[B]) : Instrumentation[B] = {
////    val v = f(value)
//    if(v.system != this.system) {
//      v.system.shutdown()
//    }
//    new Instrumentation[B] {
//      override val recorder: ActorRef = recorder
//      override val value: B = v.value
//      override val system: ActorSystem = system
//    }
//  }
}

object Instrumentation {
  def apply[T](actorSystem:ActorSystem) : Instrumentation[T] = {
    val server = actorSystem.actorOf(WebSocketServer.props(), "websocket")

    val t = IO(UHttp)(actorSystem) ! Http.Bind(server, "localhost", 8080)

    Thread.sleep(5000)

    new Instrumentation[T] {
      override val recorder: ActorRef = actorSystem.actorOf(Props[Recorder],"recorder")
//      override val value: T = initValue
      override val system: ActorSystem = actorSystem
    }
  }
  def apply[T](name:String) : Instrumentation[T] = {
    apply(ActorSystem.create(name))
  }
}