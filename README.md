# scarl

> 
> "The historical records will claim that the Rebel Alliance was born on Corellia.
> That is a documented fact. But the truth is this: The Rebel Alliance was born 
> somewhere in the Scarl system aboard Darth Vader's personal starship, the Executor."
>   - http://starwars.wikia.com/wiki/Scarl_system
> 

`Scarl` is the binding layer between Erlang and Scala/Akka universes. The library implements message bridge using Erlang distribution protocol and it is based on [jinterface](http://www.erlang.org/doc/apps/jinterface/jinterface_users_guide.html).

The project is targeted for Scala and Erlang developers who builds a distributed systems using Actor techniques provided by these environments.

The library assumes that distributed system is built from nodes. Each node maintains peer-to-peer 
connection to neighbor peers. The chapter [Distributed Erlang](http://erlang.org/doc/reference_manual/distributed.html) provides comprehensive description on the network topology. The `scarl` application is spawned with actor system using `scarl.Scarl(...)` primitive. It spawns automatically default node with given name. The message recipients are identified by registered names. The name is unique within the node
and associated to implicit mailbox is used to receive messages. The library defines a bind primitives
`scarl.Scarl.bind(...)` to associate either Akka actor of lambda expression with mailbox. 



## Inspiration

The development of distributed system is a complex subject. Usually, it require variety of software management layers that provides concurrency, job scheduling, request marshaling, message routing, membership, failure detection and recovery. Erlang/OTP is an indispensable technology to solve these problem. Its run-time, distribution, supervisor and fault-tolerance frameworks gives all necessary means to address listed problem. This library allows to reuse distribution assets proven by time in Scala projects.



## Getting started


### getting scarl

The project is Scala library, its latest version is available from `master` branch.  All development, including new features and bug fixes, take place on master branch using forking and pull requests as described in [contribution guideline](doc/contribution.md). The usage and development requires [Scala](http://www.scala-lang.org) and [sbt](http://www.scala-sbt.org). 


### installation

The library is not integrated yet into open-source project repository. It requires a manual assembly and publish at local environment.
```
sbt publish-local
```


### continue to...

* Deep dive study on [distributed Erlang](http://erlang.org/doc/reference_manual/distributed.html)
* Explore [binding interface](src/main/scala/org/zalando/scarl/Scarl.scala) 
* Learn [supervisor interface](src/main/scala/org/zalando/scarl/Supervisor.scala)


## Supported patterns

### Erlag binding
[learn more](doc/binding.md)

### Supervisor
[learn more](doc/supervisor.md)


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

If you experience any issue with the project, please let us know using [github issue](https://github.com/zalando/scarl/issue). We appreciate detailed and accurate reports that helps us to identity and replicate the issue. 

* **specify** configuration of your environment, what operating system, version of runtime environments are used. 

* **attach** possible logs, screen shots or exception experienced by you.

* **reveal** the steps to reproduce the problem.


## Contacts

* email: dmitry.kolesnikov@zalando.fi
* bugs: [here](https://github.com/zalando/scarl/issues) 


# License

Copyright 2015 Zalando SE

Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0.

Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
