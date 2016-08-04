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
import scala.annotation.tailrec
import scala.concurrent._
import scala.concurrent.duration._
import scala.util.{Try, Success, Failure}

/**
  *
  */
object Supervisor {
  implicit val t: akka.util.Timeout = 60 seconds

  /** spawn children
    */
  def spawn(sup: ActorRef, spec: Instance): Future[ActorRef] = {
    sup.ask(Spawn(spec)).mapTo[ActorRef]
  }

  /** synchronously resolve actor selection
    */
  def resolve(selector: ActorSelection): Option[ActorRef] = {
    resolve(selector, 5 seconds)
  }

  def resolve(selector: ActorSelection, t: FiniteDuration): Option[ActorRef] = {
    retry(t){Await.result(selector.resolveOne(t), t)}
  }

  /** lookup children
    */
  def child(sup: ActorRef, id: String)(implicit sys: ActorSystem): Option[ActorRef] = {
    val f = sys.actorSelection(sup.path / id).resolveOne(5 seconds)
    Try {Await.result(f, 5 seconds)} match {
      case Success(x) => Some(x)
      case _ => None
    }
  }

  /** time constrained retry (time in milliseconds)
    *
    */
  private
  def retry[T](t: FiniteDuration)(fn: => T): Option[T] = {
    val t0 = System.currentTimeMillis

    @annotation.tailrec
    def retryUntil[T](deadline: Long, fn: => T): Option[T] = {
      Try { fn } match {
        case Success(x) => Some(x)
        case _ if deadline < System.currentTimeMillis => retryUntil(deadline, fn)
        case Failure(e) => None
      }
    }

    retryUntil(t0 + t.toMillis, fn)
  }


  /** specification of children */
  sealed trait Instance {
    def id: String
    def props: Props
  }
  case class Worker(id: String, props: Props) extends Instance
  case class Supervisor(id: String, props: Props) extends Instance

  /** primitives */
  private[scarl] sealed trait Message
  private[scarl] case object Spawn extends Message
  private[scarl] case object Check extends Message
  private[scarl] case class Spawn(spec: Instance) extends Message

  /** failures */
  class RestartLimitExceeded extends RuntimeException
  class UnknownMessage extends RuntimeException

  /** state idenitity */
  sealed trait SID
  case object Config extends SID // Supervisor is busy to spawn child actors
  case object Active extends SID // Supervisor is active all actors are ready

  //
  implicit
  class SystemSupervisor(val sys: ActorSystem) extends scala.AnyRef {
    /** sequentially spawn root supervisor and it children
      */
    def supervisor(spec: Supervisor): ActorRef = {
      import akka.pattern.ask
      implicit val t: akka.util.Timeout = 60 seconds

      @tailrec
      def wait(sup: ActorRef): ActorRef = {
        Await.result(sup ? Check, Duration.Inf) match {
          case Config =>
            wait(sup)
          case Active =>
            sup
        }
      }
      wait(sys.actorOf(spec.props, spec.id))
    }
  }
}

//
// state definition
private[scarl] sealed trait State
private[scarl] case object Nothing extends State
private[scarl] case class Init(head: Option[ActorRef], list: Seq[Supervisor.Instance]) extends State

//
//
abstract
class Supervisor extends FSM[Supervisor.SID, State] with ActorLogging {
  /** specification of services to spawn*/
  def init: Seq[Supervisor.Instance]

  //
  val shutdown = false

  //
  startWith(Supervisor.Config, Init(None, init))

  //
  when(Supervisor.Config) {
    case Event(Supervisor.Spawn, Init(_, Nil)) =>
      context.parent ! ActorIdentity(None, Some(self))
      goto(Supervisor.Active) using Nothing

    case Event(Supervisor.Spawn, Init(_, x :: xs)) =>
      stay using Init(Some(spawn(x)), xs)

    case Event(ActorIdentity(_, pid), Init(head, list)) if pid == head =>
      self ! Supervisor.Spawn
      stay using Init(None, list)

    case Event(Supervisor.Check, _) =>
      sender() ! Supervisor.Config
      stay

    case _ =>
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
      sender() ! Supervisor.Active
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
  def spawn(spec: Supervisor.Instance): ActorRef =
    spec match {
      case Supervisor.Worker(id, props) =>
        log.info(s"spawn worker $id")
        val pid = context.watch(context.actorOf(props, id))
        pid ! Identify(id)
        pid

      case Supervisor.Supervisor(id, props) =>
        log.info(s"spawn supervisor $id")
        context.watch(context.actorOf(props, id))
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
}

//
abstract
class RootSupervisor extends Supervisor {
  override val shutdown = true
}


