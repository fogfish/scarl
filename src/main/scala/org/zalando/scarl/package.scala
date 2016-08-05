//
//  Copyright 2015 Zalando SE
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
package org.zalando

import akka.actor._
import scala.concurrent.{Promise, Future}
import scala.concurrent.duration._
import scala.util.{Failure, Success, Try}

package object scarl {

  implicit
  class ScarlSelection(val selector: ActorSelection) extends scala.AnyRef {

    /** synchronously resolve actor selection
      */
    def resolve(): Option[ActorRef] = {
      resolve(5.seconds)
    }

    def resolve(t: FiniteDuration): Option[ActorRef] = {
      retry(t){selector.resolveOne(t)}
    }

    /** synchronously lookup actor selection
      */
    def lookup(): Option[ActorRef] = {
      lookup(5.seconds)
    }

    def lookup(t: FiniteDuration): Option[ActorRef] = {
      selector.resolveOne(t).value flatMap {_.toOption}
    }
  }

  implicit
  class ScarlRef(val ref: ActorRef) extends scala.AnyRef {

    /** synchronously lookup child actor
      */
    def lookup(id: String)(implicit sys: ActorSystem): Option[ActorRef] = {
      lookup(id, 5.seconds)
    }

    def lookup(id: String, t: FiniteDuration)(implicit sys: ActorSystem): Option[ActorRef] = {
      sys.actorSelection(ref.path / id).resolveOne(t).value flatMap {_.toOption}
    }
  }


  implicit
  class ScarlSupervisor(val sys: ActorSystem) extends scala.AnyRef {

    /** synchronously spawn root supervisor and it children
      */
    def rootSupervisor(specs: Supervisor.Specs): ActorRef = {
      rootSupervisor(specs, 1800.seconds)
    }

    def rootSupervisor(specs: Supervisor.Specs, t: FiniteDuration): ActorRef = {
      import akka.pattern.ask
      implicit val timeout: akka.util.Timeout = 5 seconds
      implicit val ec = sys.dispatcher

      val root = sys.actorOf(specs.props, specs.id)

      def req() = {
        val p = Promise[ActorRef]()
        root.ask(Supervisor.Check)
          .map {
            case Supervisor.Config => None
            case Supervisor.Active => Some(root)
          }
          .onComplete {
            case Success(Some(x)) => p.success(x)
            case Success(None) => p.failure(new RuntimeException)
            case _ => p.failure(new RuntimeException)
          }
        p.future
      }

      retry(t){req()}
      root
    }
  }



  /** time constrained retry (time in milliseconds)
    */
  private
  def retry[T](t: FiniteDuration)(fn: => Future[T]): Option[T] = {
    val t0 = System.currentTimeMillis

    @annotation.tailrec
    def retryUntil[T](deadline: Long, fn: => Future[T], f: Future[T]): Option[T] = {
      f.value match {
        // future is not completed yet, do not call fn
        case None if deadline > System.currentTimeMillis =>
          retryUntil(deadline, fn, f)
        // future is completed
        case x =>
          x flatMap {_.toOption} match {
            case None if deadline > System.currentTimeMillis =>
              retryUntil(deadline, fn, fn)
            case x: Option[T] =>
              x
          }
      }
    }

    retryUntil(t0 + t.toMillis, fn, fn)
  }

}
