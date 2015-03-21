package at.segv.play.broker

import akka.actor.{PoisonPill, ActorRef, Props, Actor}
import akka.actor.Actor.Receive
import akka.event.Logging
import at.segv.play.broker.api._
import collection.JavaConversions._

class Broker(client: Client) extends Actor {

  val log = Logging(context.system, this)

  var lastAction = 0;

  override def receive = waitTick

  override def preStart() = {
    context.system.eventStream.subscribe(self, classOf[Tick])
  }

  def processTick(t: Tick): Unit = {
    client.actor ! t

    if (t.nr - lastAction > 10) {
      log.info("died of boredom: "+client)
      self ! PoisonPill
    }
    else {
      context.become(waitAction)
    }
  }

  def waitTick: Receive = {
    case t: Tick => {
      lastAction = t.nr
      log.info("process: "+lastAction)
      processTick(t)

    }
    case _ => {
      log.info("invalid message from " + client);
    }
  }

  def waitAction: Receive = {
    case p: PutOrder => {
      context.parent ! Action(p, client)
      context.become(waitTick)
    }
    case c: CallOrder => {
      context.parent ! Action(c, client)
      context.become(waitTick)
    }
    case t: Tick => processTick(t)
  }
}

object Broker {
  def props(client: Client) = Props(new Broker(client))
}


