package controllers

import akka.actor.{ActorPath, Actor, Props, ActorRef}
import akka.serialization._

import akka.event.Logging
import at.segv.play.broker.{Client, Scores}
import at.segv.play.broker.api.Tick

import scala.collection.mutable
import scala.slick.driver.H2Driver.simple._


class ScoreReader extends Actor with DbCon {


  override def receive: Actor.Receive = {
    case 'readScores =>
      db.withSession {
        implicit session =>
          val list: List[Tick] = Ticks.ticks.sortBy(_.id.desc).take(10).list

          if (!list.isEmpty) {
            val lastTick: Tick = list.head
            val map: mutable.Map[String, Int] = mutable.HashMap()
            for (k <- ScoresTab.scores.filter(_.tickId === lastTick.nr).list) {
              map += k._2 -> k._3
            }
            sender ! Scores(lastTick, map.toMap)
          }
          else {
            sender ! Scores(Tick(0, 1000000, 0, 0), Map())
          }


      }
  }
}

object ScoreReader {
  def props(out: ActorRef) = Props(new ScoreReader)
}

class ScoreWriter extends Actor with DbCon {

  val log = Logging(context.system, this)

  override def preStart() = {
    context.system.eventStream.subscribe(self, classOf[Scores])
    try {
      db.withSession(implicit session => Ticks.ticks.ddl.create)
      db.withSession(implicit session => ScoresTab.scores.ddl.create)
    }
    catch{ case _ => None}

  }

  override def receive: Receive = {
    case Scores(tick, scores) => {
      db.withSession {
        implicit session =>
          Ticks.ticks += tick
          for ((c, v) <- scores) {
            ScoresTab.scores += ((None, c, v, tick.nr))

          }
      }
    }
  }
}

object ScoreWriter {
  def props(out: ActorRef) = Props(new ScoreWriter)
}

class Ticks(tag: Tag) extends Table[Tick](tag, "TICKS") {
  def id = column[Int]("TICK_NR", O.PrimaryKey)

  def price = column[Long]("PRICE")

  def callVol = column[Int]("CALL")

  def putVol = column[Int]("PUT")

  def * = (id, price, callVol, putVol) <>(Tick.tupled, Tick.unapply)
}

object Ticks {
  lazy val ticks = TableQuery[Ticks]
}


class ScoresTab(tag: Tag) extends Table[(Option[Int], String, Int, Int)](tag, "SCORE") {
  def id = column[Int]("ID", O.PrimaryKey, O.AutoInc)

  // This is the primary key column
  def client = column[String]("CLIENT")

  def score = column[Int]("SCORE")

  def tickId = column[Int]("TICK_ID")

  def * = (id.?, client, score, tickId)

  def tickFk = foreignKey("FK_TICK_ID", tickId, Ticks.ticks)(_.id)


}

object ScoresTab {
  lazy val scores = TableQuery[ScoresTab]
}