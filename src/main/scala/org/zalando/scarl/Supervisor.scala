//
//  Copyright 2016 Zalando SE
//
//  Licensed under the Apache License, Version 2.0 (the "License");
//  you may not use this file except in compliance with the License.
//  You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
//  Unless required by applicable law or agreed to in writing, software
//  distributed under the License is distributed on an "AS IS" BASIS,
//  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//  See the License for the specific language governing permissions and
//  limitations under the License.
//
package org.zalando.scarl

import akka.actor._
import akka.actor.SupervisorStrategy._
import akka.pattern.ask
import scala.concurrent._
import scala.concurrent.duration._

/**
  *
  */
object Supervisor {
  implicit val t: akka.util.Timeout = 60 seconds

  /** spawn a new children at supervisor context
    */
  def actorOf(sup: ActorRef, spec: Specs): Future[ActorRef] = {
    sup.ask(Spawn(spec)).mapTo[ActorRef]
  }

  def actorOf(sup: Future[ActorRef], spec: Specs)(implicit ec: ExecutionContext): Future[ActorRef] = {
    sup flatMap{actorOf(_, spec)}
  }

  /** specification of children process(es) */
  case class Specs(id: String, props: Props)

  /** primitives */
  private[scarl] sealed trait Message
  private[scarl] case object Spawn extends Message
  private[scarl] case object Check extends Message
  private[scarl] case class Spawn(spec: Specs) extends Message

  /** failures */
  class RestartLimitExceeded extends RuntimeException
  class UnknownMessage extends RuntimeException

  /** state idenitity */
  sealed trait SID
  case object Config extends SID // Supervisor is busy to spawn child actors
  case object Active extends SID // Supervisor is active all actors are ready
}

//
// state definition
private[scarl] sealed trait State
private[scarl] case object Nothing extends State
private[scarl] case class Init(
  head: Option[ActorRef],
  list: Seq[Supervisor.Specs],
  refs: List[ActorRef]
) extends State

//
//
abstract
class Supervisor extends FSM[Supervisor.SID, State] with ActorLogging {

  /** specification of child services to spawn
    */
  def specs: Seq[Supervisor.Specs]

  //
  val shutdown = false

  //
  startWith(Supervisor.Config, Init(None, specs, List(context.parent)))

  //
  when(Supervisor.Config) {
    case Event(Supervisor.Spawn, Init(_, Nil, refs)) =>
      refs foreach {_ ! ActorIdentity(None, Some(self))}
      goto(Supervisor.Active) using Nothing

    case Event(Supervisor.Spawn, state @ Init(_, x :: xs, _)) =>
      stay using state.copy(head = Some(spawn(x)), list = xs)

    case Event(ActorIdentity(_, pid), state @ Init(head, _, _)) if pid == head =>
      self ! Supervisor.Spawn
      stay using state.copy(head = None)

    case Event(Supervisor.Check, state @ Init(_, _, refs)) =>
      stay using state.copy(refs = sender() :: refs)

    case x: Any =>
      throw new Supervisor.UnknownMessage
  }

  //
  when(Supervisor.Active) {
    case Event(Terminated(_), Nothing) if !shutdown =>
      throw new Supervisor.RestartLimitExceeded

    case Event(Terminated(_), Nothing)  =>
      context.system.terminate
      throw new Supervisor.RestartLimitExceeded

    case Event(ActorIdentity(None, _), Nothing) =>
      stay

    case Event(Supervisor.Check, _) =>
      sender() ! ActorIdentity(None, Some(self))
      stay

    case Event(Supervisor.Spawn(spec), Nothing) =>
      sender() ! spawn(spec)
      stay
  }

  final
  override
  def preStart() = {
    self ! Supervisor.Spawn
  }

  private
  def spawn(specs: Supervisor.Specs): ActorRef = {
    if (classOf[Supervisor].isAssignableFrom(specs.props.actorClass())) {
      log.info(s"spawn supervisor ${specs.id}")
      context.watch(context.actorOf(specs.props, specs.id))
    } else {
      log.info(s"spawn worker ${specs.id}")
      val pid = context.watch(context.actorOf(specs.props, specs.id))
      pid ! Identify(specs.id)
      pid
    }
  }

  protected
  def strategyOneForOne(maxN: Int, maxT: Duration) =
    OneForOneStrategy(maxNrOfRetries = maxN, withinTimeRange = maxT) {
      case _: Exception => Restart
    }

  protected
  def strategyAllForOne(maxN: Int, maxT: Duration) =
    AllForOneStrategy(maxNrOfRetries = maxN, withinTimeRange = maxT) {
      case _: Exception => Restart
    }

  protected
  def strategyFailSafe() =
    OneForOneStrategy(maxNrOfRetries = 1000000, withinTimeRange = 1 seconds) {
      case _: Exception => Restart
    }

  protected
  def actorOf(props: Props): ActorRef = {
    // todo: suspend supervisor FSM until child are ready
    context.watch(context.actorOf(props))
  }

  protected
  def actorOf(props: Props, id: String): ActorRef = {
    context.watch(context.actorOf(props))
  }
}

//
abstract
class RootSupervisor extends Supervisor {
  override val shutdown = true
}


