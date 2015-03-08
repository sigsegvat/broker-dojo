package at.segv.play.broker

import akka.actor.{ActorRef, Props, Actor}
import akka.actor.Actor.Receive
import akka.event.Logging
import at.segv.play.broker.api._
import collection.JavaConversions._



class Exchange extends Actor {

  val log = Logging(context.system, this)

  var nr = 0

  var price = 1000000

  var currentCall: Set[Client] = Set()
  var currentPut: Set[Client] = Set()

  val scoreMap  = scala.collection.mutable.Map[Client,Int]()

  override def receive: Receive = {

    case 'tickle => {
      nr += 1
      if (currentCall.size > currentPut.size) {
        price += -1
        for(winner <- currentPut) scoreMap.put(winner,scoreMap.getOrElse(winner,0)+1)
      }
      else if (currentCall.size < currentPut.size){
        price += 1
        for(winner <- currentCall) scoreMap.put(winner,scoreMap.getOrElse(winner,0)+1)
      }



      val callArray = currentCall.toArray.map( x => x.name)

      val putArray = currentPut.toArray.map( x => x.name)

      val tick = Tick(nr, price, callArray, putArray)

      for (child <- context.children) child ! tick

      currentCall = Set()
      currentPut = Set()

      log.info(tick.toString)
      log.info(scoreMap.toString())
    }

    case r: Register => {
      context.actorOf(Broker.props(sender(), r.name))
      log.info("registered " + r)
    }

    case Action(Order('call), client) => {
      currentCall = currentCall + client
      log.info("received call from "+client)
    }

    case Action(Order('put), client) => {
      currentPut = currentPut + client
      log.info("received put from "+client)
    }



  }

}


class Broker(client: Client) extends Actor {

  val log = Logging(context.system, this)

  override def receive = waitAction

  override def preStart() = {
    log.info("started: "+client)
  }

  def processTick(t: Tick): Unit = {
    if (sender equals context.parent) {
      client.actor ! t
      context.become(waitAction)
    }
  }

  def waitTick: Receive = {
    case t: Tick =>  processTick(t)

  }

  def waitAction: Receive = {
    case o: Order => {
      context.parent ! Action(o, client)
      context.become(waitTick)
    }
    case t: Tick =>  processTick(t)
  }
}

object Broker {
  def props(actor: ActorRef, name: String) = Props(new Broker(new Client(actor, name)))
}

case class Client(actor: ActorRef, name: String)


case class Action(action: Order, client: Client)

