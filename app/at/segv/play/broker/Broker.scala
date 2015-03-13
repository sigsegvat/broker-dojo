package at.segv.play.broker

import akka.actor.{ActorRef, Props, Actor}
import akka.actor.Actor.Receive
import akka.event.Logging
import at.segv.play.broker.api._
import collection.JavaConversions._

class Broker(client: Client) extends Actor {

  val log = Logging(context.system, this)

  override def receive = waitAction

  override def preStart() = {
    context.system.eventStream.subscribe(self, classOf[Tick])
  }

  def processTick(t: Tick): Unit = {
      client.actor ! t
      context.become(waitAction)
  }

  def waitTick: Receive = {
    case t: Tick=>  {
      processTick(t)
    }
    case _ => {
      log.info("invalid message from "+client);
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
    case t: Tick =>  processTick(t)
  }
}

object Broker {
  def props(actor: ActorRef, name: String) = Props(new Broker(new Client(actor, name)))
}


