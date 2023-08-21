package com.gabrielgavrilov.mocha;

import java.io.*;
import java.nio.Buffer;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;

public class MochaClient {

    /**
     * Initializes the MochaClient class. Reads the socket's requested header
     * and determines the response based on the method and route.
     *
     * @param clientInput Socket InputStream.
     * @param clientOutput Socket OutputStream.
     */
    MochaClient(InputStream clientInput, OutputStream clientOutput)
    {
        try
        {
            InputStreamReader sr = new InputStreamReader(clientInput);
            BufferedReader br = new BufferedReader(sr);
            StringBuilder clientHeader = new StringBuilder();

            String line;
            while((line = br.readLine()).length() != 0)
            {
                clientHeader.append(line + "\r\n");
            }

            String route = getRequestedRoute(clientHeader.toString());
            String method = getRequestedMethod(clientHeader.toString());

            handleRequest(clientHeader.toString(), route, method, clientOutput, br);
        }
        catch (IOException e)
        {
            throw new RuntimeException(e);
        }
    }

    /**
     * Handles the given HTTP request.
     *
     * @param header Socket header.
     * @param route Requested route.
     * @param method Requested method.
     * @param clientOutput Client output stream.
     * @param br Buffered Reader
     * @throws IOException
     */
    private void handleRequest(String header, String route, String method, OutputStream clientOutput, BufferedReader br) throws IOException
    {
        String type = checkForStaticRoute(route);

        if(type != null)
        {
            handleStaticRoute(route, type, clientOutput);
            return;
        }

        switch(method)
        {
            case "GET":
                handleGetRequest(header, route, clientOutput);
                break;
            case "POST":
                handlePostRequest(header, route, clientOutput, br);
                break;
        }
    }

    /**
     * Checks if the given route is a static file. If the route is a static file, then
     * this method will return the static file's type. Otherwise, it'll return a null.
     *
     * @param route Requested route.
     * @return String.
     * @throws IOException
     */
    private String checkForStaticRoute(String route) throws IOException
    {
        if(route.contains("."))
        {
            String[] routeSplit = route.split("\\.");
            return routeSplit[routeSplit.length-1];
        }

        return null;
    }

    /**
     * Handles the static route.
     *
     * @param route Requested route.
     * @param type Static file type.
     * @param clientOutput Client output stream.
     * @throws IOException
     */
    private void handleStaticRoute(String route, String type, OutputStream clientOutput) throws IOException
    {
        switch(type)
        {
            case "css":
                renderStaticFile("text/css", route, clientOutput);
                break;
            case "js":
                renderStaticFile("text/javascript", route, clientOutput);
                break;
            case "png":
                renderStaticImage("image/png", route, clientOutput);
                break;
            case "jpeg":
                renderStaticImage("image/jpeg", route, clientOutput);
                break;
        }
    }

    /**
     * Renders the static file.
     *
     * @param contentType Content type of the static file.
     * @param route requested route.
     * @param clientOutput Client output stream.
     * @throws IOException
     */
    private void renderStaticFile(String contentType, String route, OutputStream clientOutput) throws IOException
    {
        String file = route.substring(1);
        MochaResponse response = new MochaResponse();
        String fileContent = Files.readString(Paths.get(Mocha.STATIC_DIRECTORY + file));

        response.initializeHeader("200 OK", contentType);

        clientOutput.write(response.header.toString().getBytes());
        clientOutput.write("\r\n".getBytes());
        clientOutput.write(fileContent.getBytes());
        clientOutput.flush();
    }

    /**
     * Renders the static image.
     *
     * @param contentType Content type of the static image.
     * @param route Requested route.
     * @param clientOutput Client output stream.
     * @throws IOException
     */
    private void renderStaticImage(String contentType, String route, OutputStream clientOutput) throws IOException
    {
        String file = route.substring(1);
        MochaResponse response = new MochaResponse();

        response.initializeHeader("200 OK", contentType);

        clientOutput.write(response.header.toString().getBytes());
        clientOutput.write("\r\n".getBytes());

        try
        {
            FileInputStream content = new FileInputStream(Mocha.STATIC_DIRECTORY + file);

            int i = 0;
            while((i = content.read()) != -1)
            {
                clientOutput.write(i);
            }

            content.close();
        }
        catch(IOException e)
        {
            e.printStackTrace();
        }
        finally
        {
            clientOutput.flush();
        }
    }

