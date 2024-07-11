package testing;

import com.gabrielgavrilov.mocha.Mocha;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.util.HashMap;

public class Main extends Mocha {
    public static void main(String[] args) {

        post("/", (request, response)-> {
            response.initializeHeader("200 OK", "application/json");

            System.out.println(request.payload.get("msg"));

            response.send("OK");
        });

        listen(3000, ()-> {
            System.out.println("Running on port 3000...");
        });

    }
}
