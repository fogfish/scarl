# Scarl

>
> "The historical records will claim that the Rebel Alliance was born on Corellia.
> That is a documented fact. But the truth is this: The Rebel Alliance was born 
> somewhere in the Scarl system aboard Darth Vader's personal starship, the Executor."
>   - [Star Wars' Scarl system](http://starwars.wikia.com/wiki/Scarl_system)
> 

The Scarl library acts as a binding layer to connect the Erlang and Scala/Akka universes. It's made for Scala and Erlang developers who build distributed systems using the Actors provided by these languages. Scarl uses Erlang's distribution protocol to implement message bridging and is based on [jinterface](http://www.erlang.org/doc/apps/jinterface/jinterface_users_guide.html).

Scarl assumes that your distributed system is built from nodes. Each node maintains a peer-to-peer connection with neighboring peers. The chapter [Distributed Erlang](http://erlang.org/doc/reference_manual/distributed.html) provides a comprehensive description on network topology. 

You can use the `scarl.Scarl(...)` primitive to spawn the `scarl` application with the actor system, and/or to automatically spawn a default node with a given name. Registered names identify message recipients. These registered names are unique within the node and associated to the implicit mailbox used to receive messages. Scarl defines a bind primitives, `scarl.Scarl.bind(...)`, to associate either Akka actor of a Lambda expression with the implicit mailbox. 


## Inspiration

Developing distributed systems usually requires  software management layers that provide concurrency, job scheduling, request marshaling, message routing, membership, failure detection and recovery. Erlang/OTP is an indispensable technology to solve these problem. Its run-time, distribution, supervisor and fault-tolerance frameworks gives all necessary means to address listed problem. This library allows to reuse distribution assets proven by time in Scala projects.

## Getting Started
###Requirements
To use and develop Scarl, you need:
- [Scala](http://www.scala-lang.org)
- [sbt](http://www.scala-sbt.org). 

### Getting Scarl

The project is a Scala library, its latest version is available from `master` branch.  All development, including new features and bug fixes, take place on master branch using forking and pull requests as described in [contribution guideline](doc/contribution.md). 


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