    /**
     * Handles the GET request.
     *
     * @param header Client HTTP header.
     * @param route Requested route.
     * @param clientOutput Client output stream.
     * @throws IOException
     */
    private void handleGetRequest(String header, String route, OutputStream clientOutput) throws IOException
    {
        BiConsumer<MochaRequest, MochaResponse> consumerResponse = getBiConsumerFromParsedRoute(route, Mocha.GET_ROUTES);

        if(consumerResponse != null)
        {
            handleParsedGetRoute(header, consumerResponse, route, clientOutput);
            return;
        }

        if(Mocha.GET_ROUTES.get(route) != null)
            handleGetResponse(header, route, clientOutput);

        else
            handleRouteNotFoundRequest(clientOutput);
    }

    /**
     * Handles the POST request.
     *
     * @param header Client HTTP header.
     * @param route Requested route.
     * @param clientOutput Client output stream.
     * @param br Buffered reader.
     * @throws IOException
     */
    private void handlePostRequest(String header, String route, OutputStream clientOutput, BufferedReader br) throws IOException
    {
        StringBuilder payload = new StringBuilder();
        BiConsumer<MochaRequest, MochaResponse> consumerResponse = getBiConsumerFromParsedRoute(route, Mocha.POST_ROUTES);

        while(br.ready())
        {
            payload.append((char)br.read());
        }

        if(consumerResponse != null)
        {
            handleParsedPostRoute(header, consumerResponse, route, clientOutput, payload.toString());
            return;
        }

        if(Mocha.POST_ROUTES.get(route) != null)
            handlePostResponse(header, route, clientOutput, payload.toString());

        else
            handleRouteNotFoundRequest(clientOutput);
    }

    /**
     * Handles the GET response.
     *
     * @param header Client HTTP header.
     * @param route Requested route.
     * @param clientOutput Client output stream.
     * @throws IOException
     */
    private void handleGetResponse(String header, String route, OutputStream clientOutput) throws IOException
    {
        MochaRequest request = new MochaRequest();
        MochaResponse response = new MochaResponse();
        parseCookiesToHashMap(header);

        request.cookie = parseCookiesToHashMap(header);
        request.header = header;

        consume(Mocha.GET_ROUTES.get(route), request, response);
        writeFullResponse(response, clientOutput);
    }

    /**
     * Handles the POST response.
     *
     * @param header Client HTTP header.
     * @param route Requested route.
     * @param clientOutput Client output stream.
     * @param payload Post payload.
     * @throws IOException
     */
    private void handlePostResponse(String header, String route, OutputStream clientOutput, String payload) throws IOException
    {
        MochaRequest request = new MochaRequest();
        MochaResponse response = new MochaResponse();

        request.payload = parsePayloadToHashMap(payload);
        request.cookie = parseCookiesToHashMap(header);
        request.header = header;

        consume(Mocha.POST_ROUTES.get(route), request, response);
        writeFullResponse(response, clientOutput);
    }

    /**
     * Handles the parsed GET route.
     *
     * @param header Client HTTP header.
     * @param consumer MochaRequest and MochaResponse BiConsumer.
     * @param route Requested route.
     * @param clientOutput Client output stream.
     * @throws IOException
     */
    private void handleParsedGetRoute(String header, BiConsumer<MochaRequest, MochaResponse> consumer, String route, OutputStream clientOutput) throws IOException
    {
        MochaRequest request = new MochaRequest();
        MochaResponse response = new MochaResponse();
        MochaParser parser = new MochaParser(getTemplateFromParsedRoute(route, Mocha.GET_ROUTES), route);

        request.parameter = parser.parse();
        request.cookie = parseCookiesToHashMap(header);
        request.header = header;

        consume(consumer, request, response);
        writeFullResponse(response, clientOutput);
    }

    /**
     * Handles the parsed POST route.
     *
     * @param header Client HTTP header.
     * @param consumer MochaRequest and MochaResponse BiConsumer.
     * @param route Requested route.
     * @param clientOutput Client output stream.
     * @param payload POST payload.
     * @throws IOException
     */
    private void handleParsedPostRoute(String header, BiConsumer<MochaRequest, MochaResponse> consumer, String route, OutputStream clientOutput, String payload) throws IOException
    {
        MochaRequest request = new MochaRequest();
        MochaResponse response = new MochaResponse();
        MochaParser parser = new MochaParser(getTemplateFromParsedRoute(route, Mocha.POST_ROUTES), route);

        request.parameter = parser.parse();
        request.payload = parsePayloadToHashMap(payload);
        request.cookie = parseCookiesToHashMap(header);
        request.header = header;

        consume(consumer, request, response);
        writeFullResponse(response, clientOutput);
    }

