/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.ste.specificationstuff;

import java.util.*;

import com.ste.emreparser.*;

import java.math.BigInteger;
//import java.util.Date;
import java.util.HashSet;
import java.util.Collections;

//CONSULT DRIVE FOR EASIER OVERVIEW OF FUNCTIONS - https://drive.google.com/drive/u/0/folders/1vgE6a2_4SK2RJL6YsllVkD5Wmiu9INAC

/*Note: Bytes should always be read from left to right. Enums are always represented using a single byte, whereas booleans are represented using a single bit. Integers can be
* represented with upto 32 bits. Furthermore, the bytes that represent enums are typically equal to 00, 01, 02, 03 etc in hexadecimal. If a variable has three states it can only
* be equal to 00, 01, and 02. Also, enums are allowed to have non-standard byte sequences. In this case STE defines a what the byte sequence of a specific state looks like in the
* CAN_typedef_2019_17-format.csv file.
*
* Examples:
* signal A has 1 boolean that is set to true. The 8 bytes look as follows: 00000001 00000000 00000000 00000000 00000000 00000000 00000000 00000000
* signal B has 3 booleans in the following order (from top to bottom in CAN-X) true, false, true. 8 bytes: 00000101 00000000 00000000 00000000 00000000 00000000 00000000 00000000
* signal C has 2 booleans and 1 enum in the following order true true enumState2. 8 bytes: 00000011 00000010 00000000 00000000 00000000 00000000 00000000 00000000
* signal D has 2 booleans and 2 enum in the following order true enumState0 enumState1 true. 8 bytes: 00000001 00000000 00000001 00000001 00000000 00000000 00000000 00000000
* signal E has 3 booleans and a predefined enum state as 0x03 in the following order false false true enumState: 00000100 00000011 00000000 00000000 00000000 00000000 00000000 00000000
/*

/**
 *
 * @author 20172420
 */

public class CANEnumParser {
	// The idea of this class is that we receive a CAN message such as the string
	// 'testmsg' down below, and we convert it to a hashmap that contains the name
	// of the variable
	// together with its value. The example below should eventually output a hashmap
	// that contains {(mode, ACUmode), (bmsAlive, T/F), (ssbAlive, T/F),
	// (srvRegenAlive, T/F),
	// (inverter, InverterType)}. This hashmap can then be easily interpreted on the
	// front-end.

	// Note that the current version of the file contains hardcoded examples to
	// translate. Eventually we need to be able to translate the CAN message using
	// only the CAN message itself.

	// The actual message we'll eventually need to parse
	static String testmsg = "(1600453413.322000) canx 12d#01c90100a819d400";

	// Data part of 'testmsg' (01c90100a819d400) converted to binary: 0001 11001001
	// 00000001 00000000 10101000 00011001 11010100 00000000

	// These are strings that we will eventually need to deduce using the .csv files
	// and the id of string 'testmsg'. The data types correspond to the following
	// variables:
	// mode, bmsAlive, ssbAlive, srvRegenAlive, esbAlive, inverter
	String msgdatatypes = "ACUMode, bool: 1, bool: 1, bool: 1, bool: 1, InverterType";

	// string msgdatatypes has ACUMode, booleans, and an InverterType. We will
	// eventually need to deduce what these enums are
	// by using the file 'CAN_typedef_2019_17-format.csv' file.
	static String ACUdef = "enum ACUMode:uint8_t{ACUModeRDW,ACUModeWSC,DARE}";
	static String InvertDef = "enum InverterType:uint8_t{InverterTypeUnknown,Tritium,NLE}";

