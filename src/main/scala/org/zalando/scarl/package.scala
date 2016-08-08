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
import scala.concurrent.{ExecutionContext, Future}
import scala.concurrent.duration._

package object scarl {

  implicit
  class ScarlSelection(val selector: ActorSelection) extends scala.AnyRef {

    /** synchronously resolve actor selection
      */
    def resolve()(implicit ec: ExecutionContext): Future[ActorRef] = {
      resolve(5.seconds)
    }

    def resolve(t: FiniteDuration)(implicit ec: ExecutionContext): Future[ActorRef] = {
      retry(t){selector.resolveOne(t)}
    }
  }


  implicit
  class ScarlSupervisor(val sys: ActorSystem) extends scala.AnyRef {

    /** synchronously spawn root supervisor and it children
      */
    def rootSupervisor(specs: Supervisor.Specs): Future[ActorRef] = {
      rootSupervisor(specs, 1800.seconds)
    }

    def rootSupervisor(specs: Supervisor.Specs, t: FiniteDuration): Future[ActorRef] = {
      import akka.pattern.ask
      implicit val timeout: akka.util.Timeout = 5 seconds
      implicit val ec = sys.dispatcher

      sys.actorOf(specs.props, specs.id)
        .ask(Supervisor.Check)
        .map {
          case ActorIdentity(None, Some(x)) => x
        }
    }
  }



  /** time constrained retry (time in milliseconds)
    */
  private
  def retry[T](t: FiniteDuration)(fn: => Future[T])(implicit ec: ExecutionContext): Future[T] = {
    val t0 = System.currentTimeMillis

    def retryUntil[T](deadline: Long, fn: => Future[T]): Future[T] = {
      fn recoverWith {
        case _: Throwable if deadline > System.currentTimeMillis =>
          retryUntil(deadline, fn)
      }
    }

    retryUntil(t0 + t.toMillis, fn)
  }

}
