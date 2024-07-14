# Mocha-jar
A tiny flexible web framework for Java

## Getting Started:

1. Download the framework located at the <a href="https://github.com/GabrielGavrilov/mocha-jar/releases">releases page.</a>

2. Start coding:
```Java
public class Server extends Mocha {
  public static void main(String[] args) {
    get("/", (request, response)-> {
      response.initializeHeader("200 OK", "text/html")
      response.send("<h1>Hello from Mocha!</h1>");
    })
  
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