	/**
	 * Create a Hashmap for each Enum which contains the bitsequence for that
	 * specific state of an enum.
	 *
	 * This bitsequence/state pair is a key/value pair in the inner HashMap. The key
	 * of the outer HashMap is the name of the enum
	 * class. Together with the inner Hashmap we then get the following form:
	 *
	 * HashMap<String EnumName, HashMap<String Bitsequence, String StateName>>
	 *
	 * A concrete example of a single entry is as follows:
	 * <"AcuMode", <"00000010", "DARE">>
	 *
	 * @return A HashMap containing the Enum classname as the key, and another
	 *         Hashmap containing a String denoting
	 *         the bitsequence as the key and a String denoting the statename as the
	 *         value
	 */
	public static HashMap<String, HashMap<String, String>> parseTypedef() {

		HashMap<String, HashMap<String, String>> parsedEnums = new HashMap<>();

		// First we make use of the CANParser class to read in the default typedefs.csv
		// file and extract all the fields per typedef
		CANParser cp = new CANParser();

		// Store all the parsed typedefs in a list of ParsedTypedef objects
		List<TypedefObject> lpdf = cp.parseTypedefsDefault();

		// Now iterate through the list of parsed typedefs and build the Hashmap by
		// getting the name of the enum and its codestates.
		// Assign the corresponding bitsequence to it and store it in the Hashmap
		for (TypedefObject pdf : lpdf) {
			// Build the Hashmap<String, String> first (e.g. the value of a parsed enum)
			HashMap<String, String> bitsequenceStateMap = new HashMap<>();

			// Get all the codestates this Enum class can take
			String[] codeStates = pdf.getCodeStates();

			// We're going to check whether a byte sequence is already defined in the
			// typedefs file. If so, use that value.
			// Else we assign our own values
			boolean isPredefined = false;

			for (String s : codeStates) {
				// We have a hit, this state is predefined
				if (s.contains("=")) {

					isPredefined = true;

					// Extract the predefined state by splitting on the "=" sign
					String[] predefinedState = s.split("=");

					// Remove any trailing spaces and save the predefined name and its value
					String name = predefinedState[0].trim();
					String value = predefinedState[1].trim();

					// We now have all the predefined states in an array, check whether this is a
					// hex or integer
					if (value.contains("0x")) {
						// This is a hex, parse it to an integer
						int i = Integer.decode(value);

						// Add the String bitsequence, String StateName pair to the inner HashMap
						bitsequenceStateMap.put(String.format("%08d", Integer.parseInt(Integer.toString(i, 2))), name);
					} else {
						// This is an int, parse it to a Byte
						int i = Integer.parseInt(value);

						bitsequenceStateMap.put(String.format("%08d", Integer.parseInt(Integer.toString(i, 2))), name);
					}
				}
			}

			// TODO: Make it so that we don't have to hardcode these states
			// There are 3 enums that have only a single (the first) state predefined as
			// "0". Assign bitsequences to the rest of the states
			if (isPredefined) {
				String name = pdf.getName();
				if (name.equals("KeyStatus") || name.equals("LVCState") || name.equals("VehicleState")) {
					for (int i = 1; i < pdf.getCodeStates().length; i++) {
						bitsequenceStateMap.put(String.format("%08d", Integer.parseInt(Integer.toString(i, 2))),
								pdf.getCodeStates()[i]);
					}
				}
			}

			// No predefined states were found for this Enum, continue with the basic
			// assignment of bitsequences, starting with 0
			if (!isPredefined) {
				for (int i = 0; i < pdf.getCodeStates().length; i++) {
					bitsequenceStateMap.put(String.format("%08d", Integer.parseInt(Integer.toString(i, 2))),
							pdf.getCodeStates()[i]);
				}
			}

			// Now that we have mapped all the states to a bitsequence, set this as the key
			// in the inner hashmap and use the statename as the value
			parsedEnums.put(pdf.getName(), bitsequenceStateMap);
		}

		return parsedEnums;
	}

	// Converts a hexadecimal strong to a (unpadded) binary string. Used in
	// parseDataString()
	static String hexToBin(String s) {
		return new BigInteger(s, 16).toString(2);
	}

