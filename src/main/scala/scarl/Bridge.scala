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

import java.util.concurrent.TimeUnit

import akka.actor.{ActorRef, Actor}
import akka.pattern.pipe
import com.ericsson.otp.erlang._

import scala.concurrent.Future
import scala.concurrent.duration.Duration

class Bridge(node: OtpNode, name: String, actor: ActorRef) extends Actor with Message {
  implicit val cx   = context.system.dispatcher
  var mbox: OtpMbox = _

  override def preStart() = {
    mbox = node.createMbox(name)
    context.system
      .scheduler
      .scheduleOnce(
        Duration.create(0, TimeUnit.SECONDS),
        context.self,
        'run
      )(context.system.dispatcher)
  }

  def receive = {
    case 'run =>
      recv()
    case msg: OtpErlangObject =>
      actor ! decode(msg)
      recv()
    case ('send, to: OtpErlangPid, msg: Any) =>
      mbox.send(to, encode(msg))
    case ('send, to: String, node: String, msg: Any) =>
      mbox.send(to, node, encode(msg))
    case _ =>

  }

  def recv() = {
    pipe(Future {
      mbox.receive()
    }) to self
  }

}
