package at.segv.play.broker.api



@SerialVersionUID(2001)
case class Tick(nr: Int, price: Long, call: Set[String], put: Set[String])


@SerialVersionUID(1001)
case class Register(name: String)
