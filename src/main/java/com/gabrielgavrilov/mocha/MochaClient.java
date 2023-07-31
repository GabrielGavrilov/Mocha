package com.gabrielgavrilov.mocha;

import java.io.*;
import java.nio.Buffer;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;

public class MochaClient {

    /**
     * Initializer for the MochaClient class. Reads the socket's requested header
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
     *
     *
     * @param header
     * @param route
     * @param method
     * @param clientOutput
     * @param br
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
     *
     *
     * @param route
     * @return
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
     *
     *
     * @param route
     * @param type
     * @param clientOutput
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
     *
     *
     * @param contentType
     * @param route
     * @param clientOutput
     * @throws IOException
     */
    private void renderStaticFile(String contentType, String route, OutputStream clientOutput) throws IOException
    {
        String file = route.substring(1);
        MochaResponse response = new MochaResponse("200 OK", contentType);

        response.render(file, Mocha.STATIC_DIRECTORY);

        clientOutput.write(response.header.toString().getBytes());
        clientOutput.flush();
    }

    /**
     *
     *
     * @param contentType
     * @param route
     * @param clientOutput
     * @throws IOException
     */
    private void renderStaticImage(String contentType, String route, OutputStream clientOutput) throws IOException
    {
        String file = route.substring(1);
        MochaResponse response = new MochaResponse("200 OK", contentType);

        clientOutput.write(response.header.toString().getBytes());

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
     *
     *
     * @param header
     * @param route
     * @param clientOutput
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
     *
     *
     * @param header
     * @param route
     * @param clientOutput
     * @param br
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
     *
     *
     * @param header
     * @param route
     * @param clientOutput
     * @throws IOException
     */
    private void handleGetResponse(String header, String route, OutputStream clientOutput) throws IOException
    {
        MochaRequest request = new MochaRequest();
        MochaResponse response = new MochaResponse("200 OK", "text/html");
        parseCookiesToHashMap(header);

        request.cookie = parseCookiesToHashMap(header);
        request.header = header;

        consume(Mocha.GET_ROUTES.get(route), request, response);

        clientOutput.write(response.header.toString().getBytes());
        clientOutput.flush();
    }

    /**
     *
     *
     * @param header
     * @param route
     * @param clientOutput
     * @param payload
     * @throws IOException
     */
    private void handlePostResponse(String header, String route, OutputStream clientOutput, String payload) throws IOException
    {
        MochaRequest request = new MochaRequest();
        MochaResponse response = new MochaResponse("200 OK", "text/html");

        request.payload = parsePayloadToHashMap(payload);
        request.cookie = parseCookiesToHashMap(header);
        request.header = header;

        consume(Mocha.POST_ROUTES.get(route), request, response);

        clientOutput.write(response.header.toString().getBytes());
        clientOutput.flush();
    }

    /**
     *
     *
     * @param header
     * @param consumer
     * @param route
     * @param clientOutput
     * @throws IOException
     */
    private void handleParsedGetRoute(String header, BiConsumer<MochaRequest, MochaResponse> consumer, String route, OutputStream clientOutput) throws IOException
    {
        MochaRequest request = new MochaRequest();
        MochaResponse response = new MochaResponse("200 OK", "text/html");
        MochaParser parser = new MochaParser(getTemplateFromParsedRoute(route, Mocha.GET_ROUTES), route);

        request.parameter = parser.parse();
        request.cookie = parseCookiesToHashMap(header);
        request.header = header;

        consume(consumer, request, response);

        clientOutput.write(response.header.toString().getBytes());
    }

    /**
     *
     *
     * @param header
     * @param consumer
     * @param route
     * @param clientOutput
     * @param payload
     * @throws IOException
     */
    private void handleParsedPostRoute(String header, BiConsumer<MochaRequest, MochaResponse> consumer, String route, OutputStream clientOutput, String payload) throws IOException
    {
        MochaRequest request = new MochaRequest();
        MochaResponse response = new MochaResponse("200 OK", "text/html");
        MochaParser parser = new MochaParser(getTemplateFromParsedRoute(route, Mocha.POST_ROUTES), route);

        request.parameter = parser.parse();
        request.payload = parsePayloadToHashMap(payload);
        request.cookie = parseCookiesToHashMap(header);
        request.header = header;

        consume(consumer, request, response);

        clientOutput.write(response.header.toString().getBytes());
    }

    /**
     *
     *
     * @param route
     * @param hashMap
     * @return
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
     *
     *
     * @param route
     * @param hashMap
     * @return
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
     *
     *
     * @param payload
     * @return
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
     *
     *
     * @param header
     * @return
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
     *
     *
     * @param clientOutput
     * @throws IOException
     */
    private void handleRouteNotFoundRequest(OutputStream clientOutput) throws IOException
    {
        MochaResponse response = new MochaResponse("200 OK", "text/html");
        response.send("<h2>404</h2><p>The page you are looking for does not exist.</p>");
        clientOutput.write(response.header.toString().getBytes());
        clientOutput.flush();
    }

    /**
     *
     *
     * @param consumer
     * @param request
     * @param response
     */
    private static void consume(BiConsumer<MochaRequest, MochaResponse> consumer, MochaRequest request, MochaResponse response)
    {
        consumer.accept(request, response);
    }

    /**
     *
     *
     * @param clientHeader
     * @return
     */
    private static String getRequestedRoute(String clientHeader)
    {
        return clientHeader.split("\r\n")[0].split(" ")[1];
    }

    /**
     *
     *
     * @param clientHeader
     * @return
     */
    private static String getRequestedMethod(String clientHeader)
    {
        return clientHeader.split("\r\n")[0].split(" ")[0];
    }
}