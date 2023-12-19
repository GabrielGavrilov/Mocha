# Mocha-jar
A tiny flexible web framework for Java
```Java
public static void main(String[] args) extends Mocha {
  get("/", (request, response)-> {
    response.send("<h1>Hello from Mocha!</h1>");
  })

  listen(3000, ()-> {
    System.out.println("Listening on port 3000");
  });
}
```
## Installation
