package Testing;

import com.gabrielgavrilov.mocha.Mocha;
import org.json.CDL;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;

public class Main extends Mocha
{
    public static void main(String[] args)
    {
        set("views", "src/main/java/Testing/facts/");

        get("/all/{animal}", (req, res) -> {
            res.initializeHeader("200 OK", "text/json");
            String animal = req.parameter.get("animal");

            if(animal.equals("cat"))
            {
                res.render("cats.csv");
            }

            else if(animal.equals("dog"))
            {
                res.render("dogs.csv");
            }
        });

        post("/{animal}", (req, res) -> {
            res.initializeHeader("200 OK", "text/json");
            String animal = req.parameter.get("animal");
            String fact = req.payload.get("fact");

            if(animal.equals("cat"))
            {
                insertNewFact("src/main/java/Testing/facts/cats.csv", fact);
                res.send("Done.");
            }

            else if(animal.equals("dog"))
            {
                insertNewFact("src/main/java/Testing/facts/dogs.csv", fact);
                res.send("Done.");
            }

        });

        listen(3000, ()-> {
            System.out.println("Mocha server is up and running!");
        });
    }

    public static int getNewId(String file)
    {
        BufferedReader reader;
        int lastId = 0;

        try
        {
            reader = new BufferedReader(new FileReader(file));
            String line = reader.readLine();
            line= reader.readLine();

            while(line != null)
            {
                lastId = Integer.parseInt(line.split(", ")[0]);
                line = reader.readLine();
            }

            reader.close();
        }
        catch (IOException e)
        {
            throw new RuntimeException(e);
        }

        int newId = lastId + 1;
        return newId;
    }

    public static void insertNewFact(String file, String fact)
    {
        try
        {
            FileWriter fw = new FileWriter(file, true);
            fw.write("\n" + getNewId(file) + ", " + fact);
            fw.close();
        }
        catch(IOException e)
        {
            e.printStackTrace();
        }
    }
}
