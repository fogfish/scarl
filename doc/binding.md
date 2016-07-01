# Erlang to Akka binding

You can experiment with `scarl` and message passing between Scala to Erlang actors in your development console. This requires downloading [Erlang/OTP](http://www.erlang.org/downloads) version 18.0 or later.

Run the development console:

```
sbt console
``` 

Let's spawn two simple actors in the console:

```scala
import akka.actor._
import org.zalando.scarl._

implicit val sys = ActorSystem("universe")
   
//
// spawn scarl application and creates default node scala@127.0.0.1
Scarl("scala", Listener(host="127.0.0.1", cookie="nocookie"))

//
// binds lambda expression to mailbox 'inbox
// Erlang can send message using {inbox, 'scala@127.0.0.1'} ! hello. 
Scarl.bind("inbox", (x: Any) => {println(x); None})

//
// ping - pong example
// Erlang can send message using {ping, 'scala@127.0.0.1'} ! {self(), "text"}.
Scarl.bind("ping", 
   (x: Any) => x match {
      case (ref: com.ericsson.otp.erlang.OtpErlangPid, text: String) =>
         Scarl.envelop(ref, text)
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

