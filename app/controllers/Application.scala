package controllers

import akka.actor._
import at.segv.play.broker.api.Tick
import play.api.mvc._
import play.api.Play.current

object Application extends Controller {

  def index = Action {
    Ok(views.html.index("Your new application is ready."))
  }

  def quotes = WebSocket.acceptWithActor[String, String] { request => out =>
    MyWebSocketActor.props(out)
  }


  object MyWebSocketActor {
    def props(out: ActorRef) = Props(new MyWebSocketActor(out))
  }

  class MyWebSocketActor(out: ActorRef) extends Actor {
    var count = 0;

    override  def preStart() = {
      context.system.eventStream.subscribe(self,classOf[Tick])
    }

    def receive = {
      case msg: Tick =>
        count += 1
        out ! ("I received your message: "+ count +" "+ msg)
    }
  }

}