package com.gabrielgavrilov.mocha;

import java.io.*;
import java.util.HashMap;
import java.util.function.BiConsumer;

public class Mocha
{

    protected static HashMap<String, BiConsumer<MochaRequest, MochaResponse>> GET_ROUTES = new HashMap<>();
    protected static HashMap<String, BiConsumer<MochaRequest, MochaResponse>> POST_ROUTES = new HashMap<>();

    protected static String VIEWS_DIRECTORY = "";
    protected static String STATIC_DIRECTORY = "";

    public static void set(String setting, String value)
    {
        switch(setting)
        {
            case "views":
                VIEWS_DIRECTORY = value;
                break;
            case "static":
                STATIC_DIRECTORY = value;
                break;
        }
    }

    public static void get(String route, BiConsumer<MochaRequest, MochaResponse> callback)
    {
        GET_ROUTES.put(route, callback);
    }

    public static void post(String route, BiConsumer<MochaRequest, MochaResponse> callback)
    {
        POST_ROUTES.put(route, callback);
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
