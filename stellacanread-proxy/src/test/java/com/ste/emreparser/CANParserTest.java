package com.ste.emreparser;

import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;

public class CANParserTest {

    // The instance variable that we'll use to call the functions on.
    private CANParser cp;

    // Instantiate a CANParser object
    @Before
    public void setUp() {
        cp = new CANParser();
    }

    /**
     * Tests that check the parseMessagesDefault method and the state of MessageObject
     */
    // Test that there are exactly 398 messages loaded in.
    @Test
    public void parseMessagesDefaultMessageAmount() {
        // The amount of messages that should be in messages.csv, this excludes
        // the 2 header lines that are present in messages.csv
        int expectedMessageAmount = 398;

        // Parse messages.csv
        List<MessageObject> messageList = cp.parseMessagesDefault();
        assertEquals("Message list size: ", messageList.size(), expectedMessageAmount);
    }

    // A MessageObject should always have a name
    @Test
    public void parseMessagesDefaultIsMessageObject() {
        // Parse message.csv
        List<MessageObject> messageList = cp.parseMessagesDefault();

        // Iterate through the list of messages and check if getName returns a String
        for (MessageObject mo : messageList) {
            // The condition that we're testing for, in this case that getName returns a String of
            // length > 0
            boolean condition = (mo.getName().length() > 0);
            assertTrue("This message has a name: ", condition);
        }
    }
    /**
     * Tests that check the parsedTypedefsDefault method
     */
    // Test that there are exactly 65 typedefs loaded in.
    @Test
    public void parseTypedefsDefaultTypdefAmount() {
        // The amount of typedefs that should be in typedefs.csv , this excludes
        // the 1 header line that is present in typedefs.csv
        int expectedTypedefAmount = 65;

        // Parse typedefs.csv and assert that the expected amount of typedef objects are present
        List<TypedefObject> typedefList = cp.parseTypedefsDefault();
        assertEquals(typedefList.size(), expectedTypedefAmount);
    }

}
