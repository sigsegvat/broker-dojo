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
    case Scores(tick,scores) =>
      if(tick.nr % 2 == 0) {
        val tickJson: JsObject = Json.obj(
          "nr" -> tick.nr,
          "price" -> tick.price,
          "putVol" -> tick.putVol,
          "callVol" -> tick.callVol
        )

        val result = Json.obj(
          "scores" -> scores.map {case (client:String, value:Int) => (client, value)},
          "tick" -> tickJson
        )

        out ! result
      }

  }
}