package waveskimmer

import com.typesafe.scalalogging.Logger

import scala.util.{Failure, Success, Try}

package object statemachine {

  case class Context[Data, Event](
      data: Data,
      currentState: State[Data, Event],
      instance: StateMachine[Data, Event],
      _wrapped: Option[Event] = None // internal use
  ) {
    def event: Event = _wrapped.getOrElse(assert(assertion = false, "No Event was available"))
  }

  type OnMethod[Data, Event] = Context[Data, Event] => Context[Data, Event]

  /** add to a state to indicate that it is an initial state
    */
  trait Initial

  /** add to a state to indicate that it is a terminal state
    */
  trait Terminal

  case class State[Data, Event](
      name: String,
      onEntry: OnMethod[Data, Event] = identity,
      onExit: OnMethod[Data, Event] = identity
  ) {
    private val logger = Logger[State[_, _]]

    def >>(that: State[Data, Event]): Transition[Data, Event] = Transition(this, that)

  }

  case class Transition[Data, Event](
      from: State[Data, Event],
      to: State[Data, Event],
      doWhen: Context[Data, Event] => Boolean = _ => true,
      doUpon: OnMethod[Data, Event] = identity
  ) {
    private val logger = Logger[Transition[_, _]]

    def when(predicate: Context[Data, Event] => Boolean): Transition[Data, Event] = this.copy(doWhen = predicate)

  }

  class StateMachineGraph[Data, Event](transitionsParam: List[Transition[Data, Event]]) {
    val transitionMap: Map[State[Data, Event], List[Transition[Data, Event]]] = transitionsParam.groupBy { _.from }
    val initialStates: Set[State[Data, Event]] = transitionMap.keys.filter(_.isInstanceOf[Initial]).toSet

  }

  /** A statemachine contains mutable state by definition as it takes the combination of current state and on a set of
    * rules.
    */
  abstract class StateMachine[Data, Event](graph: StateMachineGraph[Data, Event]) {
    val logger = Logger[StateMachine[_, _]]

    def this(transitions: List[Transition[Data, Event]]) =
      this(new StateMachineGraph(transitions))

    def start(data: Data, initialState: Option[State[Data, Event]] = None): Context[Data, Event] = {
      val state = initialState match {
        case Some(state: State[Data, Event]) =>
          assert(graph.initialStates.contains(state), s"$state is not an Initial State")
          state
        case None =>
          assert(
            graph.initialStates.size == 1,
            s"When more than one initial state is provided, an initial state must be provided"
          )
          graph.initialStates.head
      }

      Context(data, state, this, None)
    }

    def process(event: Event, context: Context[Data, Event]): Any

    /** will be called when this state because current of the statemachine */
    private def invokeOnEntry(state: State[Data, Event], ctx: Context[Data, Event]): Context[Data, Event] = {
      Try[Context[Data, Event]] {
        state.onEntry(ctx)
      } match {
        case Success(context) =>
          logger.debug(s"Success calling onEntry for $this")
          context
        case Failure(e) =>
          logger.error(s"Error calling onEntry() on $this", e)
          ctx
      }
    }

    private def invokeOnExit(state: State[Data, Event], ctx: Context[Data, Event]): Context[Data, Event] = {
      Try[Context[Data, Event]] {
        state.onExit(ctx)
      } match {
        case Success(context) =>
          logger.debug(s"Success calling onEntry for $this")
          context
        case Failure(e) =>
          logger.error(s"Error calling onEntry() on $this", e)
          ctx
      }
    }

    private def invokeOnStateChange(
        transition: Transition[Data, Event],
        ctx: Context[Data, Event]
    ): Context[Data, Event] = {
      Try[Context[Data, Event]] {
        transition.doUpon(ctx)
      } match {
        case Success(context) =>
          logger.debug(s"Success calling onEntry for $this")
          context
        case Failure(e) =>
          logger.error(s"Error calling onEntry() on $this", e)
          ctx
      }
    }

    protected def stateChange(
        transition: Transition[Data, Event],
        context: Context[Data, Event]
    ): Context[Data, Event] = {
      logger.debug(s"$transition will transition from ${transition.from} to ${transition.to}")
      Option(context)
        .map(ctx => invokeOnExit(transition.from, ctx))
        .map(ctx => ctx.copy(currentState = transition.to))
        .map(ctx => invokeOnStateChange(transition, ctx))
        .map(ctx => invokeOnEntry(transition.to, ctx))
        .get
    }

  }

}
