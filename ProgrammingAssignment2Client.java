import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.regex.Pattern;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * @author Alexander
 */

public final class ProgrammingAssignment2Client {
    static boolean isConnected = false;
    static boolean isDisconnected = false;
    static SocketControls socketControls = new SocketControls();
    static Scanner reader = new Scanner(System.in);
    static Map<String, String> groupMap = new HashMap<>();

    private static final Pattern PATTERN = Pattern.compile(
            "^(([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\.){3}([01]?\\d\\d?|2[0-4]\\d|25[0-5])$");

    public static void main(String argv[]) throws Exception {
        printWelcomeText();
        reader = new Scanner(System.in);
        System.out.println("Enter a command: ");
        while (!isDisconnected) {
            parseCommand(reader.nextLine());
        }
        reader.close();
        if (isConnected) {
            socketControls.closeSocketConnection(socketControls.controlSocket);
        }
    }

    public static void parseCommand(String commandString) throws UnknownHostException, IOException {
        String[] commandStringParts = commandString.split(" ");
        if (commandStringParts[0].length() == 0) {
            System.out.println("Please enter a valid command!");
        } else if (commandStringParts[0].startsWith("/")) {
            commandStringParts[0] = commandStringParts[0].toLowerCase();
            if (commandStringParts[0].contains("connect")) {
                if (commandStringParts.length < 3) {
                    tooFewArguementsMessage();
                } else {
                    try {
                        if (validateIP(commandStringParts[1])) {
                            int portNumber = Integer.parseInt(commandStringParts[2]);
                            connectToBoard(commandStringParts[1], portNumber);
                        } else {
                            System.out.println(String.format(
                                    "Your IP address: %s, was not a valid IP. Please enter a valid one:",
                                    commandStringParts[2]));
                        }
                    } catch (NumberFormatException e) {
                        System.out.println(String.format(
                                "Your port number: %s, was not a valid port number. Please enter a valid one:",
                                commandStringParts[1]));
                    }
                }
            } else if (commandStringParts[0].contains("join")) {
                joinGroup(commandStringParts[1]);
            } else if (commandStringParts[0].contains("post")) {
                if (commandStringParts.length < 3) {
                    tooFewArguementsMessage();
                } else {
                    postMessage(commandStringParts[1], commandStringParts[2]);
                }
            } else if (commandStringParts[0].contains("users")) {
                requestUserList();
            } else if (commandStringParts[0].contains("groups")) {
                requestGroupList();
            } else if (commandStringParts[0].contains("message")) {
                if (commandStringParts.length < 2) {
                    tooFewArguementsMessage();
                } else {
                    try {
                        int messageID = Integer.parseInt(commandStringParts[1]);
                        retrieveMessage(messageID);
                    } catch (NumberFormatException e) {
                        System.out.println(String.format(
                                "Your message ID: %s, was not a valid ID. Please enter a valid one:",
                                commandStringParts[1]));
                    }
                }
            } else if (commandStringParts[0].contains("leave")) {
                leaveGroup();
            } else if (commandStringParts[0].contains("exit")) {
                disconnect();
            } else if (commandStringParts[0].contains("help")) {
                printCommandList();
            } else {
                System.out.println(String.format(
                        "Your command: %s, does not match any valid commands. Please enter a valid one:",
                        commandStringParts[0]));
            }
        } else {
            System.out.println("Commands must start with a /");
        }
    }

    public static boolean validateIP(final String ip) {
        return PATTERN.matcher(ip).matches();
    }

    private static void printWelcomeText() {
        System.out.println("Welcome to the connection Client!\n");
        System.out.println("You can start by running one of the commands below:");
        printCommandList();
    }

