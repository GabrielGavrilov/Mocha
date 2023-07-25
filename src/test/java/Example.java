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

        get("/form", (req, res)-> {
            res.render("src/test/java/views/form.html");
        });

        get("/user/{name}", (req, res)-> {
           res.send("<p>Hello, " + req.parameters.get("name") + "</p>");
        });

        /**
         * POST ROUTES
         */

        post("/submit", (req, res)-> {
            res.send("<p>Gotten: " + req.payload + "</p>");
        });

        /**
         * SERVER LISTENER
         */

        listen(3000, ()-> {
           System.out.println("Listening on port 3000");
        });

    }
}