	// Converts a CAN message "1600453413322000#0000012d#01c90100a819d400" (which
	// is in hexadecimal notation and contains 8 bytes) to a String:
	// "00000001 11001001 00000001 00000000 10101000 00011001 11010100 00000000".
	// I'm not sure yet whether a list or string is more convenient, so we use both.
	public static String parseDataString(String CANMessage) {
		String result;

		String[] split0 = CANMessage.split("#"); // split0[1] = "01c90100a819d400"

		// int data = Integer.parseInt(split0[1], 16);
		// result = String.valueOf(data);
		// result = Integer.toBinaryString(Integer.parseInt(split0[1], 16));
		result = hexToBin(split0[2]);
		result = String.format("%064d", new BigInteger(result));

		return result;
	}

	// takes a CAN message "1600453413322000#0000012d#01c90100a819d400" and
	// returns the string id. The id is represented by the characters between the
	// #'s. In this example the id is denoted by 12d. Note: this is in hexadecimal.
	// The id in this case is equal to 301.
	public static int parseID(String CANMessage) {
		String[] split = CANMessage.split("#"); // split1[1] = "12d"
		String idInHex = split[1]; // take the 'middle' after splitting
		int ID = Integer.parseInt(idInHex, 16);

		return ID;
	}

	public static String convertToDate(String seconds) {
		long time1 = Long.valueOf(seconds).longValue();
		// long time1 = Long.valueOf(split2[0]).longValue();
		// long time2 = Long.valueOf(split2[1]).longValue();
		String result = new java.text.SimpleDateFormat("MM/dd/yyyy HH:mm:ss").format(new java.util.Date(time1 / 1000));
		return result;
	}

	// Takes a CAN message "1600453413322000#0000012d#01c90100a819d400" and
	// returns the timestamp associated with it.
	// The timestamp is the first part before the # in ms
	public static String parseTimestamp(String CANMessage) {
		String[] split = CANMessage.split("#");
		String millis = split[0];

		return millis;
	}

	/**
	 * Use the ID of a message to figure out the data types and their respective
	 * field names.
	 *
	 * Store the data types and the names in separate lists, but at the same index.
	 * This way a field name at index 3
	 * will correspond to a data type at index 3.
	 * The 0th index in each list will always be "String" and "timestamp" for the
	 * data type list and field name
	 * The first list is a singleton list containing just the signal name.
	 *
	 * @param id the ID for which the parseOverview will be created
	 * @return A List of Lists with Strings.
	 *         - The first list is a singleton list containing just the signal name.
	 *         - The second list is the list of data types
	 *         -.The third list is the list of field names.
	 *         - The fourth list is a singleton list containing just the endianness
	 *         value.
	 */
	public static List<List<String>> parseOverview(int id) {

		// Parse messages.csv and store all the messages in a list
		List<MessageObject> messageList = CANParser.getMessagesDefault();

		// Store the message with the current ID in this variable
		MessageObject message = null;

		// TODO: error handling when a message is null
		// Search the list of message for the message that has the correct ID
		for (MessageObject mo : messageList) {
			if (Integer.parseInt(mo.getId()) == id) {
				// Found the message with the correct ID, store it in the variable and stop
				// searching
				message = mo;
				break;
			}
		}

		if (message == null) {
			return null; // No message with specified ID could be found; ignore
		}

		// Now that we have the relevant message, extract the necessary data such as the
		// name and the data types
		String messageName = message.getName();
		String[] messageDataTypes = message.getDataTypes();
		String[] messageFieldNames = message.getFieldNames();

		// Instantiate the two lists that we'll be filling with relevant data for the
		// overview
		List<String> listOfDataTypes = new ArrayList<>();
		List<String> listOfFieldNames = new ArrayList<>();

		// Populate the lists with data. For index 0 we always want to have the
		// timestamp in both lists.
		// We make the assumption that messageDataTypes.length ==
		// messageFieldNames.length
		// TODO: add tests to confirm this assumption
		listOfDataTypes.add("String");
		listOfFieldNames.add("timestamp");

		for (int i = 0; i < messageDataTypes.length; i++) {
			listOfDataTypes.add(messageDataTypes[i].trim());
			listOfFieldNames.add(messageFieldNames[i].trim());
		}

		// Now we have all the data into either List of Strings or just a String.
		// Aggregate this data in a List.
		// The structure is as follows: [List<String> messageName, List<String>
		// dataTypes, List<String> fieldNames, List<String> endianness]
		List<List<String>> completeList = new ArrayList<>();
		completeList.add(Collections.singletonList(messageName));
		completeList.add(listOfDataTypes);
		completeList.add(listOfFieldNames);

		// Add the Endianness as the fourth, singleton list
		completeList.add(Collections.singletonList(message.getConvertEndianness()));

		return completeList;
	}