    private static void printCommandList() {
        System.out.println("/connect {IP address} {Port Number}\n");
        System.out.println("/join\n");
        System.out.println("/post {Subject} {Body}\n");
        System.out.println("/users\n");
        System.out.println("/groups\n");
        System.out.println("/message {Message ID}\n");
        System.out.println("/leave\n");
        System.out.println("/exit\n");
        System.out.println("Or type /help to see these commands again\n");
    }

    private static void postMessage(String subject, String body) throws IOException {
        if (isConnected) {
            String[] valueStrings = { subject, body, Integer.toString(socketControls.userID), };
            socketControls.writeToSocket("post", valueStrings);
        } else {
            notConnectedMessage();
        }
    }

    private static void disconnect() {
        isDisconnected = true;
    }

    private static void leaveGroup() {
        if (isConnected) {

            String[] valueStrings = {};
            try {
                socketControls.writeToSocket("leave", valueStrings);
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        } else {
            notConnectedMessage();
        }
    }

    private static void requestUserList() {
        if (isConnected) {
            String[] valueStrings = {Integer.toString(socketControls.userID)};
            try {
                socketControls.writeToSocket("users", valueStrings);
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        } else {
            notConnectedMessage();
        }
    }

    private static void requestGroupList() {
        if (isConnected) {

            String[] valueStrings = {};
            try {
                socketControls.writeToSocket("groups", valueStrings);
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        } else {
            notConnectedMessage();
        }
    }

    private static void joinGroup(String groupString) {
        // TODO: Need to do some group validation
        if (isConnected) {
            String[] valueStrings = { Integer.toString(socketControls.userID), groupString, groupString};
            try {
                socketControls.writeToSocket("join", valueStrings);
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        } else {
            notConnectedMessage();
        }
    }

    private static void connectToBoard(String ipAddr, int portNumber) throws UnknownHostException, IOException {
        if (!isConnected) {
            System.out.println("Please input a username:");
            String username = reader.nextLine();
            socketControls.createSocketConnection(ipAddr, portNumber, username);
            isConnected = true;

        } else {
            System.out.println("You're already connected!\n");
        }
    }

    private static void retrieveMessage(int messageID) {
        if (isConnected) {

            String[] valueStrings = { String.valueOf(messageID) };
            try {
                socketControls.writeToSocket("message", valueStrings);
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        } else {
            notConnectedMessage();
        }
    }

    private static void notConnectedMessage() {
        System.out.println("You must first connect to a board to use this command!");
    }

    private static void tooFewArguementsMessage() {
        System.out.println(
                "Command executed with too few arguments. Please try again with the correct arguments.");
    }

}

final class SocketReaderThread extends Thread {
    private Socket socketConnection;
    BufferedReader controlReader = null;
    DataOutputStream controlWriter = null;
    InputStream is;

    public SocketReaderThread(Socket socket) {
        socketConnection = socket;
    }

    @Override
    public void run() {
        while (socketConnection.isConnected()) {
            try {
                is = this.socketConnection.getInputStream();
                controlWriter = new DataOutputStream(this.socketConnection.getOutputStream());
                controlReader = new BufferedReader(new InputStreamReader(is));
                while (controlReader.ready()) {
                    readSocketResponse();
                    System.out.println("Enter a command: ");
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void readSocketResponse() throws IOException {
        String requestLine = controlReader.readLine();
        JSONObject newObject = new JSONObject(requestLine);
        System.out.println(newObject);
        String commandString = (String) newObject.get("command");
        System.out.println(newObject);
        if ((boolean) newObject.get("success")) {
            System.out.println("Value worked for test");
            // Now check value
            Object valueObject = newObject.get("value");
            System.out.println(valueObject.toString());
            // Now have object containing value
            if (commandString.equals("users")) {
                handleUsersResponse(valueObject);
            }
            if (commandString.equals("post")) {
                handlePostResponse(valueObject);
            }
            if (commandString.equals("groups")) {
                handleGroupsResponse(valueObject);
            }
            if (commandString.equals("message")) {
                handleMessageResponse(valueObject);
            }
            if (commandString.equals("exit")) {
                handleExitResponse(valueObject);
            }
            if (commandString.equals("join")) {
                handleJoinResponse(valueObject);
            }
        } else {
            System.out.println("Sorry something went wrong. Please try again");
        }
    }

    public void handleMessageBroadcast() {
        try {
            // TODO: Finish impl
            String requestLine = controlReader.readLine();
            JSONObject newObject = new JSONObject(requestLine);
            System.out.println(newObject);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    private void handleUsersResponse(Object valueObject) {
        JSONObject valueJson = new JSONObject(valueObject.toString());
        JSONArray jsonTest = (JSONArray) valueJson.get("users");
        System.out.println("User List:");
        jsonTest.forEach(user -> {
            System.out.println(user.toString());
        });
    }

    private void handleGroupsResponse(Object valueObject) {
        JSONObject valueJson = new JSONObject(valueObject.toString());
        JSONArray jsonTest = (JSONArray) valueJson.get("groups");
        System.out.println("Group List:");
        jsonTest.forEach(group -> {
            System.out.println(group.toString());
        });
    }

    private void handleMessageResponse(Object valueObject) {
        JSONObject valueJson = new JSONObject(valueObject.toString());
        String message = (String) valueJson.get("message");
        System.out.println(message);
    }

    private void handlePostResponse(Object valueObject) {
        JSONObject valueJson = new JSONObject(valueObject.toString());
        String messageID = (String) valueJson.get("MessageID");
        String sender = (String) valueJson.get("Sender");
        String date = (String) valueJson.get("Date");
        String subject = (String) valueJson.get("Subject");
        // TODO: Format message
        System.out.println(messageID + sender + date + subject);
    }

    private void handleJoinResponse(Object valueObject) {
        JSONObject valueJson = new JSONObject(valueObject.toString());
        String message = (String) valueJson.get("message");
        System.out.println(message);

        // TODO: Finish impl
    }

    private void handleExitResponse(Object valueObject) {
    }

}

final class SocketControls {
    int port = 0;
    Socket controlSocket = null;
    BufferedReader controlReader = null;
    DataOutputStream controlWriter = null;
    InputStream is;
    int userID = 999;

    public void createSocketConnection(String address, int portNumber, String username)
            throws UnknownHostException, IOException {
        controlSocket = new Socket(address, portNumber);
        is = this.controlSocket.getInputStream();
        controlWriter = new DataOutputStream(this.controlSocket.getOutputStream());
        controlReader = new BufferedReader(new InputStreamReader(is));
        String[] valueStrings = { username };
        writeToSocket("connect", valueStrings);
        readSocketResponse();
    }

    public void writeToSocket(String commandString, String[] valueStrings) throws IOException {
        JSONObject json = new JSONObject();
        json.put("value", valueStrings);
        json.put("command", commandString);
        controlWriter.writeUTF(json.toString());
        System.out.println(json.toString());
    }

    public void readSocketResponse() throws IOException {
        String requestLine = controlReader.readLine();
        JSONObject newObject = new JSONObject(requestLine);
        System.out.println(newObject);
        if ((boolean) newObject.get("success")) {
            Object valueObject = newObject.get("value");
            System.out.println(valueObject.toString());
            // Now have object containing value
            handleConnectResponse(valueObject);
        } else {
            System.out.println("Sorry username was taken! Please try another!");
        }

    }

    private void handleConnectResponse(Object valueObject) {
        JSONObject valueJson = new JSONObject(valueObject.toString());
        userID = Integer.parseInt((String) valueJson.get("ID"));
        // Need to add group stuff here as well
        Thread socketThread = new Thread(new SocketReaderThread(controlSocket));

        // Start the thread.
        socketThread.start();
        System.out.println("Enter a command: ");
    }

    public void closeSocketConnection(Socket controlSocket) throws IOException {
        controlSocket.close();
    }

}