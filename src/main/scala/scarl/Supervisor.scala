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
// @description
//  application root supervisor
package scarl

import akka.actor.{ActorSystem, ActorRef, Actor}
import akka.util.Timeout

import scala.concurrent.Await

class Supervisor(node: String, host: String, cookie: String) extends Actor {
  import akka.actor.{OneForOneStrategy, Props}
  import akka.actor.SupervisorStrategy._
  import scala.concurrent.duration._

  override val supervisorStrategy =
    OneForOneStrategy(maxNrOfRetries = 60, withinTimeRange = 1 minute) {
      case _: Exception => Restart
    }

  override def preStart() = {
    spawn("default", node, cookie)
  }

  def receive = {
    case ('spawn, id: String, cookie: String) =>
      sender() ! spawn(id, id, cookie)
    case ('spawn, id: String) =>
      sender() ! spawn(id, id, cookie)
  }

  private def spawn(name: String, id: String, cookie: String) = {
    context.actorOf(Props(new NodeSup(id + "@" + host, cookie)), name)
  }
}


object Supervisor {
  import akka.pattern.ask
  import scala.concurrent.duration._
  implicit val timeout = Timeout(5 seconds)

  def spawn(id: String, cookie: String)(implicit sys: ActorSystem): ActorRef = {
    implicit val cx = sys.dispatcher
    val req = sys.actorSelection("/user/scarl")
      .resolveOne
      .flatMap {
        sup: ActorRef => {
          ask(sup, ('spawn, id, cookie)).mapTo[ActorRef]
        }
      }
    Await.result(req, timeout.duration)
  }
}
