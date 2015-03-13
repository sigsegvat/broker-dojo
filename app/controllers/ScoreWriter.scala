package controllers

import akka.actor.{Actor, Props, ActorRef}
import akka.actor.Actor.Receive
import akka.event.Logging
import at.segv.play.broker.Scores
import at.segv.play.broker.api.Tick
import scala.slick.driver.H2Driver
import scala.slick.driver.H2Driver.simple._



class ScoreReader extends Actor {

  private val db: H2Driver.Backend#DatabaseDef = Database.forURL("jdbc:h2:./db/broker.h2.db", driver = "org.h2.Driver")

  override def receive: Actor.Receive = {
    case 'readScores =>
      db.withSession {
        implicit session =>
          val list: List[Tick] = Ticks.ticks.sortBy(_.id.desc).take(10).list
          sender ! list
      }
  }
}

object ScoreReader {
  def props(out: ActorRef) = Props(new ScoreReader)
}

class ScoreWriter extends Actor {

  private val db: H2Driver.Backend#DatabaseDef = Database.forURL("jdbc:h2:./db/broker.h2.db", driver = "org.h2.Driver")

  val log = Logging(context.system, this)



  override def preStart() = {
    context.system.eventStream.subscribe(self, classOf[Scores])
  }

  override def receive: Receive = {
    case Scores(tick,scores) =>  {
      log.info("writing " +tick)
      db.withSession {
        implicit session =>
          Ticks.ticks += tick
      }
    }
  }
}

object ScoreWriter {
  def props(out: ActorRef) = Props(new ScoreWriter)
}

class Ticks(tag: Tag) extends Table[Tick](tag, "TICKS") {
  def id = column[Int]("TICK_NR", O.PrimaryKey) // This is the primary key column
  def price = column[Long]("PRICE")
  def callVol = column[Int]("CALL")
  def putVol = column[Int]("PUT")

  def * = (id, price, callVol, putVol) <> (Tick.tupled, Tick.unapply)
}

object Ticks {
  lazy val ticks = TableQuery[Ticks]
}