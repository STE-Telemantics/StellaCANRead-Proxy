package com.ste;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Properties;

import io.confluent.ksql.api.client.Client;
import io.confluent.ksql.api.client.ClientOptions;

public class Main {

    // Create a ksqlDB client
    public static Client ksqlDBClient;

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

        Properties ksqlProps = new Properties();

        try {
            ksqlProps = loadConfig("ksql.config");
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Could not open ksql.config file!");
            return;
        }

        // Initialize ksqlDB client
        ClientOptions options = ClientOptions.create()
                .setBasicAuthCredentials(ksqlProps.getProperty("username"),
                        ksqlProps.getProperty("password"))
                .setExecuteQueryMaxResultRows(Integer.MAX_VALUE).setHost(ksqlProps.getProperty("host"))
                .setPort(Integer.parseInt(ksqlProps.getProperty("port")))
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

    public static Properties loadConfig(final String configFile) throws IOException {
        if (!Files.exists(Paths.get(configFile))) {
            throw new IOException(configFile + " not found.");
        }

        final Properties cfg = new Properties();
        try (InputStream inputStream = new FileInputStream(configFile)) {
            cfg.load(inputStream);
        }
        return cfg;
    }
}
