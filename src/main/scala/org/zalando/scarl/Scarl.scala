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
package org.zalando.scarl

import akka.actor.{ActorRef, Props, ActorSystem}
import com.ericsson.otp.erlang.OtpErlangPid


/** distribution listener interface specification
  *
  */
case class Listener(
  /** hostname or ip address to bind the distribution listener */
  host: String = "127.0.0.1",

  /** a magic cookie as defined by Erlang distribution */
  cookie: String = "nocookie"
)


object Scarl {
  /** application id */
  val uid  = "scarl"


  /** the Scala system can communicate with any remote Erlang process using
    * either direct process reference (pid) or abstarct name (aka akka path)
    * The direct reference is type of OtpErlangPid, the abstract name is
    * tuple of (String, String). The wired data are tuples, and the tuple structure
    * is application protocol outside of scarl implementation (it is a messanger)
    *
    * The library uses concept of Envelope to express communication intent.
    * Envelop contains the destination address and message. The union type is required to
    * model the message destination [OtpErlangPid or (String, String)]
    * The Curry-Howard isomorphism is elegant solution on union type
    * https://issues.scala-lang.org/browse/SI-3749
    * http://milessabin.com/blog/2011/06/09/scala-union-types-curry-howard/
    */
  type \[A]  = A => Nothing
  type v[A, B] = \[\[A] with \[B]]
  type \\[A] = \[\[A]]
  type u[A, B] = { type E[X] = \\[X] <:< (A v B) }

  abstract class Envelop
  case class Egress(to: Any, message: Any) extends Envelop

  /** start scarl application, create default node with give name.
    * it returns reference to root supervisor
    *
    * @param node default node name
    * @param dist distribution listener interface specification
    * @return
    */
  def apply(node: String, dist: Listener = Listener())(implicit sys: ActorSystem): ActorRef = {
    sys.actorOf(Props(new Supervisor(node, dist)), uid)
  }


  /** spawn new node on existing interface
    *
    * @param node locally unique node name
    * @param sys implicit reference to actor system
    * @return
    */
  def spawn(node: String)(implicit sys: ActorSystem) = {
    Supervisor.spawn(node)
  }


  /** bind actor to external mailbox on default node
    *
    * @param mbox unique mailbox name, the name is used as process address to send a message
    * @param actor either ref to existed actor of lambda expression
    * @param sys implicit reference to actor system
    * @return
    */
  def bind(mbox: String, actor: ActorRef)(implicit sys: ActorSystem): ActorRef = {
    NodeSup.bind("default", mbox, actor)
  }

  def bind(mbox: String, actor: Any => Option[Envelop])(implicit sys: ActorSystem): ActorRef = {
    NodeSup.bind("default", mbox, actor)
  }

  /** bind actor to external mailbox on defined node
    *
    * @param node node name
    * @param mbox mbox unique mailbox name
    * @param actor either ref to existed actor of lambda expression
    * @param sys implicit reference to actor system
    * @return
    */
  def bind(node: String, mbox: String, actor: ActorRef)(implicit sys: ActorSystem): ActorRef = {
    NodeSup.bind(node, mbox, actor)
  }

  def bind(node: String, mbox: String, actor: Any => Option[Envelop])(implicit sys: ActorSystem): ActorRef = {
    NodeSup.bind(node, mbox, actor)
  }

  /** build envelop
    *
    * @param to destination address
    * @param message communication message
    * @tparam T either OptErlangPid or (String, String) process identity
    * @return
    */
  def envelop[T: (OtpErlangPid u (String, String))#E](to: T, message: Any) = {
    Some(Egress(to, message))
  }

}
