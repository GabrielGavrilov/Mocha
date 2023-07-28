package com.gabrielgavrilov.mocha;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Base64;

public class MochaResponse
{

    public StringBuilder header = new StringBuilder();

    /**
     * Constructor for the MochaResponse class. Used in MochaClient
     * to initialize a new HTTP response.
     *
     * @param status Status.
     * @param contentType Content type of the response.
     */
    MochaResponse(String status, String contentType)
    {
        this.header.append("HTTP/1.0 " + status + "\r\n");
        this.header.append("Content-Type: " + contentType + "\r\n");
        appendEmpty();
    }

    /**
     * Adds a new header row to the HTTP response header.
     *
     * @param header Header name.
     * @param value Header value.
     */
    public void addHeader(String header, String value)
    {
        this.header.append(header + ": " + value + "\r\n");
    }

    /**
     * Used to set a cookie name and value to the HTTP response.
     *
     * @param key Cookie name.
     * @param value Cookie value.
     */
    public void setCookie(String key, String value)
    {
        addHeader("Set-Cookie", key+"="+value);
    }

    /**
     * Appends the given data to the HTTP response render.
     *
     * @param data Data.
     */
    public void send(String data)
    {
        this.header.append(data);
        appendEmpty();
    }

    /**
     * Loads the given file data into the HTTP response header using the Mocha views directory.
     *
     * @param fileName File name.
     */
    public void render(String fileName)
    {
        render(fileName, Mocha.VIEWS_DIRECTORY);
    }

    /**
     * Loads the given file data into the HTTP response header with the provided directory.
     *
     * @param fileName File name.
     * @param directory Directory.
     */
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
