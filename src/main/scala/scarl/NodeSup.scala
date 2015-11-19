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
// @doc
//
package scarl

import akka.actor.{ActorSystem, ActorRef, Actor}
import akka.util.Timeout
import com.ericsson.otp.erlang.OtpNode


class NodeSup(id: String, cookie: String) extends Actor {
  import akka.actor.{OneForOneStrategy, Props}
  import akka.actor.SupervisorStrategy._
  import scala.concurrent.duration._

  var node: OtpNode = _
  override val supervisorStrategy =
    OneForOneStrategy(maxNrOfRetries = 60, withinTimeRange = 1 minute) {
      case _: Exception => Restart
    }

  override def preStart() = {
    node = new OtpNode(id, cookie)
  }

  def receive = {
    case ('bind, mbox: String, actor: ActorRef) =>
      sender() ! context.actorOf(Props(new Bridge(node, mbox, actor)))
    case ('bind, mbox: String, actor: (Any => Any)) =>
      sender() ! context.actorOf(Props(new Functor(node, mbox, actor)))
  }
}


object NodeSup {
  import akka.pattern.ask
  import scala.concurrent.duration._
  import scala.concurrent.Await

  implicit val timeout = Timeout(5 seconds)

  def bind(node: String, name: String, actor: Any)(implicit sys: ActorSystem): ActorRef = {
    implicit val cx = sys.dispatcher
    val req = sys.actorSelection("/user/scarl/" + node)
      .resolveOne
      .flatMap {
        sup: ActorRef => {
          ask(sup, ('bind, name, actor)).mapTo[ActorRef]
        }
      }
    Await.result(req, timeout.duration)
  }
}
