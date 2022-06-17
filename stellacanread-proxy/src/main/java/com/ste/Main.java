package com.ste;

import com.ste.specificationstuff.CANEnumParser;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.*;

import org.json.simple.JSONObject;

public class Main {

    public static void main(String[] args) {
        // Open the .scl file

        try (BufferedReader br = new BufferedReader(
                new FileReader("stellacanread-proxy/src/main/resources/era_zolder_vrijdag.scl"))) {
            for (String line; (line = br.readLine()) != null;) {
                long start = System.currentTimeMillis();

                interpretMessage(line);

                while (System.currentTimeMillis() < start + 0L) {
                    // Do nothing
                }
            }
            // line is not visible here.
        } catch (Exception e) {
            e.printStackTrace();
        }
        // Read each line

        // For each line:
        // - Parse the message using the CAN Parser
        // Get the JSON object
        // Producer.produce(json object)
        // Wait 0.5s per message

        System.out.println("done");
    }

    public static void interpretMessage(String CANMessage) {
        int ID = CANEnumParser.parseID(CANMessage);
        String timestamp = CANEnumParser.parseTimestamp(CANMessage);
        String dataBytes = CANEnumParser.parseDataString(CANMessage);
        List<List<String>> testLists = CANEnumParser.parseOverview(ID);
        if (testLists == null) { // Ignore messages with unknown ID;
            return;
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

        System.out.println(obj);
    }
}
