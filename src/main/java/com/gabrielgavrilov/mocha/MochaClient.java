package com.gabrielgavrilov.mocha;

import java.io.*;
import java.nio.Buffer;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;

public class MochaClient {

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

            //System.out.println(clientHeader.toString());

            String route = getRequestedRoute(clientHeader.toString());
            String method = getRequestedMethod(clientHeader.toString());

            handleRequest(clientHeader.toString(), route, method, clientOutput, br);
        }
        catch (IOException e)
        {
            throw new RuntimeException(e);
        }
    }

    private void handleRequest(String header, String route, String method, OutputStream clientOutput, BufferedReader br) throws IOException
    {
        String type = checkForStaticRoute(route);

        if(type != null)
        {
            handleStaticRoute(route, type, clientOutput, br);
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

    private String checkForStaticRoute(String route) throws IOException
    {
        if(route.contains("."))
        {
            String[] routeSplit = route.split("\\.");
            return routeSplit[routeSplit.length-1];
        }

        return null;
    }

    private void handleStaticRoute(String route, String type, OutputStream clientOutput, BufferedReader br) throws IOException
    {
        switch(type)
        {
            case "css":
                handleStylesheetFile(route, clientOutput, br);
                break;
        }
    }

    private void handleStylesheetFile(String route, OutputStream clientOutput, BufferedReader br) throws IOException
    {
        String file = route.substring(1);
        MochaResponse response = new MochaResponse("200 OK", "text/css");
        response.render(file, Mocha.STATIC_DIRECTORY);
        clientOutput.write(response.header.toString().getBytes());
        clientOutput.flush();
    }

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

    private void handleGetResponse(String header, String route, OutputStream clientOutput) throws IOException
    {
        MochaRequest request = new MochaRequest();
        MochaResponse response = new MochaResponse("200 OK", "text/html");

        request.header = header;

        consume(Mocha.GET_ROUTES.get(route), request, response);

        clientOutput.write(response.header.toString().getBytes());
        clientOutput.flush();
    }

    private void handlePostResponse(String header, String route, OutputStream clientOutput, String payload) throws IOException
    {
        MochaRequest request = new MochaRequest();
        MochaResponse response = new MochaResponse("200 OK", "text/html");

        request.payload = parsePayloadToHashMap(payload);
        request.header = header;

        consume(Mocha.POST_ROUTES.get(route), request, response);

        clientOutput.write(response.header.toString().getBytes());
        clientOutput.flush();
    }

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

    private void handleParsedGetRoute(String header, BiConsumer<MochaRequest, MochaResponse> consumer, String route, OutputStream clientOutput) throws IOException
    {
        MochaRequest request = new MochaRequest();
        MochaResponse response = new MochaResponse("200 OK", "text/html");
        MochaParser parser = new MochaParser(getTemplateFromParsedRoute(route, Mocha.GET_ROUTES), route);

        request.parameter = parser.parse();
        request.header = header;

        consume(consumer, request, response);

        clientOutput.write(response.header.toString().getBytes());
    }
    private void handleParsedPostRoute(String header, BiConsumer<MochaRequest, MochaResponse> consumer, String route, OutputStream clientOutput, String payload) throws IOException
    {
        MochaRequest request = new MochaRequest();
        MochaResponse response = new MochaResponse("200 OK", "text/html");
        MochaParser parser = new MochaParser(getTemplateFromParsedRoute(route, Mocha.POST_ROUTES), route);

        request.parameter = parser.parse();
        request.payload = parsePayloadToHashMap(payload);
        request.header = header;

        consume(consumer, request, response);

        clientOutput.write(response.header.toString().getBytes());
    }

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

    private void handleRouteNotFoundRequest(OutputStream clientOutput) throws IOException
    {
        MochaResponse response = new MochaResponse("200 OK", "text/html");
        response.send("<h2>404</h2><p>The page you are looking for does not exist.</p>");
        clientOutput.write(response.header.toString().getBytes());
        clientOutput.flush();
    }

    private static void consume(BiConsumer<MochaRequest, MochaResponse> consumer, MochaRequest request, MochaResponse response)
    {
        consumer.accept(request, response);
    }

    private static String getRequestedRoute(String clientHeader)
    {
        return clientHeader.split("\r\n")[0].split(" ")[1];
    }

    private static String getRequestedMethod(String clientHeader)
    {
        return clientHeader.split("\r\n")[0].split(" ")[0];
    }
}
