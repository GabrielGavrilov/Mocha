package com.gabrielgavrilov.mocha;

import java.io.*;
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

            String route = getRequestedRoute(clientHeader.toString());
            String method = getRequestedMethod(clientHeader.toString());

            handleRequest(route, method, clientOutput, br);
        }
        catch (IOException e)
        {
            throw new RuntimeException(e);
        }
    }

    private void handleRequest(String route, String method, OutputStream clientOutput, BufferedReader br) throws IOException
    {
        switch(method)
        {
            case "GET":
                handleGetRequest(route, clientOutput);
                break;
            case "POST":
                handlePostRequest(route, clientOutput, br);
                break;
        }
    }

    private void handleGetRequest(String route, OutputStream clientOutput) throws IOException
    {
        BiConsumer<MochaRequest, MochaResponse> consumerResponse = getBiConsumerFromParsedRoute(route, Mocha.GET_ROUTES);

        if(consumerResponse != null)
        {
            handleParsedGetRoute(consumerResponse, route, clientOutput);
            return;
        }

        if(Mocha.GET_ROUTES.get(route) != null)
            handleGetResponse(route, clientOutput);

        else
            handleRouteNotFoundRequest(clientOutput);
    }

    private void handlePostRequest(String route, OutputStream clientOutput, BufferedReader br) throws IOException
    {
        StringBuilder payload = new StringBuilder();
        BiConsumer<MochaRequest, MochaResponse> consumerResponse = getBiConsumerFromParsedRoute(route, Mocha.POST_ROUTES);

        while(br.ready())
        {
            payload.append((char)br.read());
        }

        if(consumerResponse != null)
        {
            handleParsedPostRoute(consumerResponse, route, clientOutput, payload.toString());
            return;
        }

        if(Mocha.POST_ROUTES.get(route) != null)
            handlePostResponse(route, clientOutput, payload.toString());

        else
            handleRouteNotFoundRequest(clientOutput);
    }

    private void handleGetResponse(String route, OutputStream clientOutput) throws IOException
    {
        MochaRequest request = new MochaRequest();
        MochaResponse response = new MochaResponse();

        consume(Mocha.GET_ROUTES.get(route), request, response);

        clientOutput.write(response.toString().getBytes());
        clientOutput.flush();
    }

    private void handlePostResponse(String route, OutputStream clientOutput, String payload) throws IOException
    {
        MochaRequest request = new MochaRequest();
        MochaResponse response = new MochaResponse();

        request.payload = payload;

        consume(Mocha.POST_ROUTES.get(route), request, response);

        clientOutput.write(response.toString().getBytes());
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

    private void handleParsedGetRoute(BiConsumer<MochaRequest, MochaResponse> consumer, String route, OutputStream clientOutput) throws IOException
    {
        MochaRequest request = new MochaRequest();
        MochaResponse response = new MochaResponse();
        MochaParser parser = new MochaParser(getTemplateFromParsedRoute(route, Mocha.GET_ROUTES), route);

        request.parameters = parser.parse();

        consume(consumer, request, response);

        clientOutput.write(response.toString().getBytes());
    }
    private void handleParsedPostRoute(BiConsumer<MochaRequest, MochaResponse> consumer, String route, OutputStream clientOutput, String payload) throws IOException
    {
        MochaRequest request = new MochaRequest();
        MochaResponse response = new MochaResponse();
        MochaParser parser = new MochaParser(getTemplateFromParsedRoute(route, Mocha.POST_ROUTES), route);

        request.parameters = parser.parse();
        request.payload = payload;

        consume(consumer, request, response);

        clientOutput.write(response.toString().getBytes());
    }

    private void handleRouteNotFoundRequest(OutputStream clientOutput) throws IOException
    {
        MochaResponse response = new MochaResponse();
        response.send("<h2>404</h2><p>The page you are looking for does not exist.</p>");
        clientOutput.write(response.toString().getBytes());
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
