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
package org.zalando.scarl.erlang

import akka.actor.ActorRef
import akka.pattern.pipe
import com.ericsson.otp.erlang._
import org.zalando.scarl.Scarl
import Scarl.{Egress, Envelop}

import scala.concurrent.{ExecutionContext, Future}


trait Mailbox {
  implicit val exec: ExecutionContext
  val mbox: OtpMbox

  /** receive Erlang/OTP message using future,
    * schedules message to bound actor
    *
    * @param actor
    * @return
    */
  def recv(actor: ActorRef) = {
    pipe(Future {
      mbox.receive()
    }) to actor
  }

  /** Send message to communication process
    *
    * @param x
    */
  def send(x: Envelop) = x match {
    case Egress(to: OtpErlangPid, message: Any) =>
      mbox.send(to, encode(message))

    case Egress((name: String, node: String), message: Any) =>
      mbox.send(name, node, encode(message))
  }

  /** decode Erlang/OTP data structure to Scala native
    *
    * @param x
    * @return
    */
  def decode(x: OtpErlangObject): Any = x match {
    case x: OtpErlangAtom =>
      Symbol(x.atomValue())

    case x: OtpErlangBitstr =>
      x.binaryValue()

    case x: OtpErlangDouble =>
      x.doubleValue()

    case x: OtpErlangExternalFun =>
      null

    case x: OtpErlangFun =>
      null

    case x: OtpErlangList =>
      x.elements().map({case x => decode(x)}).toList

    case x: OtpErlangLong =>
      x.longValue()

// @todo: Map is new datatype, it is not supported by jar from public repo
//    case x: OtpErlangMap =>
//      null

    case x: OtpErlangPid =>
      x

    case x: OtpErlangPort =>
      x

    case x: OtpErlangRef =>
      x

    case x: OtpErlangString =>
      x.stringValue()

    case x: OtpErlangTuple =>
      x.elements().map({case x => decode(x)}) match {
        case Array(a) => (a)
        case Array(a,b) => (a,b)
        case Array(a,b,c) => (a,b,c)
        case Array(a,b,c,d) => (a,b,c,d)
        case Array(a,b,c,d,e) => (a,b,c,d,e)
        case Array(a,b,c,d,e,f) => (a,b,c,d,e,f)
      }
  }

  /** encode Scala data structure to Erlang/OTP
    *
    * @param x
    * @return
    */
  def encode(x: Any): OtpErlangObject = x match {
    case x: Symbol =>
      new OtpErlangAtom(x.name)

    case x: Double =>
      new OtpErlangDouble(x)

    case x: List[Any] =>
      new OtpErlangList(x.map{case x => encode(x)}.toArray)

    case x: Long =>
      new OtpErlangLong(x)

    case x: Int =>
      new OtpErlangLong(x)

    case x: OtpErlangPid =>
      x

    case x: OtpErlangPort =>
      x

    case x: OtpErlangRef =>
      x

    case x: String =>
      new OtpErlangString(x)

    case (a,b,c,d,e,f) =>
      new OtpErlangTuple(Array(a,b,c,d,e,f).map{case x => encode(x)})

    case (a,b,c,d,e) =>
      new OtpErlangTuple(Array(a,b,c,d,e).map{case x => encode(x)})

    case (a,b,c,d) =>
      new OtpErlangTuple(Array(a,b,c,d).map{case x => encode(x)})

    case (a,b,c) =>
      new OtpErlangTuple(Array(a,b,c).map{case x => encode(x)})

    case (a,b) =>
      new OtpErlangTuple(Array(a,b).map{case x => encode(x)})

    case (a) =>
      new OtpErlangTuple(Array(a).map{case x => encode(x)})
  }

}
