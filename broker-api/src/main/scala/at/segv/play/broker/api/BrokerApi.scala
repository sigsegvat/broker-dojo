package at.segv.play.broker.api



@SerialVersionUID(2001)
case class Tick(nr: Int, price: Long, callVol: Int, putVol: Int)

@SerialVersionUID(1001)
case class Register(name: String)

@SerialVersionUID(3002)
trait Order {
  val volume: Int
}

@SerialVersionUID(4001)
case class CallOrder(volume: Int) extends Order

@SerialVersionUID(5001)
case class PutOrder(volume: Int) extends Order