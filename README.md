# Mocha-jar
A tiny flexible web framework for Java

## Getting Started:

1. Download the framework located at the <a href="https://github.com/GabrielGavrilov/mocha-jar/releases">releases page.</a>

2. Start coding:
```Java
public class Server extends Mocha {
  public static void main(String[] args) {
    get("/", (request, response)-> {
      response.initializeHeader("200 OK", "text/html");
      response.send("<h1>Hello from Mocha!</h1>");
    });
  
    listen(3000, ()-> {
      System.out.println("Listening on port 3000...");
    });
  }
}
```
3. Run and view:
```
http://localhost:3000
```

## Routes

A Mocha route is made up of two components: 
- A path
- A callback method

Mocha supports the current routes:

```java
get("/", (request, response)-> {
  // Show something
});

post("/", (request, response)-> {
  // Create something
});

put("/", (request, response)-> {
  // Update something
});

delete("/", (request, response)-> {
  // Delete something
});
```

Routes also support parameters, and can be accessed by calling the ``parameter`` hashmap. 

```java
get("/greet/{name}", (request, response)-> {
  response.initializeHeader("200 OK", "text/html");
  String name = request.parameter.get("name");
  response.send("Hello, " + name + "!");
})
```

## Request

The Request class has the following information and functionality:
```java
request.payload()        // A MochaPayload class used to retrieve body payload information
request.parameter()      // A hashmap used to map a parameter's value by name
request.cookie()         // A hashmap used to map a cookie's value by name
request.header           // requested HTTP header
```

## Response
The Response class has the following information and functionality:
```java
response.initializeHeader()    // Used to intiialize the status and content-type for the response
response.addHeader()           // Appends a custom header to the response
response.status()              // Sets the HTTP status
response.contentType()         // Sets the content-type
response.cookie()              // Sets a cookie
response.send()                // Sends data to the response
response.render()              // Renders a given file
```

## Payloads

Mocha expects a body payload for the following routes:
- POST
- UPDATE
- DELETE

As of right now, Mocha only accepts ``raw`` and ``JSON`` payloads. Payloads can be accessed by using the Request class:
```java
request.payload.get("foo")
```