	/*
	 * 
	 * We can determine which bits belong to which data as this has a structured
	 * order.
	 * We will create a hashmap that links these as follows:
	 * {(ACUMode mode, 00000001), (bool bmsAlive, 1), (bool ssbAlive, 0),
	 * (bool srvRegenAlive, 1), (bool esbAlive, 1), (InverterType inverter,
	 * 00000110)}
	 * 
	 * Note that we will probably save the timestamp and pass it along as a separate
	 * variable,
	 * only to be added to the final result at the end. It would otherwise clutter
	 * the hashmap.
	 * 
	 * As for the parameters:
	 * l1: (String, ACUMode, bool, bool, bool, bool, InverterType)
	 * l2: (timestamp, mode, bmsAlive, ssbAlive, srvRegenAlive, esbAlive, inverter).
	 * endianness: if endianness is >= 1 the byte order is different. Each signal
	 * has a specific integer denoted to endianness, and this should thus be checked
	 * in messages.csv.
	 * 
	 * Lastly, all available data types are:
	 * bool, bool:1, uint8_t, uint8_t:2, uint8_t:6, uint16_t, uint16_t:11, uint32_t,
	 * uint64_t, uint64_t:48, int8_t, int16_t, int32_t, int64_t, float,
	 * and OTHER (where OTHER is always an enum).
	 * 
	 * small note for a potential STE member looking for bugs in the future:
	 * We haven't been able to fully test how ints of type uint8_t:2 or uint64_t:48
	 * work because no examples
	 * of these data types were provided to us by the STE team. Should you
	 * experience issues with these
	 * data types I would suggest looking into their cases in this function below.
	 */

