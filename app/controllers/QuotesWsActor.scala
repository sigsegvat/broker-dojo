package controllers

import akka.actor.{Actor, Props, ActorRef}
import at.segv.play.broker.{Client, Scores}
import at.segv.play.broker.api.Tick
import play.Logger
import play.api.libs.json.{Json, JsObject}

object QuotesWsActor {
  def props(out: ActorRef) = Props(new QuotesWsActor(out))
}

class QuotesWsActor(out: ActorRef) extends Actor {

  override def preStart() = {
    context.system.eventStream.subscribe(self, classOf[Scores])
  }

  def receive = {
    case s: Scores =>
      Logger.info(s.toString)
      val t = s.tick
      val tickJson: JsObject = Json.obj(
        "nr" -> t.nr,
        "price" -> t.price,
        "put" -> t.put,
        "call" -> t.call
      )

      val result = Json.obj(
        "scores" -> s.scores.map {case (client:Client, value:Int) => (client.name, value)},
        "tick" -> tickJson
      )

      out ! result
  }
}