    /**
     * Returns the BiConsumer from the parsed route.
     *
     * @param route Requested route.
     * @param hashMap Method hashmap.
     * @return MochaRequest and MochaResponse BiConsumer
     */
    private BiConsumer<MochaRequest, MochaResponse> getBiConsumerFromParsedRoute(String route, HashMap<String, BiConsumer<MochaRequest, MochaResponse>> hashMap)
    {
        for(Map.Entry<String, BiConsumer<MochaRequest, MochaResponse>> entry : hashMap.entrySet())
        {
            MochaParser parser = new MochaParser(entry.getKey(), route);
            if(parser.isParsable())
                return entry.getValue();
        }

        return null;
    }

    /**
     * Returns the template route from the parsed route.
     *
     * @param route Requested route.
     * @param hashMap Method hashmap.
     * @return String
     */
    private String getTemplateFromParsedRoute(String route, HashMap<String, BiConsumer<MochaRequest, MochaResponse>> hashMap)
    {
        for(Map.Entry<String, BiConsumer<MochaRequest, MochaResponse>> entry : hashMap.entrySet())
        {
            MochaParser parser = new MochaParser(entry.getKey(), route);
            if(parser.isParsable())
                return entry.getKey();
        }

        return null;
    }

    /**
     * Parses the raw payload into a hashmap.
     *
     * @param payload Raw payload.
     * @return String and String Hashmap.
     */
    private HashMap<String, String> parsePayloadToHashMap(String payload)
    {
        HashMap<String, String> payloadData = new HashMap<>();
        String[] payloads = payload.split("&");

        for(int i = 0; i < payloads.length; i++)
        {
            String[] currentPayload = payloads[i].split("=");
            payloadData.put(currentPayload[0], currentPayload[1]);
        }

        return payloadData;
    }

    /**
     * Parses the cookies into a hash map.
     *
     * @param header Client HTTP header.
     * @return String and String Hashmap.
     */
    private HashMap<String, String> parseCookiesToHashMap(String header)
    {
        HashMap<String, String> cookieData = new HashMap<>();

        if(header.contains("Cookie"))
        {
            String[] headerSplit = header.split("\n");
            for(int i = 0; i < headerSplit.length; i++)
            {
                if(headerSplit[i].contains("Cookie"))
                {
                    String cookieHeader = headerSplit[i].substring(8);
                    String[] cookies = cookieHeader.split("; ");

                    for(int j = 0; j < cookies.length; j++)
                    {
                        String[] cookie = cookies[j].split("=");
                        cookieData.put(cookie[0], cookie[1]);
                    }

                    return cookieData;
                }
            }
        }

        return null;
    }

    /**
     * Handles the 404 page.
     *
     * @param clientOutput Client output stream.
     * @throws IOException
     */
    private void handleRouteNotFoundRequest(OutputStream clientOutput) throws IOException
    {
        for(Map.Entry<String, BiConsumer<MochaRequest, MochaResponse>> entry : Mocha.GET_ROUTES.entrySet())
        {
            if(entry.getKey().equals("*"))
            {
                MochaRequest request = new MochaRequest();
                MochaResponse response = new MochaResponse();
                BiConsumer<MochaRequest, MochaResponse> callback = entry.getValue();

                consume(callback, request, response);
                writeFullResponse(response, clientOutput);
                return;
            }
        }

        MochaResponse response = new MochaResponse();
        response.initializeHeader("200 OK", "text/html");
        response.send("<h2>404</h2><p>The page you are looking for does not exist.</p>");
        writeFullResponse(response, clientOutput);
    }

    /**
     * Executes the BiConsumer.
     *
     * @param consumer MochaRequest and MochaResponse BiConsumer.
     * @param request Mocha request.
     * @param response Mocha response.
     */
    private static void consume(BiConsumer<MochaRequest, MochaResponse> consumer, MochaRequest request, MochaResponse response)
    {
        consumer.accept(request, response);
    }

    /**
     * Returns the requested route.
     *
     * @param clientHeader Client HTTP header.
     * @return String
     */
    private static String getRequestedRoute(String clientHeader)
    {
        return clientHeader.split("\r\n")[0].split(" ")[1];
    }

    /**
     * Returns the reauested method.
     *
     * @param clientHeader Client HTTP header.
     * @return String
     */
    private static String getRequestedMethod(String clientHeader)
    {
        return clientHeader.split("\r\n")[0].split(" ")[0];
    }

    private static void writeFullResponse(MochaResponse response, OutputStream clientOutput) throws IOException {
        clientOutput.write(response.header.toString().getBytes());
        clientOutput.write("\r\n".getBytes());
        clientOutput.write(response.body.toString().getBytes());
        clientOutput.flush();
    }
}