	public static List<String> determineBits(List<String> l1, List<String> l2, String dataBytes, int endianness) {

		List<String> result = new ArrayList<String>();

		for (int i = 0; i < l1.size(); i++) {
			String dataType = l1.get(i);
			dataType = dataType.replaceAll("\\s+", ""); // make "bool: 1" and "bool:1" equivalent
			dataType = dataType.replace("u", ""); // make "uints" equivalent to "ints"

			switch (dataType) {

				case "String": // String is only used for timestamp, and the timestamp itself isn't contained
								// within the 8 bytes. We can already translate this and it doesnt matter where
								// we do it, so we simply do it here.
					result.add(parseTimestamp(testmsg)); // TO DO: make sure that rather than 'testmsg' it uses the
															// actual CANmessage that is being tranlated dynamically.
					break;

				case "bool": // 8 bits
					result.add(dataBytes.substring(0, 8)); // add bits to list3 (see overview on drive for a description
															// of list3)
					dataBytes = dataBytes.substring(8); // remove the byte from the overall String of bytes.
					break;

				case "bool:1": // 1 bit
					// We need to check whether there are multiple "bool:1"s in a row. If there are
					// we need to look within the same byte to find the correct corresponding bit.
					int count = 0;
					for (int j = i + 1; j < l1.size(); j++) {
						if (l1.get(j).replaceAll("\\s+", "").equals("bool:1")) {
							count++;
						} else {
							j = l1.size();
						}
					}

					if (count == 0) { // No consecutive "bool:1"s, thus only the least significant bit of the
										// left-most byte is relevant.
						result.add(dataBytes.substring(7, 8));
						dataBytes = dataBytes.substring(8);

					} else { // multiple consecutive "bool:1"s
						int beginIndex = 7;
						int endIndex = 8;
						for (int k = 0; k < count + 1; k++) {
							result.add(dataBytes.substring(beginIndex, endIndex));
							beginIndex--;
							endIndex--;
							i++; // we are moving ahead in l1, thus i also needs to increase.

							if (beginIndex < 0) { // we've processed a full byte, and thus should move onto the next
													// byte.
								dataBytes = dataBytes.substring(8);
								beginIndex = 7;
								endIndex = 8;
							}
						}
						i--; // we've done i++ one too many times for consecutive booleans, as the for loop
								// itself will also increment i at the end. We will thus decrement
								// it once.

						// we have to add this if-statement, because ONLY when the next data type is of
						// the form 'uint8_t: 2', we cannot move onto the next
						// byte. Thus we have to check whether this is the case.
						if (!(i == l1.size() - 1)) { // causes index error if we're at the end of the list without this
														// if-statement
							if ((((l1.get(i + 1)).replaceAll("\\s+", "")).equals("uint8_t:2")
									|| ((l1.get(i + 1)).replaceAll("\\s+", "")).equals("uint8_t:6")
									|| ((l1.get(i + 1)).replaceAll("\\s+", "")).equals("uint16_t:11")
									|| ((l1.get(i + 1)).replaceAll("\\s+", "")).equals("uint64_t:48"))) {
								// say count == 11, thus we have 12 booleans in a row. of string
								// 0011001100110011 we'd want to remove the
								// right-most 12 bits. In this case we remove the bits 001100110011 s.t. we keep
								// bits 0011.
								String split0 = dataBytes.substring(0, (8 - ((count + 1) % 8)));
								String split1 = dataBytes.substring(8);
								dataBytes = split0.concat(split1);
							} else {
								dataBytes = dataBytes.substring(8);
							}
						}
					}

					break;

				case "int8_t":
					result.add(dataBytes.substring(0, 8));
					dataBytes = dataBytes.substring(8);
					break;

				case "int8_t:2":
					result.add(dataBytes.substring(0, 2));
					dataBytes = dataBytes.substring(2);
					break;

				case "int8_t:6":
					result.add(dataBytes.substring(0, 6));
					dataBytes = dataBytes.substring(6);
					break;

				case "int16_t":
					if (endianness > 0) {
						result.add(dataBytes.substring(0, 16));
						dataBytes = dataBytes.substring(16);
					} else {
						String temp = dataBytes.substring(8, 16);
						temp = temp.concat(dataBytes.substring(0, 8));
						result.add(temp);

						dataBytes = dataBytes.substring(16);
					}
					break;

				case "int16_t:11":
					if (endianness > 0) {
						result.add(dataBytes.substring(0, 11));
					} else {
						String temp = dataBytes.substring(3, 11);
						temp = temp.concat(dataBytes.substring(0, 3));
						result.add(temp);
					}
					dataBytes = dataBytes.substring(11);
					break;

				case "int32_t":
					if (endianness > 0) {
						result.add(dataBytes.substring(0, 32));
						dataBytes = dataBytes.substring(32);
					} else {
						String temp = dataBytes.substring(24, 32);
						temp = temp.concat(dataBytes.substring(16, 24));
						temp = temp.concat(dataBytes.substring(8, 16));
						temp = temp.concat(dataBytes.substring(0, 8));
						result.add(temp);
					}
					break;

				case "int64_t":
					if (endianness > 0) {
						result.add(dataBytes);// is the only variable in the data, so string dataBytes does not need to
												// be trimmed
					} else {
						String temp = dataBytes.substring(56, 64);
						temp = temp.concat(dataBytes.substring(48, 56));
						temp = temp.concat(dataBytes.substring(40, 48));
						temp = temp.concat(dataBytes.substring(32, 40));
						temp = temp.concat(dataBytes.substring(24, 32));
						temp = temp.concat(dataBytes.substring(16, 24));
						temp = temp.concat(dataBytes.substring(8, 16));
						temp = temp.concat(dataBytes.substring(0, 8));
						result.add(temp);
					}
					break;

				case "int64_t:48":
					if (endianness > 0) {
						result.add(dataBytes.substring(0, 48));
					} else {
						String temp = dataBytes.substring(40, 48);
						temp = temp.concat(dataBytes.substring(32, 40));
						temp = temp.concat(dataBytes.substring(24, 32));
						temp = temp.concat(dataBytes.substring(16, 24));
						temp = temp.concat(dataBytes.substring(8, 16));
						result.add(temp);
					}
					dataBytes = dataBytes.substring(48);
					break;

				case "float":
					// for an unknown reason the endianness-logic works the opposite way for floats,
					// so we add a '!' to the if clause
					if (!(endianness > 0)) {
						result.add(dataBytes.substring(0, 32));
					} else {
						String temp = dataBytes.substring(24, 32);
						temp = temp.concat(dataBytes.substring(16, 24));
						temp = temp.concat(dataBytes.substring(8, 16));
						temp = temp.concat(dataBytes.substring(0, 8));
						result.add(temp);
					}
					dataBytes = dataBytes.substring(32);
					break;

				default:
					result.add(dataBytes.substring(0, 8));
					dataBytes = dataBytes.substring(8);
					break;
			}
		}
		return result;
	}

