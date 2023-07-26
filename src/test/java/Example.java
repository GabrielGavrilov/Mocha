import com.gabrielgavrilov.mocha.Mocha;

public class Example extends Mocha
{
    public static void main(String[] args)
    {

        set("views", "src/test/java/views/");
        set("static", "src/test/java/public/");

        /**
         * GET ROUTES
         */

        get("/", (req, res)->
        {
            res.render("index.html");
        });

        get("/form", (req, res)->
        {
            res.render("form.html");
        });

        get("/user/{name}", (req, res)->
        {
           res.send("<p>Hello, " + req.parameter.get("name") + "</p>");
        });

        /**
         * POST ROUTES
         */

        post("/submit", (req, res)->
        {
            res.send("<p>Hello, " + req.payload.get("firstName") + "</p>");
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
