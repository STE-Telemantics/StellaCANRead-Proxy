package com.ste.specificationstuff;
//import specificationstuff;
import com.ste.emreparser.*;
import org.junit.Test;
import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.math.BigInteger;


public class CANUnitTests {
    /* 
     * This file contains all the unit tests that are used in the parser of the project.
     * Each unit test is supposed to check only one atomic part of the parser code.
     * The test cases are written using jUnit, version 4.10.
     */


    // Used to access methods implemented for CANEnumParser
    static private CANEnumParser testParser = new CANEnumParser();

    // Sanity test
    @Test
    public void testAdd() {
        String str = "Junit is working fine";
        assertEquals("Junit is working fine", str);
    }
    
    // Test to see if all hexadecimal characters get converted to correct equivalent binary number
    @Test
    public void testHexStringToBinAllCharacters() {
        String str = "0123456789ABCDEF";
        String expected = "0000000100100011010001010110011110001001101010111100110111101111";
        // Function name to be replaced with actual function name
        String result = CANEnumParser.hexToBin(str);
		result = String.format("%064d", new BigInteger(result));
        assertEquals(expected, result);
    }

    // Test to see if all zero sequence gets converted to corresponding all zero sequence
    @Test
    public void testHexStringToBinZeros() {
        String str = "0000";
        String expected = "0000000000000000000000000000000000000000000000000000000000000000";

        String result = CANEnumParser.hexToBin(str);
		result = String.format("%064d", new BigInteger(result));
        assertEquals(expected, result);
    }

    // Test to see if empty string is handled correctly
    @Test
    public void testHexStringToBinEmpty() {
        String str = "";
        String expected = "";

        assertEquals(expected, CANEnumParser.hexToBin(str));
    }

    // Test to see if short hex string gets converted correctly
    @Test
    public void testHexStringToBinShort() {
        String str = "625AC";
        String expected = "01100010010110101100";

        assertEquals(expected, CANEnumParser.hexToBin(str));
    }

