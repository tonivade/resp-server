# resp-server

Netty implementation of REdis Serialization Protocol, and a simple framework to implement command based protocols.

## Why?

I love REDIS, IMHO is one of the best pieces of code ever made. Is fast, small
and easy. One of the things I love of REDIS is the RESP protocol, and I think that
it would be nice to build a library to implement services using this protocol. I
have to say that this piece of code is based in another project I'm working 
[ClauDB](https://github.com/tonivade/claudb).

## What?

[RESP](http://redis.io/topics/protocol) is the protocol used by REDIS, but it
can be used to implement other client-server protocols.

It's nice because:

- Is easy
- Is fast
- Also is human readable

RESP can serialize some data types

- simple strings: `+PONG\r\n`
- errors: `-ERROR\r\n`
- integers: `:1\r\n`
- bulk strings (binary-safe): `$4\r\nPING\r\n`
- arrays: `*3\r\n:1\r\n:2\r\n:3\r\n`

What binary safe means? It means that can be what ever you want, a UTF-8 String
or compressed data, a picture, etc...

Arrays can hold any data types inside, also other arrays.

And use \r\n as delimiter, that means you can open a `telnet/netcat/nc` against
the server port and send whatever you want.

So, what provide this project in addition to the protocol implementation? Well,
it defines a framework to implement command oriented protocols.

You only need to define a set of commands and implement them.

Some commands are built-in, `ping`, `echo`, `time` and `quit`.

## What is a command?

A command is an array of bulk string in RESP, first element in array is the command, 
and the additional elements are the parameters of the command. This is how a command
looks like:

```
    *2\r\n
    $4\r\n
    ECHO\r\n
    $13\r\n
    Hello World!\r\n
```

In this sample, `ECHO` is the command and `Hello world!` is the parameter.

## How is implemented?

The protocol is implemented in Java8, using asynchronous IO (netty), and using the
reactive programming paradigm (rxjava). What that means? It means that is single
thread, every request is managed inside the same thread, so there's no concurrency
issues at all, the same way as REDIS works.

## How can I use it?

It's very easy, you only need 2 lines of code to start the server

```java
    RespServer server = RespServer.builder()
      .host("localhost").port(12345).commands(new CommandSuite()).build();
    server.start();
```

CommandSuite is the default commands suite but you can extend and add your own commands,
well, that's the point :)

What a command looks like?

```java
    @Command("ping")
    public class PingCommand implements RespCommand {
        @Override
        public RedisToken execute(Request request) {
            return RedisToken.status("PONG");
        }
    }
```
    
A command must implement the interface `RespCommand`. This interface only defines
the method `execute`, who receives a `Request` object and returns a `RedisToken`.

You can get the parameter of the command like this

```java
    SafeString param0 = request.getParam(0);
```
    
Every parameter is a `SafeString`, and what the hell is a `SafeString`? Previously,
we said that RESP is binary-safe, so, it means that you can receive anything. `SafeString`
wraps the bytes received, but, don't worry, it's not going to be a problem, trust me.

And you can response to a request this way:

```java
    return RedisToken.status("PONG");
```
    
You have similar methods like `array`, `integer`, `string`, `error`...

Annotations are used to define some metadata to the commands, `@Command` annotation
defines the command name, also there's another annotation, `@ParamLength` to define
the number of the parameter accepted for this command

```java
    @Command("echo")
    @ParamLength(1)
    public class EchoCommand implements RespCommand {
        @Override
        public RedisToken execute(Request request) {
            return RedisToken.string(request.getParam(0));
        }
    }
```
    
If the number of parameters is less than the especified value, the command
is rejected with an error.

## Maven

    <dependency>
        <groupId>com.github.tonivade</groupId>
        <artifactId>resp-server</artifactId>
        <version>0.16.0</version>
    </dependency>

## Gradle

    compile 'com.github.tonivade:resp-server:0.16.0'
    
## Stargazers over time

[![Stargazers over time](https://starchart.cc/tonivade/resp-server.svg)](https://starchart.cc/tonivade/resp-server)

## Continuous Integration

[![Build Status](https://api.travis-ci.org/tonivade/resp-server.svg?branch=master)](https://travis-ci.org/tonivade/resp-server)
[![Codacy Badge](https://api.codacy.com/project/badge/Grade/47b2b3213b7248eca911e4783ed6d031)](https://www.codacy.com/app/tonivade/resp-server?utm_source=github.com&amp;utm_medium=referral&amp;utm_content=tonivade/resp-server&amp;utm_campaign=Badge_Grade)
[![Codacy Coverage](https://api.codacy.com/project/badge/Coverage/47b2b3213b7248eca911e4783ed6d031)](https://www.codacy.com/app/tonivade/resp-server?utm_source=github.com&utm_medium=referral&utm_content=tonivade/resp-server&utm_campaign=Badge_Coverage)

## LICENSE

This project is released under MIT License
