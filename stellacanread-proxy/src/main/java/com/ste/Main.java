package com.ste;

import java.io.IOException;
import java.net.*;

public class Main {

    public static void main(String[] args) {
        // Try to get the port from the arguments
        int port = 0;
        try {
            Integer.parseInt(args[0]);
        } catch (Exception e) {
            // Otherwise take port 5000 by default
            port = 5000;
        }

        // Try to open the server with the input port
        try (ServerSocket server = new ServerSocket(port)) {
            // Keep listening for connections forever
            while (true) {
                // Wait for a connection to come in
                Socket socket = server.accept();

                // If a connection has come in, enable TCP KeepAlive
                socket.setKeepAlive(true);

                System.out.println("New client connected");

                // And start a thread to handle the socket communication
                new ProducerThread(socket).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
