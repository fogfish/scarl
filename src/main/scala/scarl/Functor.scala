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
//
package scarl

import akka.actor.Actor
import com.ericsson.otp.erlang._
import scarl.Scarl.Envelop

class Functor[A, B](node: OtpNode, name: String, actor: Any => Option[Envelop])
  extends Actor
  with Mailbox {

  implicit val exec = context.system.dispatcher
  val mbox: OtpMbox = node.createMbox(name)

  override def preStart() = {
    self ! 'recv
  }

  def receive = {
    case 'recv =>
      recv(self)

    case message: OtpErlangObject =>
      actor(decode(message)) map {send(_)}
      recv(self)

    case _ =>

  }

}
