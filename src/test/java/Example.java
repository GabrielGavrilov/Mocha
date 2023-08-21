import com.gabrielgavrilov.mocha.Mocha;

public class Example extends Mocha
{
    public static void main(String[] args)
    {
        set("views", "src/test/java/views/");
        set("static", "src/test/java/public/");

        get("/", (req, res)->
        {
            res.initializeHeader("200 OK", "text/html");
            res.setCookie("name", "Gabriel");
            res.render("index.html");
        });

        get("/form", (req, res)-> {
            res.initializeHeader("200 OK", "text/html");
            res.render("form.html");
        });

        get("*", (req, res)-> {
            res.initializeHeader("200 OK", "text/html");
            res.send("<h1>404</h1");
        });

        /**
         * SERVER LISTENER
         */

        listen(3000, ()->
        {
           System.out.println("Listening on port 3000");
        });

    }
}
