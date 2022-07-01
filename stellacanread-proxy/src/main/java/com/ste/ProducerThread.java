package com.ste;

import java.io.*;
import java.net.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ExecutionException;

import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.NewTopic;

import org.apache.kafka.common.errors.TopicExistsException;

import org.json.simple.JSONObject;

import com.ste.specificationstuff.CANEnumParser;

public class ProducerThread extends Thread {

    // The socket of this connection
    private Socket socket;

    // Properties required for the Kafka Producer
    private Properties props;

    // A Kafka producer that will put the received messages into Kafka
    private Producer<String, String> producer;

    private Set<String> topics;

    // A Kafka AdminClient used to create topics if they do not yet exist
    private AdminClient client;

    public ProducerThread(Socket socket) {
        this.socket = socket;
        props = loadProperties();

        if (props == null) {
            System.out.println("Properties null");
            return;
        }

        producer = new KafkaProducer<String, String>(props);
        client = AdminClient.create(props);

        try {
            topics = new HashSet<>(client.listTopics().names().get());
        } catch (final InterruptedException | ExecutionException e) {
            topics = new HashSet<>();
        }
    }

    public void run() {
        try {
            InputStream input = socket.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(input));

            while (true) {
                String message = reader.readLine();

                // If the read message is null, the end of the stream has been reached
                // TODO: Verify that if the teensy is 'idle' (e.g. not producing messages) this
                // is not terminated as new messages can be produced again later
                if (message == null) {
                    break;
                }

                // Retrieve the key of the record and the CAN message that needs to be
                // interpreted
                // message = "carx:can_message"
                String[] keyValue = message.split(":");
                String key = keyValue[0];
                String canMessage = keyValue[1];

                // Parse the CAN Message into an interpretable JSON object
                JSONObject obj = interpretMessage(canMessage);

                System.out.println(obj);

                // Something went wrong during parsing, skip this message
                if (obj == null) {
                    continue;
                }

                // System.out.println(obj.toJSONString());

                // System.out.print(obj.toString());

                // Get the topic for which this message is produced
                String topic = (String) obj.get("name");

                // Ensure the topic already exists in Confluent
                if (!topics.contains(topic)) {
                    // Add it if it doesn't, create it
                    createTopic(topic);
                }

                // Parse our JSON Object to string
                String record = obj.toJSONString();

                // And put it into Kafka
                producer.send(
                        new ProducerRecord<String, String>(topic, 0, (Long) obj.get("timestamp"),
                                key, record));

                JSONObject historicalJsonObject = new JSONObject();
                historicalJsonObject.put("key", key);
                historicalJsonObject.put("name", topic);
                historicalJsonObject.put("timestamp", obj.get("timestamp"));
                historicalJsonObject.put("value", obj.toJSONString());

                String historicTopic = "historical_test";

                if (!topics.contains(historicTopic)) {
                    createTopic(historicTopic);
                }

                String historicRecord = historicalJsonObject.toJSONString();
                producer.send(
                        new ProducerRecord<String, String>(historicTopic, 0, (Long) obj.get("timestamp"),
                                key, historicRecord));
            }

            // Close the socket
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private Properties loadProperties() {
        Properties p = new Properties();
        try {
            p = loadConfig("stellacanread-proxy/target/classes/java.config");
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Add additional properties.
        p.put(ProducerConfig.ACKS_CONFIG, "all");
        p.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringSerializer");
        p.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG,
                "org.apache.kafka.common.serialization.StringSerializer");
        p.put(ProducerConfig.ACKS_CONFIG, "1");
        p.put(ProducerConfig.LINGER_MS_CONFIG, 10);

        return p;
    }

    @SuppressWarnings({ "unchecked" })
    private JSONObject interpretMessage(String CANMessage) {
        long startTime = System.currentTimeMillis();
        int ID = CANEnumParser.parseID(CANMessage);
        String timestamp = CANEnumParser.parseTimestamp(CANMessage);
        String dataBytes = CANEnumParser.parseDataString(CANMessage);
        List<List<String>> testLists = CANEnumParser.parseOverview(ID);
        if (testLists == null) { // Ignore messages with unknown ID;
            return null;
        }
        int endianness = Integer.parseInt(testLists.get(3).get(0));
        String signalName = testLists.get(0).get(0);
        List<String> dataTypes = testLists.get(1);
        List<String> variableNames = testLists.get(2);
        List<String> bytes = CANEnumParser.determineBits(dataTypes, variableNames, dataBytes, endianness);
        List<Object> values = CANEnumParser.determineConcreteData(dataTypes, variableNames, bytes, timestamp);

        JSONObject obj = new JSONObject();

        obj.put("name", signalName);
        obj.put("timestamp", Long.parseLong(timestamp));
        for (int i = 1; i < dataTypes.size(); i++) {
            obj.put(variableNames.get(i), values.get(i));
        }

        long endTime = System.currentTimeMillis();

        // System.out.println("Interpreting took: " + (endTime - startTime) + "ms");
        return obj;
    }

    // Create topic in Confluent Cloud
    private void createTopic(final String topic) {
        final NewTopic newTopic = new NewTopic(topic, Optional.of(2), Optional.empty());

        try {
            client.createTopics(Collections.singletonList(newTopic)).all().get();
        } catch (final InterruptedException | ExecutionException e) {
            // Ignore TopicExistsExceptions, which may be valid if topic exists
            if (!(e.getCause() instanceof TopicExistsException)) {
                throw new RuntimeException(e);
            }
            return;
        }

        // The topic was added succesfully, add it to our list of topics to ensure we
        // don't try to add it again
        topics.add(topic);
    }

    private Properties loadConfig(final String configFile) throws IOException {
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
