//
//  Copyright 2016 Zalando SE
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

import akka.actor._
import scala.concurrent.{Future, Await}
import scala.concurrent.duration._
import akka.pattern.ask
import org.zalando.scarl.Supervisor._

import scala.util.Success

class SupervisorSpec extends UnitSpec {
  import org.zalando.scarl.ScarlSupervisor

  implicit val t: akka.util.Timeout = 5.seconds

  "Supervisor" should "spawn actors" in {
    implicit val sys = ActorSystem("test-sup")
    implicit val fec = sys.dispatcher

    val sup = sys.rootSupervisor(Specs("root", Props[SupA]))
    await(sup)

    await(sys.actorSelection("/user/root/a").resolve())
    await(sys.actorSelection("/user/root/b").resolve())

    await(sys.terminate())
  }

  "Supervisor" should "spawn dynamic actors" in {
    implicit val sys = ActorSystem("test-sup")
    implicit val fec = sys.dispatcher

    val sup = sys.rootSupervisor(Specs("root", Props[SupC]))

    Supervisor.actorOf(sup, Specs("a", Props[WorkerA]))
    await(sys.actorSelection("/user/root/a").resolve())

    await(sys.terminate())
  }


  "Supervisor" should "re-spawn actors" in {
    implicit val sys = ActorSystem("test-sup")
    implicit val fec = sys.dispatcher

    sys.rootSupervisor(Specs("root", Props[SupB]))

    val a = await(sys.actorSelection("/user/root/a").resolve())
    val f = sys.actorSelection("/user/root/a").resolve()
        .map {failure _}
        .flatMap {_ ? Identify("")}

    await(f) match {
      case ActorIdentity(_, Some(x)) =>
        x shouldBe a
    }

    await(sys.terminate())
  }


  "Supervisor" should "re-spawn dynamic actors" in {
    implicit val sys = ActorSystem("test-sup")
    implicit val fec = sys.dispatcher

    val sup = sys.rootSupervisor(Specs("root", Props[SupC]))
    val a   = Supervisor.actorOf(sup, Specs("a", Props[WorkerA]))

    val f = sys.actorSelection("/user/root/a").resolve()
      .map {failure _}
      .flatMap {_ ? Identify("")}

    await(f) match {
      case ActorIdentity(_, Some(x)) =>
        x shouldBe await(a)
    }

    await(sys.terminate())
  }



  "Supervisor" should "terminate actor system" in {
    implicit val sys = ActorSystem("test-sup")
    implicit val fec = sys.dispatcher

    val sup = sys.rootSupervisor(Specs("root", Props[SupB]))
    await( sup )

    val x = sys.actorSelection("/user/root/a").resolve()
      .map {failure _}
      .map {failure _}
      .map {failure _}
      .flatMap {
        _ => sys.actorSelection("/user/root/a").resolve()
      }
    an[Throwable] should be thrownBy await(x)

    await(sys.whenTerminated)
  }

  private def await[T](f: Future[T]): T = Await.result(f, Duration.Inf)

  private def failure(x: ActorRef) = {
    x ! 'fail
    x
  }
}

//
// simple fail-safe
class SupA extends RootSupervisor {
    override
    val supervisorStrategy = strategyOneForOne(2, 1 minute)

    def specs = Seq(
      Specs("a", Props[WorkerA]),
      Specs("b", Props[WorkerA])
    )
}

class WorkerA extends Actor {
  def receive = {
    case _ =>
  }
}


//
// simple fail-it
class SupB extends RootSupervisor {
  override
  val supervisorStrategy = strategyOneForOne(2, 1 minute)

  def specs = Seq(
    Specs("a", Props[WorkerB]),
    Specs("b", Props[WorkerB])
  )
}

class WorkerB extends Actor {
  def receive = {
    case _ =>
      throw new Exception("fail it.")
  }
}


//
// empty supervisor
class SupC extends RootSupervisor {
  override
  val supervisorStrategy = strategyOneForOne(2, 1 minute)

  def specs = Seq()
}

