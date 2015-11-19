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
//  Erlang to Scala/Akka binding
package scarl

import akka.actor.{ActorRef, Props, ActorSystem}


object Scarl {
  val uid    = "scarl"
  val host   = "127.0.0.1"
  val cookie = "nocookie"

  /**
    * start application
    *
    * @param node
    * @param sys
    * @return
    */
  def apply(node: String)(implicit sys: ActorSystem) = {
    sys.actorOf(Props(new Supervisor(node, host, cookie)), uid)
  }

  def apply(node: String, cookie: String)(implicit sys: ActorSystem) = {
    sys.actorOf(Props(new Supervisor(node, host, cookie)), uid)
  }

  def apply(node: String, host: String, cookie: String)(implicit sys: ActorSystem) = {
    sys.actorOf(Props(new Supervisor(node, host, cookie)), uid)
  }

  /**
    * spawn new node
    *
    * @param node
    * @param cookie
    * @param sys
    * @return
    */
  def spawn(node: String)(implicit sys: ActorSystem) = {
    Supervisor.spawn(node, cookie)
  }

  def spawn(node: String, cookie: String)(implicit sys: ActorSystem) = {
    Supervisor.spawn(node, cookie)
  }

  /**
    * bind actor to external mailbox
    *
    * @param node
    * @param mbox
    * @param actor
    * @param sys
    * @return
    */
  def bind(mbox: String, actor: ActorRef)(implicit sys: ActorSystem): ActorRef = {
    NodeSup.bind("default", mbox, actor)
  }

  def bind(mbox: String, actor: Any => Any)(implicit sys: ActorSystem): ActorRef = {
    NodeSup.bind("default", mbox, actor)
  }

  def bind(node: String, mbox: String, actor: ActorRef)(implicit sys: ActorSystem): ActorRef = {
    NodeSup.bind(node, mbox, actor)
  }

  def bind(node: String, mbox: String, actor: Any => Any)(implicit sys: ActorSystem): ActorRef = {
    NodeSup.bind(node, mbox, actor)
  }
}
