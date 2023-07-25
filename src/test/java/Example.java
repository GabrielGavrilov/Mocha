import com.gabrielgavrilov.mocha.Mocha;

public class Example extends Mocha
{
    public static void main(String[] args)
    {

        /**
         * GET ROUTES
         */

        get("/", (req, res)-> {
            res.render("src/test/java/views/index.html");
        });

        get("/about", (req, res)-> {
            res.render("src/test/java/views/about.html");
        });

        get("/user/{user}", (req, res)-> {
            res.send("<p>Hello, " + req.parameters.get("user") + "!</p>");
        });

        /**
         * STATIC FILES
         */

        get("/background.css", (req, res)-> {
           res.render("src/test/java/views/background.css", "text/css");
        });

        get("/foreground.css", (req, res)-> {
           res.render("src/test/java/views/foreground.css", "text/css");
        });

        /**
         * SERVER LISTENER
         */

        listen(3000, ()-> {
           System.out.println("Listening on port 3000");
        });

    }
}
