# scarl

> 
> "The historical records will claim that the Rebel Alliance was born on Corellia.
> That is a documented fact. But the truth is this: The Rebel Alliance was born 
> somewhere in the Scarl system aboard Darth Vader's personal starship, the Executor."
>   - http://starwars.wikia.com/wiki/Scarl_system
> 

`Scarl` is the binding layer between Erlang and Scala/Akka universes. The library implements message bridge using Erlang distribution protocol and it is based on [jinterface](http://www.erlang.org/doc/apps/jinterface/jinterface_users_guide.html) for details.

The project is targeted for Scala and Erlang developers who builds a distributed systems using Actor techniques provided by these environments.

The library assumes that distributed system is built from nodes. Each node maintains peer-to-peer 
connection to neighbor peers. The chapter [Distributed Erlang](http://erlang.org/doc/reference_manual/distributed.html) provides comprehensive description on the network topology. The `scarl` application is spawned with actor system using `scarl.Scarl(...)` primitive. It spawns automatically default node with given name. The message recipients are identified by registered names. The name is unique within the node
and associated to implicit mailbox is used to receive messages. The library defines a bind primitives
`scarl.Scarl.bind(...)` to associate either Akka actor of lambda expression with mailbox. 



## inspiration

The development of distributed system is a complex subject. Usually, it require variety of software management layers that provides concurrency, job scheduling, request marshaling, message routing, membership, failure detection and recovery. Erlang/OTP is an indispensable technology to solve these problem. Its run-time, distribution, supervisor and fault-tolerance frameworks gives all necessary means to address listed problem. This library allows to reuse distribution assets proven by time in Scala projects.



## getting started


### getting scarl

The project is Scala library, its latest version is available from `master` branch.  All development, including new features and bug fixes, take place on master branch using forking and pull requests as described in [contribution guideline](doc/contribution.md). The usage and development requires [Scala](http://www.scala-lang.org) and [sbt](http://www.scala-sbt.org). 


### running scarl

You can experiment `scarl` and message passing between Scala to Erlang actors in development console. It requires [Erlang/OTP](http://www.erlang.org/downloads) version 18.0 or later.

Run the development console
```
sbt console
``` 

Let's spawn two simples actors in the console
```scala
   implicit val sys = akka.actor.ActorSystem("universe")
   
   //
   // spawn scarl application and creates default node scala@127.0.0.1
   scarl.Scarl("scala", scarl.Listener(host="127.0.0.1", cookie="nocookie"))
   
   //
   // binds lambda expression to mailbox 'inbox
   // Erlang can send message using {inbox, 'scala@127.0.0.1'} ! hello. 
    scarl.Scarl.bind("inbox", (x: Any) => {println(x); None})
   
   //
   // ping - pong example
   // Erlang can send message using {ping, 'scala@127.0.0.1'} ! {self(), "text"}.
   scarl.Scarl.bind("ping", 
      (x: Any) => x match {
         case (ref: com.ericsson.otp.erlang.OtpErlangPid, text: String) =>
            scarl.Scarl.envelop(ref, text)
      }
   )
```

Open a new terminal and start Erlang console
```
erl -name erlang@127.0.0.1 -setcookie nocookie
```

Let's send a one-way message to Scala universe.
```
{inbox, 'scala@127.0.0.1'} ! hello.
```
As the result, you will see hello message at Scala console  
```
scala> 'hello
```

Let's send request / response message to Scala universe.
```
{ping, 'scala@127.0.0.1'} ! {self(), "hello"}.
```
Typing the `flush().` command at Erlang console, you will see that message is send back to Erlang actor
```
(erlang@127.0.0.1)2> flush().
Shell got "hello"
ok
```


### deploying scarl

The library is not integrated yet into open-source project repository. It requires a manual assembly and publish at local environment.
```
sbt publish-local
```

### continue to...

* Deep dive study on [distributed Erlang](http://erlang.org/doc/reference_manual/distributed.html)
* Explore [native interface](src/main/scala/scarl/Scarl.scala) 



## contributing
See [contribution guideline](doc/contribution.md) for details on PR submission.



## bugs
See [bug reporting](doc/bugs.md) for guidelines on raising issues. 



## contacts

* email: dmitry.kolesnikov@zalando.fi
* bugs: [here](https://github.com/zalando/scarl/issues) 


# License

Copyright 2015 Zalando SE

Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0.

Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
