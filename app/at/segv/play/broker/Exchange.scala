package at.segv.play.broker

import akka.actor.{ActorRef, Actor}
import akka.event.Logging
import at.segv.play.broker.api._
import controllers.ScoreReader


class Exchange extends Actor {

  val log = Logging(context.system, this)

  var nr = 0

  var price: Long = 1000000

  var currentCall: Set[Client] = Set()
  var currentPut: Set[Client] = Set()

  val scoreMap  = scala.collection.mutable.Map[Client,Int]()

  val scoreReader: ActorRef = context.actorOf(ScoreReader.props(sender()))

  override def preStart() = {
    scoreReader ! 'readScores

  }

  override def receive : Receive = {
    case list: List[Tick] => {
      nr = list.head.nr
      price = list.head.price
      context.become(initialized)
    }
  }

  def initialized: Receive = {

    case 'tickle => {
      nr += 1
      if (currentCall.size > currentPut.size) {
        price += -1
        for(winner <- currentPut) scoreMap.put(winner,scoreMap.getOrElse(winner,0)+1)
        for(looser <- currentCall) scoreMap.put(looser,scoreMap.getOrElse(looser,0))
      }
      else if (currentCall.size < currentPut.size){
        price += 1
        for(winner <- currentCall) scoreMap.put(winner,scoreMap.getOrElse(winner,0)+1)
        for(looser <- currentPut) scoreMap.put(looser,scoreMap.getOrElse(looser,0))
      }

      val tick = Tick(nr, price, currentCall.size, currentPut.size)

      context.system.eventStream.publish(tick)

      context.system.eventStream.publish(Scores(tick,scoreMap.toMap))

      currentCall = Set()
      currentPut = Set()

      log.info(tick.toString)

    }

    case r: Register => {
      context.actorOf(Broker.props(sender(), r.name))
      log.info("registered " + r)
    }

    case Action(p: PutOrder, client) => {
      currentCall = currentCall + client
      log.info("received call from "+ client)
    }

    case Action(c: CallOrder, client) => {
      currentPut = currentPut + client
      log.info("received put from "+client)
    }



  }

}

case class Client(actor: ActorRef, name: String)

case class Action[T <: Order](action: T, client: Client)

case class Scores(tick: Tick, scores: Map[Client,Int])
