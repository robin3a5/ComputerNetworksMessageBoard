package ClientFiles;

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

    private static final Pattern PATTERN = Pattern.compile(
            "^(([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\.){3}([01]?\\d\\d?|2[0-4]\\d|25[0-5])$");

    public static void main(String argv[]) throws Exception {
        printWelcomeText();
        reader = new Scanner(System.in);
        while (!isDisconnected) {
            enterCommandMessage();
            parseCommand(reader.nextLine().trim());
        }
        reader.close();
        if (isConnected) {
            socketControls.closeSocketConnection(socketControls.controlSocket);
        }
    }

    public static void parseCommand(String commandString) throws UnknownHostException, IOException {
        if (commandString.length() == 0) {
            System.out.println("Please enter a valid command!");
        } else if (commandString.startsWith("/")) {
            commandString = commandString.toLowerCase();
            if (commandString.equals("/connect")) {
                if (!isConnected) {
                    String iPString = "";
                    String portString = "";
                    try {
                        System.out.println("Please enter an IP address:");
                        iPString = reader.nextLine().trim();
                        if (validateIP(iPString.trim())) {
                            System.out.println("Please enter a port number:");
                            portString = reader.nextLine().trim();
                            int portNumber = Integer.parseInt(portString.trim());
                            connectToBoard(iPString, portNumber);
                        } else {
                            System.out.println(String.format(
                                    "Your IP address: %s, was not a valid IP. Please enter a valid one:",
                                    iPString));
                        }
                    } catch (NumberFormatException e) {
                        System.out.println(String.format(
                                "Your port number: %s, was not a valid port number. Please enter a valid one:",
                                portString));
                    }
                } else {
                    System.out.println("You're already connected!");
                    enterCommandMessage();
                }
            } else if (commandString.equals("/groups")) {
                requestGroupList();
            } else if (commandString.equals("/grouppost")) {
                System.out.println("Please enter a group name or group id:");
                String groupString = reader.nextLine().trim();
                if (validateGroup(groupString)) {
                    System.out.println("Please enter a subject:");
                    String subjectString = reader.nextLine().trim();
                    System.out.println("Please enter a body:");
                    String bodyString = reader.nextLine().trim();
                    postGroupMessage(subjectString, bodyString, groupString);
                } else {
                    groupErrorMessage(groupString);
                }
            } else if (commandString.equals("/groupusers")) {
                System.out.println("Please enter a group name or group id:");
                String groupString = reader.nextLine().trim();
                if (validateGroup(groupString)) {
                    requestGroupUsers(groupString);
                } else {
                    groupErrorMessage(groupString);
                }
            } else if (commandString.equals("/groupmessage")) {
                System.out.println("Please enter a group name or group id:");
                String groupString = reader.nextLine().trim();
                String messageIDString = "";
                if (validateGroup(groupString)) {
                    System.out.println("Please enter a message id:");
                    messageIDString = reader.nextLine().trim();
                    try {
                        int messageID = Integer.parseInt(messageIDString);
                        retrieveGroupMessage(messageID, groupString);
                    } catch (NumberFormatException e) {
                        System.out.println(String.format(
                                "Your message ID: %s, was not a valid ID. Please enter a valid one:",
                                messageIDString));
                    }
                } else {
                    groupErrorMessage(groupString);
                }
            } else if (commandString.equals("/groupjoin")) {
                System.out.println("Please enter a group name or group id:");
                String groupString = reader.nextLine().trim();
                if (validateGroup(groupString)) {
                    joinPrivateGroup(groupString);
                } else {
                    groupErrorMessage(groupString);
                }
            } else if (commandString.equals("/groupleave")) {
                System.out.println("Please enter a group name or group id:");
                String groupString = reader.nextLine().trim();
                if (validateGroup(groupString)) {
                    leavePrivateGroup(groupString);
                } else {
                    groupErrorMessage(groupString);
                }
            } else if (commandString.equals("/join")) {
                joinGroup();
            } else if (commandString.equals("/post")) {
                System.out.println("Please enter a subject:");
                String subjectString = reader.nextLine().trim();
                System.out.println("Please enter a body:");
                String bodyString = reader.nextLine().trim();
                postMessage(subjectString, bodyString);
            } else if (commandString.equals("/message")) {
                String messageIdString = "";
                try {
                    System.out.println("Please enter a message id:");
                    messageIdString = reader.nextLine().trim();
                    int messageID = Integer.parseInt(messageIdString);
                    retrieveMessage(messageID);
                } catch (NumberFormatException e) {
                    System.out.println(String.format(
                            "Your message ID: %s, was not a valid ID. Please enter a valid one:",
                            messageIdString));
                }
            } else if (commandString.equals("/users")) {
                requestUserList();
            } else if (commandString.equals("/help")) {
                printCommandList();
            } else if (commandString.equals("/leave")) {
                leaveGroup();
            } else if (commandString.equals("/exit")) {
                disconnect();
            } else {
                System.out.println(String.format(
                        "Your command: %s, does not match any valid commands. Please enter a valid one:",
                        commandString));
            }
        } else {
            System.out.println("Commands must start with a /");
        }
    }

    private static void postGroupMessage(String subject, String body,
            String groupString) throws IOException {
        if (isConnected) {
            String[] valueStrings = { body, subject, Integer.toString(socketControls.userID), groupString };
            socketControls.writeToSocket("grouppost", valueStrings);
        } else {
            notConnectedMessage();
        }
    }

    private static void retrieveGroupMessage(int messageID, String groupString) {
        if (isConnected) {

            String[] valueStrings = { String.valueOf(messageID), groupString };
            try {
                socketControls.writeToSocket("groupmessage", valueStrings);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            notConnectedMessage();
        }
    }

    private static void joinPrivateGroup(String groupString) {
        if (isConnected) {
            String[] valueStrings = { Integer.toString(socketControls.userID), groupString, groupString };
            try {
                socketControls.writeToSocket("groupjoin", valueStrings);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            notConnectedMessage();
        }
    }

    private static void requestGroupUsers(String groupString) {
        if (isConnected) {

            String[] valueStrings = { groupString };
            try {
                socketControls.writeToSocket("groupusers", valueStrings);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            notConnectedMessage();
        }
    }

    private static void leavePrivateGroup(String groupString) {
        if (isConnected) {
            String[] valueStrings = { Integer.toString(socketControls.userID), groupString, groupString };
            try {
                socketControls.writeToSocket("groupleave", valueStrings);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            notConnectedMessage();
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
        System.out.println("/connect\n");
        System.out.println("/join\n");
        System.out.println("/post\n");
        System.out.println("/users\n");
        System.out.println("/groups\n");
        System.out.println("/message\n");
        System.out.println("/leave\n");
        System.out.println("/grouppost\n");
        System.out.println("/groupusers\n");
        System.out.println("/groupmessage\n");
        System.out.println("/groupjoin\n");
        System.out.println("/groupleave\n");
        System.out.println("/exit\n");
        System.out.println("Or type /help to see these commands again\n");
    }

    private static void postMessage(String subject, String body) throws IOException {
        if (isConnected) {
            String[] valueStrings = { body, subject, Integer.toString(socketControls.userID), };
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
            String[] valueStrings = { Integer.toString(socketControls.userID) };
            try {
                socketControls.writeToSocket("leave", valueStrings);
            } catch (IOException e) {

                e.printStackTrace();
            }
        } else {
            notConnectedMessage();
        }
    }

    private static void requestUserList() {
        if (isConnected) {
            String[] valueStrings = {};
            try {
                socketControls.writeToSocket("users", valueStrings);
            } catch (IOException e) {

                e.printStackTrace();
            }
        } else {
            notConnectedMessage();
        }
    }

    private static void requestGroupList() {
        if (isConnected) {
            socketControls.groupMap.forEach((k, v) -> {
                System.out.println(k + " " + v);
            });
        } else {
            notConnectedMessage();
        }
    }

    private static void joinGroup() {
        if (isConnected) {
            String[] valueStrings = { Integer.toString(socketControls.userID) };
            try {
                socketControls.writeToSocket("join", valueStrings);
            } catch (IOException e) {

                e.printStackTrace();
            }
        } else {
            notConnectedMessage();
        }
    }

    private static void connectToBoard(String ipAddr, int portNumber) throws UnknownHostException, IOException {
        if (!isConnected) {
            System.out.println("Please input a username:");
            String username = reader.nextLine().trim();
            socketControls.createSocketConnection(ipAddr, portNumber, username);
            while (!isConnected) {
                if (socketControls.readSocketResponse()) {
                    isConnected = true;
                } else {
                    retryUsernameConnection();
                }
            }
        }
    }

    private static void retryUsernameConnection() {
        System.out.println("Sorry username was taken! Please enter another!");
        String username = reader.nextLine().trim();
        try {
            String[] valueStrings = { username };
            socketControls.writeToSocket("connect", valueStrings);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private static void retrieveMessage(int messageID) {
        if (isConnected) {
            String[] valueStrings = { String.valueOf(messageID) };
            try {
                socketControls.writeToSocket("message", valueStrings);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            notConnectedMessage();
        }
    }

    private static void notConnectedMessage() {
        System.out.println("You must first connect to a board to use this command!");
        enterCommandMessage();
    }

    private static void enterCommandMessage() {
        System.out.println("Enter a command: ");
    }

    private static void groupErrorMessage(String groupString) {
        System.out.println("The group you entered, " + groupString
                + ", does not correspond to a recognized group please enter a valid one");
    }

    private static boolean validateGroup(String groupString) {
        return socketControls.groupMap.containsKey(groupString)
                || socketControls.groupMap.containsValue(groupString);
    }
}

final class SocketReaderThread extends Thread {
    private Socket socketConnection;
    BufferedReader controlReader = null;
    DataOutputStream controlWriter = null;
    InputStream is;
    private ResponseWindow responseWindow;

    public SocketReaderThread(Socket socket) {
        socketConnection = socket;
    }

    @Override
    public void run() {
        try {
            is = this.socketConnection.getInputStream();
            controlWriter = new DataOutputStream(this.socketConnection.getOutputStream());
            controlReader = new BufferedReader(new InputStreamReader(is));
            responseWindow = new ResponseWindow();
        } catch (IOException e) {
            e.printStackTrace();
        }
        while (!socketConnection.isClosed()) {
            try {
                while (controlReader.ready()) {
                    readSocketResponse();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void readSocketResponse() throws IOException {
        String requestLine = controlReader.readLine();
        JSONObject jsonReturn = new JSONObject(requestLine);
        String commandString = (String) jsonReturn.get("command");
        if ((boolean) jsonReturn.get("success")) {
            // Now check value
            Object valueObject = jsonReturn.get("value");
            // Now have object containing value
            if (commandString.equals("users")) {
                handleUsersResponse(valueObject);
            }
            if (commandString.equals("post")) {
                handlePostResponse(valueObject);
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
            if (commandString.equals("leave")) {
                handleLeaveResponse(valueObject);
            }
        } else {
            System.out.println("Sorry something went wrong. Please try again");
        }
    }

    private void handleLeaveResponse(Object valueObject) {
        // TODO: Need false condition checks on all of these
        JSONObject valueJson = new JSONObject(valueObject.toString());
        String message = (String) valueJson.get("message");
        responseWindow.updateResponse(message);
    }

    private void handleUsersResponse(Object valueObject) {
        JSONObject valueJson = new JSONObject(valueObject.toString());
        JSONArray jsonTest = (JSONArray) valueJson.get("users");
        responseWindow.updateResponse("User List:");
        jsonTest.forEach(user -> {
            responseWindow.updateResponse(user.toString());
        });
    }

    private void handleMessageResponse(Object valueObject) {
        JSONObject valueJson = new JSONObject(valueObject.toString());
        String message = (String) valueJson.get("message");
        responseWindow.updateResponse("Message body: " + message);
    }

    private void handlePostResponse(Object valueObject) {
        JSONObject valueJson = new JSONObject(valueObject.toString());
        String messageID = (String) valueJson.get("MessageID");
        String sender = (String) valueJson.get("Sender");
        String date = (String) valueJson.get("Date");
        String subject = (String) valueJson.get("Subject");
        responseWindow.updateResponse(date + " - " + sender + ": " + subject + " (" + messageID + ")");

    }

    private void handleJoinResponse(Object valueObject) {
        JSONObject valueJson = new JSONObject(valueObject.toString());
        String message = (String) valueJson.get("message");
        responseWindow.updateResponse(message);
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
    Map<String, String> groupMap = new HashMap<>();

    public void createSocketConnection(String address, int portNumber, String username)
            throws UnknownHostException, IOException {
        controlSocket = new Socket(address, portNumber);
        is = this.controlSocket.getInputStream();
        controlWriter = new DataOutputStream(this.controlSocket.getOutputStream());
        controlReader = new BufferedReader(new InputStreamReader(is));
        String[] valueStrings = { username };
        writeToSocket("connect", valueStrings);
    }

    public void writeToSocket(String commandString, String[] valueStrings) throws IOException {
        JSONObject json = new JSONObject();
        json.put("value", valueStrings);
        json.put("command", commandString);
        controlWriter.writeUTF(json.toString());
    }

    public boolean readSocketResponse() throws IOException {
        String requestLine = controlReader.readLine();
        JSONObject newObject = new JSONObject(requestLine);
        if ((boolean) newObject.get("success")) {
            Object valueObject = newObject.get("value");
            // Now have object containing value
            handleConnectResponse(valueObject);
            return true;
        } else {
            return false;
        }

    }

    private void handleConnectResponse(Object valueObject) {
        JSONObject valueJson = new JSONObject(valueObject.toString());
        userID = Integer.parseInt((String) valueJson.get("ID"));
        groupMap.put("Group1", (String) valueJson.get("Group1"));
        groupMap.put("Group2", (String) valueJson.get("Group2"));
        groupMap.put("Group3", (String) valueJson.get("Group3"));
        groupMap.put("Group4", (String) valueJson.get("Group4"));
        groupMap.put("Group5", (String) valueJson.get("Group5"));

        Thread socketThread = new Thread(new SocketReaderThread(controlSocket));

        // Start the thread.
        socketThread.start();
    }

    public void closeSocketConnection(Socket controlSocket) throws IOException {
        controlSocket.close();
    }

}