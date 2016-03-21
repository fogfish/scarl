# Scarl

>
> "The historical records will claim that the Rebel Alliance was born on Corellia.
> That is a documented fact. But the truth is this: The Rebel Alliance was born 
> somewhere in the Scarl system aboard Darth Vader's personal starship, the Executor."
>   - [Star Wars' Scarl system](http://starwars.wikia.com/wiki/Scarl_system)
> 

Scarl is a Scala library that acts as a binding layer to connect the Erlang and Scala/Akka universes. It's made for Scala and Erlang developers who build distributed systems using the Actors provided by these languages. Scarl uses Erlang's distribution protocol to implement message bridging and is based on [Jinterface](http://www.erlang.org/doc/apps/jinterface/jinterface_users_guide.html). It enables you to reuse distribution assets proven by time in Scala projects.

Scarl assumes that your distributed system is built from nodes. Each node maintains a peer-to-peer connection with neighboring peers. The Jinterface chapter [Distributed Erlang](http://erlang.org/doc/reference_manual/distributed.html) provides a comprehensive description on network topology. 

You can use the `scarl.Scarl(...)` primitive to spawn the `scarl` application with the actor system, and/or to automatically spawn a default node with a given name. Registered names identify message recipients. These registered names are unique within the node and associated to the implicit mailbox used to receive messages. 

Scarl defines a bind primitives, `scarl.Scarl.bind(...)`, to associate either Akka actor of a Lambda expression with the implicit mailbox. 

## Inspiration

Developing distributed systems usually requires a lot of work — i.e., building software management layers that provide concurrency, job scheduling, request marshaling, message routing, membership, failure detection and recovery. [Erlang/OTP](http://erlang.org/doc/) is an indispensable technology for simplifying this work, thanks to its many frameworks — for runtime, distribution, supervision and fault-tolerance, for examples. 

## Getting Started
###Requirements
To use and develop Scarl, you need:
- [Scala](http://www.scala-lang.org)
- [sbt](http://www.scala-sbt.org) 

### Getting Scarl

The latest version of Scarl is available at its `master` branch.  All development, including new features and bug fixes, take place on the master branch using forking and pull requests as described in [these contribution guidelines](doc/contribution.md). 

### Running Scarl

You can experiment with `scarl` and message passing between Scala to Erlang actors in your development console. This requires downloading [Erlang/OTP](http://www.erlang.org/downloads) version 18.0 or later.

Run the development console:

```
sbt console
``` 

Let's spawn two simple actors in the console:

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

Open a new terminal, and start the Erlang console:

```
erl -name erlang@127.0.0.1 -setcookie nocookie
```

Let's send a one-way message to the Scala universe:

```
{inbox, 'scala@127.0.0.1'} ! hello.
```
In the result, you will see a "hello" message in the Scala console:

```
scala> 'hello
```

Let's send a request/response message to the Scala universe:

```
{ping, 'scala@127.0.0.1'} ! {self(), "hello"}.
```
Typing the `flush().` command in the Erlang console, you will see that the message is send back to the Erlang actor:

```
(erlang@127.0.0.1)2> flush().
Shell got "hello"
ok
```

### Deploying Scarl

The Scarl library is not integrated into an open-source project repository yet. You'll have to assemble it manually and publish it in a local environment:

```
sbt publish-local
```

### More Information

* Here's a deep-dive study on [distributed Erlang](http://erlang.org/doc/reference_manual/distributed.html)
* Explore [native interfaces](src/main/scala/scarl/Scarl.scala) 

## How to Contribute
See the [contribution guidelines](doc/contribution.md) for details on submitting pull requests.

## Bugs
If you detect a bug, please bring it to our attention via [issues](https://github.com/zalando/scarl/issues). Please make your report detailed and accurate so that we can identify and replicate the issues you experience:
- specify the configuration of your environment, including which operating system you're using and the versions of your runtime environments
- attach logs, screen shots and/or exceptions if possible
- briefly summarize the steps you took to resolve or reproduce the problem

## Contacts

* Email: dmitry.kolesnikov@zalando.fi

## License

Copyright 2015 Zalando SE

Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0.

Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