    // Test to see if long hex string get converted correctly
    @Test
    public void testHexStringToBinLong() {
        String str = "F3625B52A7D37AA9";
        String expected = "1111001101100010010110110101001010100111110100110111101010101001";
        
        String result = CANEnumParser.hexToBin(str);
		result = String.format("%064d", new BigInteger(result));
        assertEquals(expected, result);
    }
/* 
    // Test to see if an illegal argument provides the expected error message
    @Test(expected = IllegalArgumentException.class)
    public void testHexStringToBinIllegalArg() {
        // Function name to be replaced with actual function name
        testParser.convertHexadecimal(true); // should only accept strings, so this boolean should throw an error
    }

    @Test
    public void testParseIDBasic() {
        String testmsg = "(1600453413.322000) canx 12d#01c90100a819d400";
        String expected = "12";
        assertEquals(testParser.parseID("(1600453413.322000) canx 12d#01c90100a819d400"), expected);
    }

    // Test to see if an illegal argument provides the expected error message
    @Test(expected = IllegalArgumentException.class)
    public void testParseIdIllegalArg() {
        testParser.parseID(false); // should only accept strings, so this boolean should throw an error
    }

    @Test
    public void testParseDataBasic() {
        String testmsg = "(1600453413.322000) canx 12d#01c90100a819d400";
        String expected = "01c90100a819d400";
        assertEquals(testParser.parseData("(1600453413.322000) canx 12d#01c90100a819d400"), expected);
    }

    // Test to see if an illegal argument provides the expected error message
    @Test(expected = IllegalArgumentException.class)
    public void testParseDataIllegalArg() {
        testParser.parseData(false); // should only accept strings, so this boolean should throw an error
    }
    
    @Test
    public void testParseTimestampBasic() {
        String testmsg = "(1600453413.322000) canx 12d#01c90100a819d400";
        String expected = "1600453413.322000";
        assertEquals(testParser.parseTimestamp("(1600453413.322000) canx 12d#01c90100a819d400"), expected);
    }

    // Test to see if an illegal argument provides the expected error message
    @Test(expected = IllegalArgumentException.class)
    public void testParseTimestampIllegalArg() {
        testParser.parseTimestamp(false); // should only accept strings, so this boolean should throw an error
    }

    @Test
    public void testParseOverviewSignalName() {
        // This assumes ParseOverview() has output like [signal name, datatype list, data names list], in this order
        String id = "12";
        String timestamp = "1600453413.322000";
        String result_signal_name = testParser.parseOverview(id, timestamp)[0];
        String expected = "ACU_KeepAlive";
        assertEquals(expected, result_signal_name);
    }

    @Test
    public void testParseOverviewDatatypeList() {
        // This assumes ParseOverview() has output like [signal name, datatype list, data names list], in this order
        String id = "12";
        String timestamp = "1600453413.322000";
        List<String> result_datatype_list = testParser.parseOverview(id, timestamp)[1];
        List<String> expected = Arrays.asList("int",
                                                    "ACUMode",
                                                    "bool",
                                                    "bool",
                                                    "bool",
                                                    "bool",
                                                    "InverterType");
        assertEquals(expected, result_datatype_list);
    }

    @Test
    public void testParseOverviewDataNamesList() {
        // This assumes ParseOverview() has output like [signal name, datatype list, data names list], in this order
        String id = "12";
        String timestamp = "1600453413.322000";
        List<String> result_dataname_list = testParser.parseOverview(id, timestamp)[2];
        List<String> expected = Arrays.asList("timestamp",
                                                    "mode",
                                                    "bmsAlive",
                                                    "ssbAlive",
                                                    "srvRegenAlive",
                                                    "esbAlive",
                                                    "inverter");
        assertEquals(expected, result_dataname_list);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testParseOverviewIllegalArg() {
        // This assumes ParseOverview() has output like [signal name, datatype list, data names list], in this order
        int id = 2817;
        boolean timestamp = false;
        testParser.parseOverview(id, timestamp);
    }

    @Test
    public void testDetermineBitsSignalName() {
        String id = "12";
        String timestamp = "1600453413.322000";
        String data = "01c90100a819d400";
        String result_signal_name = testParser.parseOverview(id, timestamp)[0];
        List<String> result_datatype_list = testParser.parseOverview(id, timestamp)[1];
        List<String> result_dataname_list = testParser.parseOverview(id, timestamp)[2];
        String passed_on_signal_name = testParser.determineBits(result_signal_name,
                                                                result_datatype_list,
                                                                result_dataname_list,
                                                                data)[0];
        String expected = "ACU_KeepAlive";
        assertEquals(passed_on_signal_name, expected);
    }

    @Test
    public void testDetermineBitsValues() {
        String id = "12";
        String timestamp = "1600453413.322000";
        String data = "01c90100a819d400";
        String result_signal_name = testParser.parseOverview(id, timestamp)[0];
        List<String> result_datatype_list = testParser.parseOverview(id, timestamp)[1];
        List<String> result_dataname_list = testParser.parseOverview(id, timestamp)[2];
        List<Object> result_bit_values = testParser.determineBits(result_signal_name,
                                                                result_datatype_list,
                                                                result_dataname_list,
                                                                data)[1];
        List<Object> expected = Arrays.asList(null, 00000001, 1, 0, 1, 1, 00000011);
        assertEquals(result_bit_values, expected);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testDetermineBitsIllegalArg() {
        String id = "12";
        String timestamp = "1600453413.322000";
        String data = "01c90100a819d400";
        String result_signal_name = testParser.parseOverview(id, timestamp)[0];
        List<String> result_datatype_list = testParser.parseOverview(id, timestamp)[1];
        List<String> result_dataname_list = testParser.parseOverview(id, timestamp)[2];
        testParser.determineBits(result_signal_name, result_datatype_list, result_dataname_list, false);
    }

    // Not entirely sure what input for this function will be
    // TODO: Enter correct input format
    @Test
    public void testParseTypeDef() {
        HashMap<String, HashMap<String, String>> result = testParser.parseTypeDef("ACUMode");
        // TODO figure out correct way to intantite Java HashMap
        HashMap<String, HashMap<String, String>> expected = {"ACUMode", {("00000000", "ACUModeRDW"),
                                                                         ("00000001", "ACUModeRWC"),
                                                                         ("00000002", "DARE")}};
        assertEquals(result, expected);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testParseTypeDef1() {
        // This by itself should give an error
        HashMap<String, HashMap<String, String>> result = testParser.parseTypeDef(387);
    }
*/
}
