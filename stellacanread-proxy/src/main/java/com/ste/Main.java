package com.ste;

import java.io.IOException;
import java.net.*;
import java.util.concurrent.ExecutionException;

import io.confluent.ksql.api.client.Client;
import io.confluent.ksql.api.client.ClientOptions;

public class Main {

    public static Client ksqlDBClient;

    // Set the host and the port for the ksqlDB cluster
    public static String KSQLDB_SERVER_HOST = "pksqlc-03n59.westeurope.azure.confluent.cloud";
    public static int KSQLDB_SERVER_HOST_PORT = 443;

    public static void main(String[] args) {
        System.out.println("Opened server");
        // Try to get the port from the arguments
        int port = 0;
        try {
            Integer.parseInt(args[0]);
        } catch (Exception e) {
            // Otherwise take port 5000 by default
            port = 5000;
        }

        // Initialize ksqlDB client
        ClientOptions options = ClientOptions.create()
                .setBasicAuthCredentials("BR575ST7TIPP2IAC",
                        "KpbOy9WNWq7hwG+hTK+RJnWF0kLwuBGvTrPNpkSTviTuIn21Tys2wY+p83wkOIcU")
                .setExecuteQueryMaxResultRows(Integer.MAX_VALUE).setHost(KSQLDB_SERVER_HOST)
                .setPort(KSQLDB_SERVER_HOST_PORT)
                .setUseTls(true).setUseAlpn(true);
        ksqlDBClient = Client.create(options);

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
