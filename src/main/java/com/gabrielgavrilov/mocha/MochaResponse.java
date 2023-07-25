package com.gabrielgavrilov.mocha;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class MochaResponse
{

    private StringBuilder RESPONSE = new StringBuilder();

    public void send(String data)
    {
        this.send(data, "text/html");
    }

    public void render(String fileName)
    {
        render(fileName, "text/html");
    }
    public void render(String file, String contentType)
    {
        try
        {
            String fileContent = Files.readString(Paths.get(file));
            send(fileContent, contentType);
        }
        catch(IOException e)
        {
            throw new RuntimeException(e);
        }
    }


    private void initializeHeader(String status, String contentType) throws IOException
    {
        this.RESPONSE.append("HTTP/1.0 " + status + "\r\n");
        this.RESPONSE.append("Content-Type: " + contentType + "\r\n");
        this.RESPONSE.append("\r\n");
    }

    private void send(String data, String contentType)
    {
        try
        {
            if(!this.RESPONSE.toString().contains("HTTP/1.0 200 OK"))
                this.initializeHeader("200 OK", contentType);

            this.RESPONSE.append(data);
            this.closeHeader();
        }
        catch (IOException e)
        {
            throw new RuntimeException(e);
        }
    }

    private void closeHeader() throws IOException
    {
        this.RESPONSE.append("\r\n");
    }

    @Override
    public String toString()
    {
        return this.RESPONSE.toString();
    }

}