	/**
	 * Iterate over a List of Strings and filter out all unique datatypes that are
	 * present in this List
	 *
	 * Print all the unique datatypes that remain from this list.
	 */
	public void printUniqueTypes(List<String> listOfTypes) {
		// Do a conversion using a Set, since this collection prohibits duplicates it
		// will do the filtering for us
		List<String> uniqueList = new ArrayList<>(new HashSet<>(listOfTypes));

		// Now that we have a list of unique Strings, print them out
		System.out.println("The following unique Strings were found in this List: ");
		for (String s : uniqueList) {
			System.out.println("- " + s);
		}
		System.out.println();
	}

	/**
	 * Print out all the unique datatypes that are present in the messages.csv file.
	 *
	 * Requested by Mathijs Moonen
	 */
	public void printUniqueMessageTypes() {
		// Set up a CANParser object and parse the messages.csv file
		List<MessageObject> messageList = CANParser.getMessagesDefault();

		// Collect *all* the datatypes in an ArrayList. We'll pass this list to the
		// printUniqueTypes function
		List<String> allTypes = new ArrayList<>();

		// Iterate over all the messages in the list and add their data types to an
		// ArrayList
		for (MessageObject mo : messageList) {

			// Get the message types from this message object
			String[] messageTypes = mo.getDataTypes();

			// Add the message types for this message to the Arraylist
			for (String mt : messageTypes) {
				// Exclude the value of predefined types, E.g. "bool: 1"
				allTypes.add(mt.trim());
			}
		}

		// We've collected all the message from all the message present in the Message
		// list. Now print out the unique
		// messages
		printUniqueTypes(allTypes);
	}

	private static HashMap<String, HashMap<String, String>> enums = parseTypedef(); // the hashmap of enums with their
																					// corresponding byte sequence. See
																					// overview on the drive for
																					// details.

