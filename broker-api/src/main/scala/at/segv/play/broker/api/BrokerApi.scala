package at.segv.play.broker.api



@SerialVersionUID(2001)
case class Tick(nr: Int, price: Long, call: Array[String], put: Array[String]){

  override def toString = {
    "Tick (nr: "+nr+" price: "+price +" calls: "+ call.toList + " puts: "+ put.toList;
  }
}


@SerialVersionUID(1001)
case class Register(name: String)

@SerialVersionUID(3001)
case class Order(sym: Symbol)

object Order {
  def put = Order('put)

  def call = Order('call)
}
