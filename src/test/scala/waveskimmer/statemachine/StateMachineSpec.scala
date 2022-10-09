package waveskimmer.statemachine

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should

class StateMachineSpec extends AnyFlatSpec with should.Matchers {

  case class MyData(n: Int, name: String)

  object stepOne extends State[MyData, String]("One") with Initial
  object stepTwo extends State[MyData, String]("Two")
  object stepThree extends State[MyData, String]("Three")
  object finished extends State[MyData, String]("finished") with Terminal

  object simple
      extends StateMachine[MyData, String](
        List(
          stepOne >> stepTwo when { ctx: Context[MyData, String] => ctx.data.n >= 1 },
          stepTwo >> stepThree,
          stepThree >> finished
        )
      )

//    stepOne >> stepTwo >> stepThree >> finished

//  behavior of "simple machine"
//
//  it should "work for simple machine" {
//
//    assert(condition = true)
//    1
//  }

}
