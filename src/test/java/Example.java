import com.gabrielgavrilov.mocha.Mocha;

public class Example extends Mocha
{
    public static void main(String[] args)
    {
        set("views", "src/test/java/views/");
        set("static", "src/test/java/public/");

        get("/", (req, res)->
        {
            res.render("index.html");
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
