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


### Installing

The Scarl library is not integrated into an open-source project repository yet. You'll have to assemble it manually and publish it in a local environment:

```
sbt publish-local
```


## Supported patterns

### Erlag binding
[learn more](doc/binding.md)

### Supervisor
[learn more](doc/supervisor.md)


### More Information

* Here's a deep-dive study on [distributed Erlang](http://erlang.org/doc/reference_manual/distributed.html)
* Explore [binding interface](src/main/scala/org/zalando/scarl/Scarl.scala) 
* Learn [supervisor interface](src/main/scala/org/zalando/scarl/Supervisor.scala)


## How to contribute

`scarl` is Apache 2.0 licensed and accepts contributions via GitHub pull requests:

* Fork the repository on GitHub
* Read the README.md for build instructions

### commit message

The commit message helps us to write a good release note, speed-up review process. The message should address two question what changed and why. The project follows the template defined by chapter [Contributing to a Project](http://git-scm.com/book/ch5-2.html) of Git book.

>
> Short (50 chars or less) summary of changes
>
> More detailed explanatory text, if necessary. Wrap it to about 72 characters or so. In some contexts, the first line is treated as the subject of an email and the rest of the text as the body. The blank line separating the summary from the body is critical (unless you omit the body entirely); tools like rebase can get confused if you run the two together.
> 
> Further paragraphs come after blank lines.
> 
> Bullet points are okay, too
> 
> Typically a hyphen or asterisk is used for the bullet, preceded by a single space, with blank lines in between, but conventions vary here
>


## Bugs
If you experience any issues with Scarl, please let us know via [GitHub issues](https://github.com/zalando/scarl/issue). We appreciate detailed and accurate reports that help us to identity and replicate the issue. 

* **Specify** the configuration of your environment. Include which operating system you use and the versions of runtime environments. 

* **Attach** logs, screenshots and exceptions, in possible.

* **Reveal** the steps you took to reproduce the problem.


## Contacts

* Email: dmitry.kolesnikov@zalando.fi

## License

Copyright 2015 Zalando SE

Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0.

Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
