package waveskimmer.statemachine

class StateMachine(val name: String):
  var state = "hello"



@main def hello(): Unit =
  val sm = new StateMachine("First")
  println(s"state machine = ${sm.name}")