	/*
	 * Finally, we use the hashmap created by determineBits() to determine what
	 * state the bytesequence
	 * on the left partains. The integers and booleans all have set bit/byte
	 * sequences (see top of this java file),
	 * so those can be translated directly. The result should look as follows:
	 * 
	 * {
	 * "timestamp": "(1600453413.322000)"
	 * "name": "ACU_KeepAlive",
	 * "fields": [
	 * "ACU_Mode mode": "ACUModeRDW",
	 * "bool bmsAlive": true,
	 * "bool ssbAlive": true,
	 * "bool srvRegenAlive": true,
	 * "bool esbAlive": true,
	 * "InverterType inverter": "Tritium"]
	 * }
	 * 
	 * This is a specific format that allows for easier sending, receiving and
	 * parsing.
	 * 
	 * As for what the lists are, please refer to the overview on google drive.
	 */
	public static List<Object> determineConcreteData(List<String> l1, List<String> l2, List<String> l3,
			String timestamp) {

		List<Object> result = new ArrayList<>();

		for (int i = 0; i < l1.size(); i++) {
			String dataType = l1.get(i);
			String bytes = l3.get(i);

			switch (dataType.replaceAll(" ", "")) {

				case "String":// timestamp
					result.add(timestamp);
					break;

				case "bool": // 8 bits
					if (bytes.equals("00000000")) {
						result.add(false);
					} else {
						result.add(true);
					}
					break;

				case "bool:1": // 1 bit
					if (bytes.equals("0")) {
						result.add(false);
					} else {
						result.add(true);
					}
					break;

				case "uint8_t":
					result.add(Integer.parseInt(bytes, 2));
					break;

				case "uint8_t:2":
					result.add(Integer.parseInt(bytes, 2));
					break;

				case "uint8_t:6":
					result.add(Integer.parseInt(bytes, 2));
					break;

				case "uint16_t":
					result.add(Integer.parseInt(bytes, 2));
					break;

				case "uint16_t:11":
					result.add(Integer.parseInt(bytes, 2));
					break;

				case "uint32_t":
					result.add(Integer.parseInt(bytes, 2));
					break;

				case "uint64_t":
					result.add(Long.parseLong(bytes, 2));
					break;

				case "uint64_t:48":
					result.add(Long.parseLong(bytes, 2));
					break;

				case "int8_t":
					result.add(getInteger(bytes));
					break;

				case "int16_t":
					result.add(getInteger(bytes));
					break;

				case "int32_t":
					result.add(getInteger(bytes));
					break;

				case "int64_t":
					String int64Value = ""; // final value to be calculated. Needs to be done in two steps: 1) determine
					// if the value is negative or positive 2) determine value

					if (bytes.substring(0, 1).equals("1")) { // if the first bit is a '1' the value is negative.
						int64Value = "-";
					}

					int64Value = int64Value.concat(bytes.substring(1));

					result.add(Long.parseLong(int64Value, 2));
					break;

				case "float":
					String floatStringValue = "";

					if (bytes.substring(0, 1).equals("1")) {
						floatStringValue = "-";
					}

					floatStringValue = floatStringValue.concat(bytes.substring(1));

					int intBits = Integer.parseInt(floatStringValue, 2);

					result.add(Float.intBitsToFloat(intBits));
					break;

				default: // None of the other cases, thus it must be an enum.
					HashMap<String, String> options = enums.get(dataType);
					if (options == null) {
						// do nothing
						break;
					}
					String enumValue = options.get(bytes);
					result.add(enumValue);
					break;
			}
		}
		return result;
	}

	public static int getInteger(String bytes) {
		String intval = ""; // final value to be calculated. Needs to be done in two steps: 1) determine
							// if the value is negative or positive 2) determine value

		if (bytes.substring(0, 1).equals("1")) { // if the first bit is a '1' the value is negative.
			intval = "-";
		}

		intval = intval.concat(bytes.substring(1));

		return Integer.parseInt(intval, 2);
	}

	public static void main(String[] args) {
		String bytes = "00000000101000000000111101000011";
		int intBits = Integer.parseInt(bytes, 2);
		Float floatValue = Float.intBitsToFloat(intBits);
		String floatStringValue = floatValue.toString();

		System.out.println(floatStringValue);
	}

}
