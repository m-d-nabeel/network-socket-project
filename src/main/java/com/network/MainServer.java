package com.network;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.text.SimpleDateFormat;
import java.util.*;

import static com.network.Encryption.decryptRailFence;
import static com.network.Encryption.encryptRailFence;
import static com.network.OffensiveWords.righteous_word;
import static com.network.UtilFunctions.getTimestamp;

public class MainServer {

    // KEYS
    private static final long P = 23; // (Same for both server and client)
    private static final long G = 9; // (Same for both server and client)
    private static final long serverPrivateKey = 4;
    private static final int PORT = 4000;
    private static final String DISCONNECT_MESSAGE = "!DISCONNECTED!";
    private static final String FIRST_CONNECTION = "!FIRST_CONNECTION!";
    private static final String SERVER = "localhost";
    private static final String GAME_START = "start_the_game_please";
    private static final String PALINDROME = "check_for_palindrome_please";
    private static final String ODD_EVEN = "check_for_odd_even_please";
    private static final String CLIENT_TEXT = "initiate_client_message";
    private static final String GET_RECEIVED_MESSAGES = "get_all_received_messages";
    private static final String GET_PREVIOUS_MESSAGE = "get_previous_messages";
    private static final String FIND_IN_MESSAGES = "get_messages_with_word";

    // ANSI escape codes for text colors
    private static final String RESET = "\u001B[0m";
    private static final String RED = "\u001B[31m";
    private static final String GREEN = "\u001B[32m";
    private static final String YELLOW = "\u001B[33m";
    private static final String BLUE = "\u001B[34m";
    private static final String BRIGHT_BLACK = "\u001B[90m";
    private final Map<String, Map<String, List<String>>> textMessagesSent = new HashMap<>();
    private final Map<String, Map<String, List<String>>> textMessagesReceived = new HashMap<>();
    private final Map<String, Map<String, String>> userList = new HashMap<>();
    private final Map<String, String> previousSentMessage = new HashMap<>();
    private long sharedSecretKey;

    public static final Path path = Paths.get("database.txt");

    public static void main(String[] args) {
        MainServer server = new MainServer();
        server.start();
    }

    private long power(long a, long b, long P) {
        if (b == 1)
            return a;
        return (long) (Math.pow(a, b) % P);
    }

