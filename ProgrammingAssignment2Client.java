import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;
import java.util.regex.Pattern;
import org.json.*;

/**
 * @author Alexander
 */

public final class ProgrammingAssignment2Client {
    static boolean isConnected = false;
    static boolean isDisconnected = false;
    static SocketControls socketControls = new SocketControls();

    private static final Pattern PATTERN = Pattern.compile(
            "^(([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\.){3}([01]?\\d\\d?|2[0-4]\\d|25[0-5])$");

    public static void main(String argv[]) throws Exception {
        printWelcomeText();
        String commandString = "";
        Scanner reader = new Scanner(System.in);

        while (!isDisconnected) {
            System.out.println("Enter a command: ");
            parseCommand(reader.nextLine());
            System.out.println(commandString);
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
                joinGroup();
            } else if (commandStringParts[0].contains("post")) {
                if (commandStringParts.length < 3) {
                    tooFewArguementsMessage();
                } else {
                    postMessage(commandStringParts[1], commandStringParts[2]);
                }
            } else if (commandStringParts[0].contains("users")) {
                requestUserList();
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
        System.out.println("/message {Message ID}\n");
        System.out.println("/leave\n");
        System.out.println("/exit\n");
        System.out.println("Or type /help to see these commands again\n");
    }

    private static void postMessage(String subject, String body) throws IOException {
        if (isConnected) {
            System.out.println(" in /post\n");
            String[] valueStrings = { subject, body };
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
            System.out.println("in /leave\n");
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
            System.out.println("in /users\n");
            String[] valueStrings = {};
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

    private static void joinGroup() {
        if (isConnected) {
            System.out.println("in /join\n");
            String[] valueStrings = {};
            try {
                socketControls.writeToSocket("join", valueStrings);
                socketControls.readFromSocket();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        } else {
            notConnectedMessage();
        }
    }

    private static void connectToBoard(String ipAddr, int portNumber) throws UnknownHostException, IOException {
        socketControls.createSocketConnection(ipAddr, portNumber);
        isConnected = true;
        System.out.println("in /connect\n");
    }

    private static void retrieveMessage(int messageID) {
        if (isConnected) {
            System.out.println("in /message\n");
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

final class SocketControls {
    int port = 0;
    Socket controlSocket = null;
    BufferedReader controlReader = null;
    DataOutputStream controlWriter = null;
    String currentResponse;
    InputStream is;

    public void createSocketConnection(String address, int portNumber)
            throws UnknownHostException, IOException {
        controlSocket = new Socket(address, portNumber);
        is = this.controlSocket.getInputStream();
        controlWriter = new DataOutputStream(this.controlSocket.getOutputStream());
        controlReader = new BufferedReader(new InputStreamReader(is));
        String[] valueStrings = { "Username" };
        writeToSocket("connect", valueStrings);
        // String codeLine = controlReader.readLine();
        // String[] lineResult = codeLine.split(" ");
    }

    public void writeToSocket(String commandString, String[] valueStrings) throws IOException {
        JSONObject json = new JSONObject();
        json.put("value", valueStrings);
        json.put("command", commandString);
        controlWriter.writeUTF(json.toString());
        System.out.println(json.toString());
        String requestLine = controlReader.readLine();
        System.out.println(requestLine);
    }

    public void readFromSocket() throws IOException {

    }

    public void closeSocketConnection(Socket controlSocket) throws IOException {
        controlSocket.close();
    }
}

// Add ID to post
// Handle garceful disconnection