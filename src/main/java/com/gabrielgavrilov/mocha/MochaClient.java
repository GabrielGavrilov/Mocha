package com.gabrielgavrilov.mocha;

import java.io.*;
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
            handleRequest(route, clientOutput);
        }
        catch (IOException e)
        {
            throw new RuntimeException(e);
        }
    }

    private void handleRequest(String route, OutputStream clientOutput) throws IOException
    {
        BiConsumer<MochaRequest, MochaResponse> consumerResponse = getBiConsumerFromParsedRoute(route);

        if(consumerResponse != null)
        {
            handleParsedRoute(consumerResponse, route, clientOutput);
            return;
        }

        if(Mocha.GET_ROUTES.get(route) != null)
            handleGetRequest(route, clientOutput);

        else
            handleRouteNotFoundRequest(clientOutput);
    }

    private BiConsumer<MochaRequest, MochaResponse> getBiConsumerFromParsedRoute(String route)
    {
        for(Map.Entry<String, BiConsumer<MochaRequest, MochaResponse>> entry : Mocha.GET_ROUTES.entrySet())
        {
            MochaParser parser = new MochaParser(entry.getKey(), route);
            if(parser.isParsable())
                return entry.getValue();
        }

        return null;
    }

    private String getTemplateFromParsedRoute(String route)
    {
        for(Map.Entry<String, BiConsumer<MochaRequest, MochaResponse>> entry : Mocha.GET_ROUTES.entrySet())
        {
            MochaParser parser = new MochaParser(entry.getKey(), route);
            if(parser.isParsable())
                return entry.getKey();
        }

        return null;
    }

    private static String getRequestedRoute(String clientHeader)
    {
        return clientHeader.split("\r\n")[0].split(" ")[1];
    }

    private void handleParsedRoute(BiConsumer<MochaRequest, MochaResponse> consumer, String route, OutputStream clientOutput) throws IOException
    {
        MochaRequest request = new MochaRequest();
        MochaResponse response = new MochaResponse();

        MochaParser parser = new MochaParser(getTemplateFromParsedRoute(route), route);
        request.parameters = parser.parse();

        consume(consumer, request, response);

        clientOutput.write(response.toString().getBytes());
    }

    private void handleGetRequest(String route, OutputStream clientOutput) throws IOException
    {
        MochaRequest request = new MochaRequest();
        MochaResponse response = new MochaResponse();

        consume(Mocha.GET_ROUTES.get(route), request, response);
        String responseData = response.toString();

        clientOutput.write(responseData.getBytes());
        clientOutput.flush();
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

}
