import com.gabrielgavrilov.mocha.Mocha;

public class Example extends Mocha
{
    public static void main(String[] args)
    {

        set("views", "src/test/java/views/");
        set("static", "src/test/java/public/");

        get("/", (req, res)->
        {
            //res.addHeader("Set-Cookie", "abc=123");
            res.render("index.html");
            System.out.println(req.header);
        });

        get("/form", (req, res)->
        {
            res.render("form.html");
        });

        post("/submit", (req, res)->
        {
            System.out.println(req.header);
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
