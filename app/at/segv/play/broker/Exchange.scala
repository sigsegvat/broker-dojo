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

  val scoreMap  = scala.collection.mutable.Map[String,Int]()
  var currentClients: Set[String] = Set()

  val scoreReader: ActorRef = context.actorOf(ScoreReader.props(sender()))

  override def preStart() = {
    scoreReader ! 'readScores

  }

  override def receive : Receive = {
    case s: Scores => {
      nr = s.tick.nr
      price = s.tick.price
      s.scores.foreach( x => scoreMap += x )
      context.become(initialized)
      log.info("inizialized with "+scoreMap)
    }
  }



  def initialized: Receive = {

    case 'tickle => {
      nr += 1
      if (currentCall.size > currentPut.size) {
        price += -1
        for(winner <- currentPut) {
          val winnerString: String = winner.serialize()
          scoreMap.put(winnerString, scoreMap.getOrElse(winnerString, 0) + 1)
        }
        for(looser <- currentCall) {
          val looserString: String = looser.serialize()
          scoreMap.put(looserString, scoreMap.getOrElse(looserString, 0))
        }
      }
      else if (currentCall.size < currentPut.size){
        price += 1
        for(winner <- currentCall) {
          val winnerString: String = winner.serialize()
          scoreMap.put(winnerString, scoreMap.getOrElse(winnerString, 0) + 1)
        }
        for(looser <- currentPut) {
          val looserString: String = looser.serialize()
          scoreMap.put(looserString, scoreMap.getOrElse(looserString, 0))
        }
      }



      val tick = Tick(nr, price, currentCall.size, currentPut.size)

      context.system.eventStream.publish(tick)

      context.system.eventStream.publish(Scores(tick,scoreMap.toMap))

      currentCall = Set()
      currentPut = Set()

      log.info(tick.toString)


    }

    case r: Register => {
      val client: Client = Client(sender(), r.name)
      if (! (currentClients contains client.serialize())){
        context.actorOf(Broker.props(client))
        log.info("registered " + r)
      }
      else {
        log.info("prevented double reg of " + r)
      }

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

case class Client(actor: ActorRef, name: String) {
  def serialize()= {
    name+"@"+actor.toString()
  }
}

case class Action[T <: Order](action: T, client: Client)

case class Scores(tick: Tick, scores: Map[String,Int])
