package controllers

import akka.actor._
import at.segv.play.broker.Scores
import at.segv.play.broker.api.Tick
import play.api.Logger
import play.api.libs.json.{JsArray, JsObject, JsNumber, JsValue}
import play.api.mvc._
import play.api.Play.current
import play.api.libs.json.Json

import scala.util.parsing.json.JSONObject

object Application extends Controller {

  def index = Action {
    Ok(views.html.index("Your new application is ready."))
  }

  def quotes = WebSocket.acceptWithActor[JsValue, JsValue] { request => out =>
    QuotesWsActor.props(out)
  }

}