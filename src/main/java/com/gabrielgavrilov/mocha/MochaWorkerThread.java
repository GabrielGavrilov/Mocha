package com.gabrielgavrilov.mocha;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

public class MochaWorkerThread extends Thread {

    private Socket client;

    MochaWorkerThread(Socket socket)
    {
        this.client = socket;
    }

    @Override
    public void run()
    {
        try
        {
            InputStream clientInput = this.client.getInputStream();
            OutputStream clientOutput = this.client.getOutputStream();

            new MochaClient(clientInput, clientOutput);

            client.close();
        }
        catch (IOException e)
        {
            throw new RuntimeException(e);
        }
    }

}
