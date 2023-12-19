package com.gabrielgavrilov.mocha;

import java.io.*;
import java.util.HashMap;
import java.util.function.BiConsumer;

/**
 * Mocha - A tiny flexible web server framework for Java
 * @author Gabriel Gavriloiv <gabriel.gavrilov02@gmail.com>
 */
public class Mocha
{

    /**
     * CRUD routes
     */
    protected static HashMap<String, BiConsumer<MochaRequest, MochaResponse>> GET_ROUTES = new HashMap<>();
    protected static HashMap<String, BiConsumer<MochaRequest, MochaResponse>> POST_ROUTES = new HashMap<>();
    protected static HashMap<String, BiConsumer<MochaRequest, MochaResponse>> PUT_ROUTES = new HashMap<>();
    protected static HashMap<String, BiConsumer<MochaRequest, MochaResponse>> DELETE_ROUTES = new HashMap<>();

    protected static String VIEWS_DIRECTORY = "";
    protected static String STATIC_DIRECTORY = "";

    /**
     * Used to set server settings.
     *
     * @param setting Setting name.
     * @param value Setting value.
     */
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

    /**
     * Creates a new GET route. Stores the route and its callback into a HashMap. Gets called by
     * the MochaClient class when needed.
     *
     * @param route Route.
     * @param callback Callback function (BiConsumer that accepts MochaRequest and MochaResponse).
     */
    public static void get(String route, BiConsumer<MochaRequest, MochaResponse> callback)
    {
        GET_ROUTES.put(route, callback);
    }

    /**
     * Creates a new POST route. Stores the route and its callback into a HashMap. Gets called by
     * the MochaClient class when needed.
     *
     * @param route Route.
     * @param callback Callback function (BiConsumer that accepts MochaRequest and MochaResponse).
     */
    public static void post(String route, BiConsumer<MochaRequest, MochaResponse> callback)
    {
        POST_ROUTES.put(route, callback);
    }

    /**
     * Creates a new PUT route. Stores the route and its callback into a hashmap. Gets called by
     * the MochaClient class when needed.
     *
     * @param route Route
     * @param callback Callback function (BiConsumer that accepts MochaRequest and MochaResponse).
     */
    public static void put(String route, BiConsumer<MochaRequest, MochaResponse> callback)
    {
        PUT_ROUTES.put(route, callback);
    }

    /**
     * Creates as new DELETE route. Stores the route and its callback into a hashmap. Gets called
     * by the MochaClient class when needed.
     *
     * @param route
     * @param callback
     */
    public static void delete(String route, BiConsumer<MochaRequest, MochaResponse> callback)
    {
        DELETE_ROUTES.put(route, callback);
    }

    /**
     * Starts the Mocha web server at the given port and listens for new sockets.
     *
     * @param port Port for the server.
     * @param callback Runnable callback that gets executed when the server starts
     *                 listening for new sockets.
     */
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

    /**
     * Starts the Mocha web server at the given port and host address and listens for new sockets.
     *
     * @param port Port for the server.
     * @param host Host IP for the server.
     * @param callback Runnable callback that gets executed when the server starts
     *                 listening for new sockets.
     */
    public static void listen(int port, String host, Runnable callback)
    {
        try
        {
            callback.run();
            MochaListenerThread serverThread = new MochaListenerThread(port, host);
            serverThread.start();
        }
        catch (IOException e)
        {
            throw new RuntimeException(e);
        }
    }

}