    public void start() {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            coloredPrint(
                    "Server is running on Local Socket Address = " + RED + serverSocket.getLocalSocketAddress() + RESET,
                    GREEN);
            while (true) {
                try {
                    Socket clientConnection = serverSocket.accept();
                    Thread thread = new Thread(
                            () -> handleClient(clientConnection, new InetSocketAddress(SERVER, PORT)));
                    thread.start();
                    coloredPrint("[NEW CONNECTION] " + clientConnection.getInetAddress() + " connected.\n", GREEN);
                    coloredPrint("[ACTIVE CONNECTIONS] " + (Thread.activeCount() - 2) + "\n", YELLOW);
                } catch (IOException e) {
                    coloredPrint("[UNABLE TO CONNECT TO THE CLIENTS]: " + e.getMessage() + "...\n", RED);
                    System.exit(0);
                }
            }
        } catch (IOException e) {
            coloredPrint("[UNABLE TO CREATE SOCKET]: " + e.getMessage() + "...\n", RED);
            System.exit(0);
        }
    }

    private void coloredPrint(Object text, String color) {
        System.out.println(color + text + RESET);
    }

    private void sendMessage(String msg, Socket clientConnection, InetSocketAddress clientAddress, String clientName) {
        try {
            PrintWriter out = new PrintWriter(clientConnection.getOutputStream(), true);
            String encrypted_text = encryptRailFence(msg, (int) sharedSecretKey);
            out.println(encrypted_text);
        } catch (IOException e) {
            coloredPrint("[UNABLE TO SEND MESSAGE TO " + userList.get(clientAddress + clientName).get("name") + "]: "
                    + e.getMessage() + "...\n", RED);
            userList.remove(clientAddress + clientName);
            System.exit(0);
        }
    }

    private String is_prime(long number) {
        if (number < 2)
            return "COMPOSITE";
        for (int i = 2; i < Math.sqrt(number) + 1; i++) {
            if (number % i == 0)
                return "COMPOSITE";
        }
        return "PRIME";
    }

    private List<String> decodeMessage(String strReceived, InetSocketAddress clientAddress, Socket clientConnection) {
        String decryptedText = decryptRailFence(strReceived, (int) sharedSecretKey);
        HashMap<String, String> clientObject = new Gson().fromJson(decryptedText,
                new TypeToken<HashMap<String, String>>() {
                }.getType());
        String clientName = clientObject.get("name");
        coloredPrint(clientName + " ⇒ " + strReceived, BRIGHT_BLACK);
        List<String> list = new ArrayList<>();
        list.add(clientName);
        coloredPrint(clientObject, BRIGHT_BLACK);
        try {
            Files.write(path, (decryptedText + "\n").getBytes(), StandardOpenOption.APPEND);
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (clientObject.get("msg").equals(FIRST_CONNECTION)) {
            userList.put(clientAddress + clientName, new HashMap<>());
            userList.get(clientAddress + clientName).put("name", clientObject.get("name"));
            textMessagesSent.put(clientAddress + clientName, new HashMap<>());
            textMessagesSent.get(clientAddress + clientName).put("SERVER", new ArrayList<>());
            textMessagesReceived.put(clientAddress + clientName, new HashMap<>());
            userList.get(clientAddress + clientName).put("option", "");
            list.add("Joined the server.");
            list.add(clientObject.get("timestamp"));
        } else {
            list.add(clientObject.get("msg"));
            list.add(clientObject.get("timestamp"));
            list.add(clientObject.get("receiver"));
            textMessagesSent.get(clientAddress + clientName).get("SERVER").add(clientObject.get("msg"));
        }
        return list;
    }

    // GAME LOGICS
    public void prime_composite_game(List<String> list, Socket clientConnection, InetSocketAddress clientAddress) {
        String clientName = list.get(0);
        String clientMsg = list.get(1);
        if (clientMsg.equals(userList.get(clientAddress + clientName).get("number"))) {
            sendMessage(GREEN + "Correct!!!" + RESET, clientConnection, clientAddress, clientName);
        } else {
            sendMessage(RED + "Wrong answer" + RESET, clientConnection, clientAddress, clientName);
        }
    }

    public void palindrome_game(List<String> list, Socket clientConnection, InetSocketAddress clientAddress) {
        String clientName = list.get(0);
        String clientMsg = list.get(1);
        boolean isPalindrome = true;
        for (int i = 0; i < clientMsg.length() / 2; i++) {
            if (clientMsg.charAt(i) != clientMsg.charAt(clientMsg.length() - i - 1)) {
                isPalindrome = false;
                break;
            }
        }
        if (isPalindrome) {
            sendMessage(GREEN + "Your input is a Palindrome!!! (/≧▽≦)/" + RESET, clientConnection, clientAddress,
                    clientName);
        } else {
            sendMessage(RED + "Your input is not a Palindrome. (´。＿。｀)" + RESET, clientConnection, clientAddress,
                    clientName);
        }
    }

    public void odd_even_game(List<String> list, Socket clientConnection, InetSocketAddress clientAddress) {
        String clientName = list.get(0);
        String clientMsg = list.get(1);
        try {
            long clientNumber = Long.parseLong(clientMsg);
            if ((clientNumber & 1) == 1) {
                sendMessage(GREEN + "Your input is an ODD number" + RESET, clientConnection, clientAddress, clientName);
            } else {
                sendMessage(GREEN + "Your input is a EVEN number" + RESET, clientConnection, clientAddress, clientName);
            }
        } catch (NumberFormatException ignored) {
            coloredPrint("Invalid number received from " + clientName, RED);
            sendMessage(RED + clientMsg + "Is an Invalid number" + RESET, clientConnection, clientAddress, clientName);
        }
    }

    public void initiate_client_to_client_message(List<String> list, Socket clientConnection,
            InetSocketAddress clientAddress) {
        String clientName = list.get(0);
        String clientMsg = list.get(1);
        String receiver = list.get(3);
        if (!textMessagesReceived.containsKey(receiver)) {
            textMessagesReceived.put(receiver, new HashMap<>());
        }
        if (!textMessagesReceived.get(receiver).containsKey(clientName)) {
            textMessagesReceived.get(receiver).put(clientName, new ArrayList<>());
        }
        if (clientMsg.equalsIgnoreCase(GET_RECEIVED_MESSAGES)) {
            String msgToSend = new Gson().toJson(textMessagesReceived.get(clientAddress + clientName));
            sendMessage(msgToSend, clientConnection, clientAddress, clientName);
        } else if (clientMsg.equalsIgnoreCase(GET_PREVIOUS_MESSAGE)) {
            sendMessage(
                    previousSentMessage.getOrDefault(clientAddress + clientName, RED + "No Previous Messages" + RESET),
                    clientConnection, clientAddress, clientName);
        } else {
            String cliMsg = String.valueOf(righteous_word(clientMsg));
            textMessagesReceived.get(receiver).get(clientName)
                    .add(RED + getTimestamp() + " ⇒ " + RESET + GREEN + cliMsg + RESET);
            previousSentMessage.put(clientAddress + clientName, clientMsg);
        }
    }

    public void find_messages_with_matching_word(List<String> list, Socket clientConnection,
            InetSocketAddress clientAddress) {
        String clientName = list.get(0);
        String clientMsg = list.get(1);

        Map<String, List<String>> matchingMessages = new HashMap<>();
        String keyToFind = clientAddress + clientName;

        Map<String, List<String>> messages = textMessagesSent.get(keyToFind);
        if (messages != null) {
            for (Map.Entry<String, List<String>> entry : messages.entrySet()) {
                List<String> messageList = entry.getValue();
                List<String> matchingStrings = new ArrayList<>();

                for (String message : messageList) {
                    if (message.toLowerCase().contains(clientMsg.toLowerCase())) {
                        matchingStrings.add(message);
                    }
                }

                if (!matchingStrings.isEmpty()) {
                    matchingMessages.put(entry.getKey(), matchingStrings);
                }
            }
        }
        String msgToSend = new Gson().toJson(matchingMessages.get("SERVER"));
        System.out.println(msgToSend);
        sendMessage(msgToSend, clientConnection, clientAddress, clientName);
    }

    private void handleClient(Socket clientConnection, InetSocketAddress clientAddress) {
        coloredPrint("[NEW CONNECTION] " + clientAddress + " connected.\n", GREEN);
        boolean connected = true;
        String clientName = "Unknown User";
        // Initial Connection
        String keyObjectMsg = new Gson().toJson(new KeyExchangeData(G, P), KeyExchangeData.class);
        // Sending Unencrypted Data
        try {
            PrintWriter out = new PrintWriter(clientConnection.getOutputStream(), true);
            out.println(keyObjectMsg);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        try {
            String serverPublicKey = String.valueOf(power(G, serverPrivateKey, P));
            BufferedReader in = new BufferedReader(new InputStreamReader(clientConnection.getInputStream()));
            String receivedKey = in.readLine();
            long clientPublicKey = Long.parseLong(receivedKey);
            sharedSecretKey = power(clientPublicKey, serverPrivateKey, P);
            coloredPrint("Shared Secret Key = " + sharedSecretKey, BRIGHT_BLACK);
            // Sending Unencrypted Data
            try {
                PrintWriter out = new PrintWriter(clientConnection.getOutputStream(), true);
                out.println(serverPublicKey);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        } catch (IOException e) {
            coloredPrint("[UNABLE TO RECEIVE MESSAGE FROM (" + clientName + ")", RED);
            String current_timestamp = new SimpleDateFormat("HH:mm:ss").format(new Date());
            System.out.println(RED + current_timestamp + " ⇒ " + RESET + BLUE + clientName + " : DISCONNECTED" + RESET);
            if (userList.get(clientAddress + clientName) != null) {
                userList.remove(clientAddress + clientName);
            }
        }
        while (connected) {
            String strReceived;
            try {
                BufferedReader in = new BufferedReader(new InputStreamReader(clientConnection.getInputStream()));
                strReceived = in.readLine();
                List<String> list = decodeMessage(strReceived, clientAddress, clientConnection);
                clientName = list.get(0);
                switch (list.get(1)) {
                    case DISCONNECT_MESSAGE -> {
                        coloredPrint(userList.get(clientAddress + clientName).get("name") + " is offline now.", RED);
                        coloredPrint("[ACTIVE CONNECTIONS] " + (Thread.activeCount() - 3) + "\n", YELLOW);
                        connected = false;
                        continue;
                    }
                    case GAME_START -> {
                        userList.get(clientAddress + clientName).put("option", GAME_START);
                        long numberForClient = (long) Math.ceil(Math.random() * 1000) + 2;
                        userList.get(clientAddress + clientName).put("number", is_prime(numberForClient));
                        coloredPrint("Sent Number = " + numberForClient + " ⇒ " + clientName, YELLOW);
                        String guessMessage = BLUE + "Guess for this Number: " + numberForClient;
                        sendMessage(guessMessage, clientConnection, clientAddress, clientName);
                        continue;
                    }
                    case PALINDROME -> {
                        userList.get(clientAddress + clientName).put("option", PALINDROME);
                        sendMessage(BLUE + "Enter a number to check its palindrome nature" + RESET, clientConnection,
                                clientAddress, clientName);
                        continue;
                    }
                    case ODD_EVEN -> {
                        userList.get(clientAddress + clientName).put("option", ODD_EVEN);
                        sendMessage(BLUE + "Enter a number to check its [odd/even] nature" + RESET, clientConnection,
                                clientAddress, clientName);
                        continue;
                    }
                    case CLIENT_TEXT -> {
                        userList.get(clientAddress + clientName).put("option", CLIENT_TEXT);
                        Gson gson = new Gson();
                        String json = gson.toJson(userList);
                        sendMessage(json, clientConnection, clientAddress, clientName);
                        continue;
                    }
                    case FIND_IN_MESSAGES -> {
                        userList.get(clientAddress + clientName).put("option", FIND_IN_MESSAGES);
                        continue;
                    }
                    default -> {
                        System.out.print(RED + list.get(2) + " ⇒ " + RESET);
                        System.out.println(
                                BLUE + userList.get(clientAddress + list.get(0)).get("name") + " : " + list.get(1)
                                        + RESET + BRIGHT_BLACK + " { Words = " + list.get(1).split(" ").length + " }"
                                        + RESET);
                    }
                }
                String option = userList.get(clientAddress + clientName).get("option");
                if (!option.isEmpty()) {
                    switch (option) {
                        case GAME_START -> prime_composite_game(list, clientConnection, clientAddress);
                        case PALINDROME -> palindrome_game(list, clientConnection, clientAddress);
                        case ODD_EVEN -> odd_even_game(list, clientConnection, clientAddress);
                        case CLIENT_TEXT -> initiate_client_to_client_message(list, clientConnection, clientAddress);
                        case FIND_IN_MESSAGES ->
                            find_messages_with_matching_word(list, clientConnection, clientAddress);
                        default -> userList.get(clientAddress + clientName).put("option", "");
                    }

                }
                System.out.println(textMessagesSent);
            } catch (IOException e) {
                coloredPrint("[UNABLE TO RECEIVE MESSAGE FROM (" + clientName + ")", RED);
                String current_timestamp = new SimpleDateFormat("HH:mm:ss").format(new Date());
                System.out.println(
                        RED + current_timestamp + " ⇒ " + RESET + BLUE + clientName + " : DISCONNECTED" + RESET);
                userList.remove(clientAddress + clientName);
                break;
            }
        }
        userList.remove(clientAddress + clientName);

        try {
            clientConnection.close();
        } catch (IOException ignored) {
            coloredPrint("Error closing the client connection", RED);
        }
    }

    public static class KeyExchangeData {
        private final long G;
        private final long P;

        public KeyExchangeData(long G, long P) {
            this.G = G;
            this.P = P;
        }

        public long getG() {
            return G;
        }

        public long getP() {
            return P;
        }
    }
}
