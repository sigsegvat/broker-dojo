package at.segv.play.broker

import akka.actor.{ActorRef, Props, Actor}
import akka.actor.Actor.Receive
import akka.event.Logging
import at.segv.play.broker.api._


class Exchange extends Actor {

  val log = Logging(context.system, this)

  var nr = 0

  var price = 1000000

  var currentCall: Set[Client] = Set()
  var currentPut: Set[Client] = Set()


  override def receive(): Receive = {

    case 'tickle => {
      nr += 1
      if (currentCall.size < currentPut.size) price += 1
      val tick = Tick(nr, 0, currentCall.map(x => x.name), currentPut.map(x => x.name))
      for (child <- context.children) child ! tick
      currentCall = Set()
      currentPut = Set()
      log.info("sent tick " + tick)
    }

    case r: Register => {
      context.actorOf(Broker.props(sender(), r.name))
      log.info("registered " + r)
    }

    case Action('call, client) => currentCall = currentCall + client
    case Action('put, client) => currentCall = currentCall + client



  }

}


class Broker(client: Client) extends Actor {

  val log = Logging(context.system, this)

  override def receive = waitAction

  override def preStart() = {
    log.info("started: "+client)
  }

  def waitTick: Receive = {
    case t: Tick =>  processTick(t)

  }

  def processTick(t: Tick): Unit = {
    if (sender equals context.parent) {
      client.actor ! t
      context.become(waitAction)
      log.info("sent tick " + t)
    }
  }

  def waitAction: Receive = {
    case s: Symbol => {
      context.parent ! Action(s, client)
      context.become(waitTick)
      log.info("wait for tick")
    }
    case t: Tick =>  processTick(t)
  }
}

object Broker {
  def props(actor: ActorRef, name: String) = Props(new Broker(new Client(actor, name)))
}

case class Client(actor: ActorRef, name: String)


case class Action(action: Symbol, client: Client)

