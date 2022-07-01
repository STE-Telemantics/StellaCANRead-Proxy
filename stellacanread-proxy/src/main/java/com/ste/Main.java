package com.ste;

import com.ste.specificationstuff.CANEnumParser;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.*;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.Producer;

import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.NewTopic;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.concurrent.ExecutionException;
import org.apache.kafka.common.errors.TopicExistsException;
import org.json.simple.JSONObject;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Properties;
import java.util.Collections;
import java.util.Optional;

public class Main {

    public static void main(String[] args) throws IOException {
        // Initalize producer
        // Load properties from a local configuration file
        // Create the configuration file (e.g. at '$HOME/.confluent/java.config') with
        // configuration parameters
        // to connect to your Kafka cluster, which can be on your local host, Confluent
        // Cloud, or any other cluster.
        // Follow these instructions to create this file:
        // https://docs.confluent.io/platform/current/tutorials/examples/clients/docs/java.html
        final Properties props = loadConfig("stellacanread-proxy/target/classes/java.config");

        // Add additional properties.
        props.put(ProducerConfig.ACKS_CONFIG, "all");
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringSerializer");
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG,
                "org.apache.kafka.common.serialization.StringSerializer");
        props.put(ProducerConfig.ACKS_CONFIG, "1");
        props.put(ProducerConfig.LINGER_MS_CONFIG, 10);

        //historical topic
        String his_topic = "historical_test";

        Producer<String, String> producer = new KafkaProducer<String, String>(props);
        // Open the .scl file

        int i = 0;
        try (BufferedReader br = new BufferedReader(
                new FileReader("stellacanread-proxy/src/main/resources/era_zolder_vrijdag.scl"))) {
            for (String line; (line = br.readLine()) != null;) {
                if (i > 30000) {
                    break;
                }
                i++;
                long start = System.currentTimeMillis();

                JSONObject obj = interpretMessage(line);

                if (obj == null)
                    continue;

                createTopic((String) obj.get("name"), props);
                String key = "car0";
                String record = obj.toJSONString();

                producer.send(
                        new ProducerRecord<String, String>((String) obj.get("name"), 0, (Long) obj.get("timestamp"),
                                key, record));

                producer.send(
                        new ProducerRecord<String, String>(his_topic, 0, (Long) obj.get("timestamp"),
                                key, record));

                while (System.currentTimeMillis() < start + 000L) {
                    // Do nothing
                }
            }
            // line is not visible here.
        } catch (Exception e) {
            e.printStackTrace();
        }

        producer.flush();

        System.out.printf("Messages were produced");

        producer.close();
        // Read each line

        // For each line:
        // - Parse the message using the CAN Parser
        // Get the JSON object
        // Producer.produce(json object)
        // Wait 0.5s per message

        System.out.println("done");
    }

    public static JSONObject interpretMessage(String CANMessage) {
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

        return obj;
    }

    // Create topic in Confluent Cloud
    public static void createTopic(final String topic,
            final Properties cloudConfig) {
        final NewTopic newTopic = new NewTopic(topic, Optional.empty(), Optional.empty());
        try (final AdminClient adminClient = AdminClient.create(cloudConfig)) {
            adminClient.createTopics(Collections.singletonList(newTopic)).all().get();
        } catch (final InterruptedException | ExecutionException e) {
            // Ignore if TopicExistsException, which may be valid if topic exists
            if (!(e.getCause() instanceof TopicExistsException)) {
                throw new RuntimeException(e);
            }
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
