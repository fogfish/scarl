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
import scala.concurrent.Await
import scala.concurrent.duration._
import akka.pattern.ask

class SupervisorSpec extends UnitSpec {
  import org.zalando.scarl.Supervisor.SystemSupervisor
  implicit val t: akka.util.Timeout = 5 seconds

  "Supervisor" should "spawn actors" in {
    implicit val sys = ActorSystem("test-sup")
    sys.supervisor(Supervisor.Supervisor("root", Props[SupA]))

    Supervisor.resolve(sys.actorSelection("/user/root/a"))
    Supervisor.resolve(sys.actorSelection("/user/root/b"))

    Await.result(sys.terminate(), Duration.Inf)
  }

  "Supervisor" should "spawn dynamic actors" in {
    implicit val sys = ActorSystem("test-sup")
    val sup = sys.supervisor(Supervisor.Supervisor("root", Props[SupC]))

    Supervisor.spawn(sup, Supervisor.Worker("a", Props[WorkerA]))
    Supervisor.resolve(sys.actorSelection("/user/root/a"))

    Await.result(sys.terminate(), Duration.Inf)
  }


  "Supervisor" should "re-spawn actors" in {
    implicit val sys = ActorSystem("test-sup")
    sys.supervisor(Supervisor.Supervisor("root", Props[SupB]))

    val a = Supervisor.resolve(sys.actorSelection("/user/root/a")).get
    a ! 'fail
    Await.result(a ? Identify("") , Duration.Inf) match {
      case ActorIdentity(_, Some(x)) =>
        x shouldBe a
    }

    Await.result(sys.terminate(), Duration.Inf)
  }


  "Supervisor" should "re-spawn dynamic actors" in {
    implicit val sys = ActorSystem("test-sup")
    val sup = sys.supervisor(Supervisor.Supervisor("root", Props[SupC]))

    val a = Await.result(Supervisor.spawn(sup, Supervisor.Worker("a", Props[WorkerA])), Duration.Inf)
    a ! 'fail
    Await.result(a ? Identify(""), Duration.Inf) match {
      case ActorIdentity(_, Some(x)) =>
        x shouldBe a
    }

    Await.result(sys.terminate(), Duration.Inf)
  }



  "Supervisor" should "terminate actor system" in {
    implicit val sys = ActorSystem("test-sup")
    sys.supervisor(Supervisor.Supervisor("root", Props[SupB]))

    val a = Supervisor.resolve(sys.actorSelection("/user/root/a")).get
    a ! 'fail
    a ! 'fail
    a ! 'fail

    Await.result(sys.whenTerminated, Duration.Inf)
  }


}

//
// simple fail-safe
class SupA extends RootSupervisor {
    override
    val supervisorStrategy = strategyOneForOne(2, 1 minute)

    def init = Seq(
      Supervisor.Worker("a", Props[WorkerA]),
      Supervisor.Worker("b", Props[WorkerA])
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

  def init = Seq(
    Supervisor.Worker("a", Props[WorkerB]),
    Supervisor.Worker("b", Props[WorkerB])
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

  def init = Seq()
}

