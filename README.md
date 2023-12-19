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
