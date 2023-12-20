# Mocha-jar
A tiny flexible web framework for Java
```Java
public static void main(String[] args) extends Mocha {
  get("/", (request, response)-> {
    response.initializeHeader("200 OK", "text/html")
    response.send("<h1>Hello from Mocha!</h1>");
  })

  listen(3000, ()-> {
    System.out.println("Listening on port 3000");
  });
}
```
## Features
- GET routes
- POST routes
- PUT routes
- DELETE routes
- Route parameters
- File rendering (html, json, etc)
- Static file rendering (css, pngs, etc)
- Cookies
- HTTP body payloads

## Installation
See "Assets" in <a href="https://github.com/GabrielGavrilov/mocha-jar/releases/tag/alpha">releases section.</a>

## Documentation
### Setting Up
First install the ``mocha.jar`` file from the releases section.

Once installed, start using Mocha by extending main method with the ``Mocha`` class. 

### Starting the Server
To start the server, call the ``listen()`` function from the Mocha class you've extended. 

Mocha currently has two listen functions; a listen function that does not support a host ip, and a listen function that does support the host id. 

Depending on which listen function you choose, you must provide a port number, a host ip address, and a runnable callback method that gets called before the server starts running. 

``` Java
listen(3000, ()-> {
  System.out.println("Listening on port 3000");
})
```
### GET Routes
To create a GET route, you must provide the route url and a BiConsumer that accepts a MochaRequest and a MochaResponse class. Inside the BiConsumer, you may provide what actions you want the route to perform. 

```Java
get("/", (request, response)-> {
  response.initializeHeader("200 OK", "text/html");
  response.send("<p>Hello, World!</p>");
})
```
### POST Routes
**POST Routes must require some form of body data to be sent in the request, otherwise Mocha will not perform the proper response necessary.**

POST routes can be created just like GET routes.

```Java
post("/", (request, response)-> {
  response.initializeHeader("200 OK", "text/html");
  response.send("<p>Hello, World!</p>");
})
```
### PUT Routes
**PUT Routes must require some form of body data to be sent in the request, otherwise Mocha will not perform the proper response necessary.**

PUT routes can be created just like GET routes.

```Java
put("/", (request, response)-> {
  response.initializeHeader("200 OK", "text/html");
  response.send("<p>Hello, World!</p>");
})
```
### DELETE Routes
**DELETE Routes must require some form of body data to be sent in the request, otherwise Mocha will not perform the proper response necessary.**

DELETE routes can be created just like GET routes.

```Java
delete("/", (request, response)-> {
  response.initializeHeader("200 OK", "text/html");
  response.send("<p>Hello, World!</p>");
})
```
