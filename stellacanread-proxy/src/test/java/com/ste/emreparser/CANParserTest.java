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

    // Test if a MessageObject always has default values for each data type
    @Test
    public void messageObjectHasDefaultValues() {
        // Parse messages.csv
        List<MessageObject> messageList = cp.parseMessagesDefault();

        // For each typedefObject in the list check whether they have equal amount of arguments
        // for the datatype array and the defaultvalues array.
        for (MessageObject mo : messageList) {
            assertEquals(mo.getDataTypes().length, mo.getDefaultValues().length);
        }
    }

    // The default messages.csv file does not have a signal with id 3, test this
    @Test
    public void parseMessagesDefaultIncorrectID() {
        // Parse messages.csv
        List<MessageObject> messageList = cp.parseMessagesDefault();

        // The ID that SHOULD be missing
        int badID = 3;

        // Iterate through the list of message and make sure that id 3 is not among the messages
        for (MessageObject mo: messageList) {
            assertNotEquals(badID, Integer.parseInt(mo.getId()));
        }
    }

    // A MessageObject should always have a name
    @Test
    public void parseMessagesDefaultHasName() {
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

    // Check to see if messages with a certain ID indeed have the correct name
    @Test
    public void parseMessagesDefaultIDName1() {
        // Parse messages.csv
        List<MessageObject> messageList = cp.parseMessagesDefault();

        // The ID and name that are going to be checked
        int id = 203;
        String expectedName = "BMS_ContinuousCharging";

        // Look through the list of messages for the relevant ID
        for (MessageObject mo : messageList) {
            if (Integer.parseInt(mo.getId()) == id) {
                // ID was found, now assert that the names are correct
                assertEquals(expectedName, mo.getName());
            }
        }
    }

    @Test
    public void parseMessagesDefaultIDName2() {
        // Parse messages.csv
        List<MessageObject> messageList = cp.parseMessagesDefault();

        // The id and name that are going to be checked
        int id = 305;
        String expectedName = "ACU_ThrottlePedal";

        // Look through the list of messages for the relevant ID
        for (MessageObject mo : messageList) {
            if (Integer.parseInt(mo.getId()) == id) {
                // ID was found, now assert tghat the names are correct
                assertEquals(expectedName, mo.getName());
            }
        }
    }

    @Test
    public void parseMessagesDefaultIDName3() {
        // Parse messages.csv
        List<MessageObject> messageList = cp.parseMessagesDefault();

        // The id and name that are going to be checked
        int id = 700106;
        String expectedName = "Ch2_CVTimeout_Set";

        // Look through the list of messages for the relevant ID
        for (MessageObject mo : messageList) {
            if (Integer.parseInt(mo.getId()) == id) {
                // ID was found, now assert that the names are correct
                assertEquals(expectedName, mo.getName());
            }
        }
    }

    // Each message should always have a field name for each data type.
    // Test if each message has the same amount of data types and field names
    @Test
    public void parseMessagesDefaultEqualAmountOfFields () {
        // Parse messages.csv
        List<MessageObject> messageList = cp.parseMessagesDefault();

        // For each message, the amount of data types should be equal to the amount of field names
        for (MessageObject mo : messageList) {
            boolean hasEqualLength = (mo.getFieldNames().length == mo.getDataTypes().length);
            assertTrue(hasEqualLength);
        }
    }

    /**
     * Tests that check the parsedTypedefsDefault method and the TypedefObject class
     */
    // Test that there are exactly 65 typedefs loaded in.
    @Test
    public void parseTypedefsDefaultTypedefAmount() {
        // The amount of typedefs that should be in typedefs.csv , this excludes
        // the 1 header line that is present in typedefs.csv
        int expectedTypedefAmount = 65;

        // Parse typedefs.csv and assert that the expected amount of typedef objects are present
        List<TypedefObject> typedefList = cp.parseTypedefsDefault();
        assertEquals(typedefList.size(), expectedTypedefAmount);
    }

    // Test if an exception is thrown if a different typedef enum is used
    @Test(expected = IllegalArgumentException.class)
    public void parseTypedefsDefaultIllegalType1() {
        // Try to create an object with type uint16_t. This is expected to fail
        new TypedefObject("AVCState",
            "enum class AVCState : uint16_t {Unknown, Startup, ConvertersEnabled, ManualOverride, Failure}",
            "State of the AVC");
    }

    // Test if an exception is thrown if a nonsensical type is used
    @Test(expected = IllegalArgumentException.class)
    public void parseTypedefsDefaultIllegalType2() {
        // Try to create an object with type uint16_t. This is expected to fail
        new TypedefObject("AVCState",
            "enum class AVCState : hjklksdg {Unknown, Startup, ConvertersEnabled, ManualOverride, Failure}",
            "State of the AVC");
    }

}
