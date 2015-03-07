import java.util.concurrent.TimeUnit

import akka.actor.{ActorSystem, Props}
import at.segv.play.broker.Exchange
import play.libs.Akka
import play.api._

import scala.concurrent.ExecutionContext
import scala.concurrent.duration.Duration
import ExecutionContext.Implicits.global


object Global extends GlobalSettings {

  override def onStart(app: Application) {
    super.onStart(app)

    val exchange = Akka.system.actorOf(Props[Exchange],"exchange")
    Akka.system.scheduler.schedule(Duration(1,TimeUnit.SECONDS), Duration(1,TimeUnit.SECONDS), exchange, 'tickle)

    Logger.info("Exchange: "+exchange)
  }



  override def onStop(app: Application) {
    super.onStop(app)
  }


}

