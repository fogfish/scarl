# scarl

> 
> "The historical records will claim that the Rebel Alliance was born on Corellia.
> That is a documented fact. But the truth is this: The Rebel Alliance was born 
> somewhere in the Scarl system aboard Darth Vader's personal starship, the Executor."
>   - http://starwars.wikia.com/wiki/Scarl_system
> 

`Scarl` is the binding layer between Erlang and Scala/Akka universes. 
The library implements message bridge using Erlang distribution protocol and build using
Erlang/OTP [jinterface](http://www.erlang.org/doc/apps/jinterface/jinterface_users_guide.html).
The library provides a message bridge between Erlang and Akka processes.

## Concept

Nodes builds a physical communication layer within the cluster. Each node maintains peer-to-peer 
connection to neighbour nodes. The chapter [Distributed Erlang](http://erlang.org/doc/reference_manual/distributed.html)
provides comprehensive description on the network topology.

The `scarl` application is spawned with actor system using `scarl.Scarl(...)` primitive.
It spawns automatically default node with given name.
 
The message recipients are identified by registered names. The name is unique within the node
and associated to implicit mailbox is used to receive messages. The library defines a bind primitives
`scarl.Scarl.bind(...)` to associate either Akka actor of lambda expression with mailbox. 
 

## Usage

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
