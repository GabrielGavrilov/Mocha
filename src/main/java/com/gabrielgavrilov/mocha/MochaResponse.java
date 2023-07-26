package com.gabrielgavrilov.mocha;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class MochaResponse
{

    public StringBuilder header = new StringBuilder();

    MochaResponse(String status, String contentType)
    {
        this.header.append("HTTP/1.0 " + status + "\r\n");
        this.header.append("Content-Type: " + contentType + "\r\n");
    }

    public void addHeader(String header, String value)
    {
        this.header.append(header + ": " + value + "\r\n");
    }

    public void send(String data)
    {
        appendEmpty();
        this.header.append(data);
        appendEmpty();
    }

    public void render(String fileName)
    {
        render(fileName, Mocha.VIEWS_DIRECTORY);
    }

    public void render(String fileName, String directory)
    {
        try
        {
            String fileContent = Files.readString(Paths.get(directory + fileName));
            send(fileContent);
        }
        catch (IOException e)
        {
            throw new RuntimeException(e);
        }
    }

    private void appendEmpty()
    {
        this.header.append("\r\n");
    }

}
