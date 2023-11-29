import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

public class App {

    public static HashMap<String, Socket> clientsMap = new HashMap<>();

    public static void handleClient(Socket clientConnection) {
        try {
            System.out.println("Client connected");
            PrintWriter out = new PrintWriter(clientConnection.getOutputStream(), true);
            out.println("Please enter your name : ");
            BufferedReader in = new BufferedReader(new InputStreamReader(clientConnection.getInputStream()));
            String name = in.readLine();
            clientsMap.put(name, clientConnection);
            boolean connected = true;
            while (connected) {
                String receivedString = in.readLine();
                if (receivedString == null) {
                    connected = false;
                    continue;
                }

                // palindrome check

                if (receivedString.toLowerCase().contains("/palindrome")) {
                    String[] words = receivedString.split(" ");
                    String stringToCheck = words[1];
                    String reversedString = new StringBuilder(stringToCheck).reverse().toString();
                    if (stringToCheck.equalsIgnoreCase(reversedString)) {
                        prettyPrintOut("Server", out, "The string is a palindrome");
                    } else {
                        prettyPrintOut("Server", out, "The string is not a palindrome");
                    }
                    continue;
                }

                // client to client messaging "/send <name> <message>"
                if (receivedString.toLowerCase().contains("send")) {
                    String[] words = receivedString.split(" ");
                    String nameToSend = words[1];
                    String message = "";
                    for (int i = 2; i < words.length; i++) {
                        message += words[i] + " ";
                    }
                    Socket socketToSend = clientsMap.get(nameToSend);
                    PrintWriter clientOut = new PrintWriter(socketToSend.getOutputStream(), true);
                    clientOut.println(name + " : " + message);
                    continue;
                }

                // count number of words "/count <message>"
                if (receivedString.toLowerCase().contains("count")) {
                    int length = receivedString.split(" ").length - 1;
                    prettyPrintOut("Server", out, "Number of words in the message : " + length);
                    continue;
                }

                if (receivedString.toLowerCase().contains("/gettext")) {
                    String randomText = "something random";
                    prettyPrintOut("Server", out, randomText);
                    String answerReceived = in.readLine();
                    int num = Integer.parseInt(answerReceived);
                    if (num == randomText.length()) {
                        prettyPrintOut("Server", out, "Correct answer");
                    } else {
                        prettyPrintOut("Server", out, "Wrong answer");
                    }
                    continue;
                }

                // send message to all clients by default "<message>"
                prettyPrintOut("You", out, receivedString);
                System.out.println("Received: " + receivedString);
                for (Map.Entry<String, Socket> entry : clientsMap.entrySet()) {
                    if (entry.getValue() == clientConnection) {
                        continue;
                    }
                    PrintWriter clientOut = new PrintWriter(entry.getValue().getOutputStream(), true);
                    clientOut.println(name + " : " + receivedString);
                    prettyPrintOut("Server", out, receivedString);
                }
            }
        } catch (Exception e) {
            System.out.println("Error handling client");
        }
    }

    public static void main(String[] args) throws Exception {

        try (ServerSocket server = new ServerSocket(8080)) {
            System.out.println("Server started at " + server.getLocalSocketAddress());
            while (true) {
                Socket clientConnection = server.accept();
                Thread thread = new Thread(() -> handleClient(clientConnection));
                thread.start();
            }
        }
    }

    public static void prettyPrintOut(String sender, PrintWriter out, String receivedString) {
        out.print("\033[1A");
        out.flush();
        out.println(sender + " : " + receivedString);
    }
}