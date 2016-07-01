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
//  scarl application root supervisor
package org.zalando.scarl.erlang

import akka.actor._
import org.zalando.scarl._
import akka.util.Timeout
import scala.concurrent.Await



class ScarlRootSup(node: String, dist: Listener) extends Actor {
  import akka.actor.SupervisorStrategy._
  import akka.actor.{OneForOneStrategy, Props}

  import scala.concurrent.duration._

  override val supervisorStrategy =
    OneForOneStrategy(maxNrOfRetries = 60, withinTimeRange = 1 minute) {
      case _: Exception => Restart
    }

  override def preStart() = {
    spawn("default", node)
  }

  def receive = {
    case ('spawn, id: String) =>
      sender() ! spawn(id, id)
  }

  private def spawn(name: String, id: String) = {
    context.actorOf(Props(new NodeSup(id + "@" + dist.host, dist.cookie)), name)
  }
}


object ScarlRootSup {
  import akka.pattern.ask

  import scala.concurrent.duration._
  implicit val timeout = Timeout(5 seconds)

  def spawn(id: String)(implicit sys: ActorSystem): ActorRef = {
    implicit val cx = sys.dispatcher
    val req = sys.actorSelection("/user/" + Scarl.uid)
      .resolveOne
      .flatMap {
        sup: ActorRef => {
          ask(sup, ('spawn, id)).mapTo[ActorRef]
        }
      }
    Await.result(req, timeout.duration)
  }
}
