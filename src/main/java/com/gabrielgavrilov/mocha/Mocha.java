package com.gabrielgavrilov.mocha;

import java.io.*;
import java.util.HashMap;
import java.util.function.BiConsumer;

public class Mocha
{

    protected static HashMap<String, BiConsumer<MochaRequest, MochaResponse>> GET_ROUTES = new HashMap<>();

    public static void get(String route, BiConsumer<MochaRequest, MochaResponse> callback)
    {
        GET_ROUTES.put(route, callback);
    }

    public static void listen(int port, Runnable callback)
    {
        try
        {
            callback.run();
            MochaListenerThread serverThread = new MochaListenerThread(port);
            serverThread.start();
        }
        catch(IOException e)
        {
            e.printStackTrace();
        }
    }

}
