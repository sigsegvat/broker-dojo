package controllers

import scala.slick.driver.H2Driver
import scala.slick.driver.H2Driver.simple._


trait DbCon {
  val db: H2Driver.Backend#DatabaseDef = Database.forURL("jdbc:h2:./db/broker.h2.db", driver = "org.h2.Driver")
}
