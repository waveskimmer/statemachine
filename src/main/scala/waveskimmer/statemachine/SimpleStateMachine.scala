package waveskimmer.statemachine

import com.typesafe.scalalogging.Logger

/** Immediate machine will process events on the caller's thread
  */
class SimpleStateMachine[Data, Event](
    graph: StateMachineGraph[Data, Event]
) extends StateMachine(graph) {
  private val logger = Logger[SimpleStateMachine[_, _]]

  override def process(event: Event, context: Context[Data, Event]): Context[Data, Event] = {
    val ctx = context.copy(_wrapped = Option(event))
    graph.transitionMap(ctx.currentState).find(_.doWhen(ctx)) match {
      case None =>
        logger.debug(s"No more Transitions")
        ctx
      case Some(t) =>
        process(event, stateChange(t, ctx))
    }
  }

}
