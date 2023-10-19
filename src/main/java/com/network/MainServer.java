package com.network;

import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.*;

public class MainServer {

    //    KEYS
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
    // ANSI escape codes for text colors
    private static final String RESET = "\u001B[0m";
    private static final String RED = "\u001B[31m";
    private static final String GREEN = "\u001B[32m";
    private static final String YELLOW = "\u001B[33m";
    private static final String BLUE = "\u001B[34m";
    private static final String BRIGHT_BLACK = "\u001B[90m";
    private final Map<String, HashMap<Object, Object>> userList = new HashMap<>();
    private long sharedSecretKey;

    public static void main(String[] args) {
        MainServer server = new MainServer();
        server.start();
    }

    public static String encryptRailFence(String text, int key) {

        // create the matrix to cipher plain text
        // key = rows , length(text) = columns
        char[][] rail = new char[key][text.length()];

        // filling the rail matrix to distinguish filled
        // spaces from blank ones
        for (int i = 0; i < key; i++)
            Arrays.fill(rail[i], '\n');

        boolean dirDown = false;
        int row = 0, col = 0;

        for (int i = 0; i < text.length(); i++) {

            // check the direction of flow
            // reverse the direction if we've just
            // filled the top or bottom rail
            if (row == 0 || row == key - 1)
                dirDown = !dirDown;

            // fill the corresponding alphabet
            rail[row][col++] = text.charAt(i);

            // find the next row using direction flag
            if (dirDown) row++;
            else row--;
        }

        // now we can construct the cipher using the rail
        // matrix
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < key; i++) {
            for (int j = 0; j < text.length(); j++) {
                if (rail[i][j] != '\n') {
                    result.append(rail[i][j]);
                }
            }
        }
        return result.toString();
    }

    // This function receives cipher-text and key
    // and returns the original text after decryption
    public static String decryptRailFence(String cipher, int key) {
        // create the matrix to cipher plain text
        // key = rows , length(text) = columns
        char[][] rail = new char[key][cipher.length()];

        // filling the rail matrix to distinguish filled
        // spaces from blank ones
        for (int i = 0; i < key; i++) {
            Arrays.fill(rail[i], '\n');
        }

        // to find the direction
        boolean dirDown = true;

        int row = 0, col = 0;

        // mark the places with '*'
        for (int i = 0; i < cipher.length(); i++) {
            // check the direction of flow
            if (row == 0) dirDown = true;
            if (row == key - 1) dirDown = false;

            // place the marker
            rail[row][col++] = '*';

            // find the next row using direction flag
            if (dirDown) row++;
            else row--;
        }

        // now we can construct the fill the rail matrix
        int index = 0;
        for (int i = 0; i < key; i++) {
            for (int j = 0; j < cipher.length(); j++) {
                if (rail[i][j] == '*' && index < cipher.length()) {
                    rail[i][j] = cipher.charAt(index++);
                }
            }
        }
        StringBuilder result = new StringBuilder();

        row = 0;
        col = 0;
        for (int i = 0; i < cipher.length(); i++) {
            // check the direction of flow
            if (row == 0) dirDown = true;
            if (row == key - 1) dirDown = false;

            // place the marker
            if (rail[row][col] != '*') result.append(rail[row][col++]);

            // find the next row using direction flag
            if (dirDown) row++;
            else row--;
        }
        return result.toString();
    }

    private long power(long a, long b, long P) {
        if (b == 1) return a;
        return (long) (Math.pow(a, b) % P);
    }

    public void start() {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            coloredPrint("Server is running on Local Socket Address = " + RED + serverSocket.getLocalSocketAddress() + RESET, GREEN);
            while (true) {
                try {
                    Socket clientConnection = serverSocket.accept();
                    Thread thread = new Thread(() -> handleClient(clientConnection, new InetSocketAddress(SERVER, PORT)));
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

    private void coloredPrint(String text, String color) {
        System.out.println(color + text + RESET);
    }

    private void sendMessage(String msg, Socket clientConnection, InetSocketAddress clientAddress, String clientName) {
        try {
            PrintWriter out = new PrintWriter(clientConnection.getOutputStream(), true);
            out.println(msg);
        } catch (IOException e) {
            coloredPrint("[UNABLE TO SEND MESSAGE TO " + userList.get(clientAddress + clientName).get("name") + "]: " + e.getMessage() + "...\n", RED);
            userList.remove(clientAddress + clientName);
            System.exit(0);
        }
    }

    private String is_prime(long number) {
        if (number < 2) return "COMPOSITE";
        for (int i = 2; i < Math.sqrt(number) + 1; i++) {
            if (number % i == 0) return "COMPOSITE";
        }
        return "PRIME";
    }

    private List<String> decodeMessage(String strReceived, InetSocketAddress clientAddress) {
        String decryptedText = decryptRailFence(strReceived, (int) sharedSecretKey);
        HashMap<String, String> clientObject = new Gson().fromJson(decryptedText, HashMap.class);
        String clientName = clientObject.get("name");
        coloredPrint(clientName + " ⇒ " + strReceived, BRIGHT_BLACK);
        List<String> list = new ArrayList<>();
        list.add(clientName);
        if (clientObject.get("msg").equals(FIRST_CONNECTION)) {
            userList.put(clientAddress + clientName, new HashMap<>());
            userList.get(clientAddress + clientName).put("name", clientObject.get("name"));
            userList.get(clientAddress + clientName).put("option", "");
            list.add("Joined the server.");
            list.add(clientObject.get("timestamp"));
        } else {
            list.add(clientObject.get("msg"));
            list.add(clientObject.get("timestamp"));
        }
        return list;
    }

    //    GAME LOGICS
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
            sendMessage(GREEN + "Your input is a Palindrome!!!🥳🥳" + RESET, clientConnection, clientAddress, clientName);
        } else {
            sendMessage(RED + "Your input is not a Palindrome🥹🥹" + RESET, clientConnection, clientAddress, clientName);
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

    private void handleClient(Socket clientConnection, InetSocketAddress clientAddress) {
        coloredPrint("[NEW CONNECTION] " + clientAddress + " connected.\n", GREEN);
        boolean connected = true;
        String clientName = "Unknown User";
        // Initial Connection
        String keyObject = new Gson().toJson(new KeyExchangeData(G, P), KeyExchangeData.class);
        sendMessage(keyObject, clientConnection, clientAddress, clientName);
        try {
            String serverPublicKey = String.valueOf(power(G, serverPrivateKey, P));
            BufferedReader in = new BufferedReader(new InputStreamReader(clientConnection.getInputStream()));
            String receivedKey = in.readLine();
            long clientPublicKey = Long.parseLong(receivedKey);
            sharedSecretKey = power(clientPublicKey, serverPrivateKey, P);
            coloredPrint("Shared Secret Key = " + sharedSecretKey, BRIGHT_BLACK);
            sendMessage(serverPublicKey, clientConnection, clientAddress, clientName);
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
                List<String> list = decodeMessage(strReceived, clientAddress);
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
                        sendMessage(BLUE + "Enter a number to check its palindrome nature" + RESET, clientConnection, clientAddress, clientName);
                        continue;
                    }
                    case ODD_EVEN -> {
                        userList.get(clientAddress + clientName).put("option", ODD_EVEN);
                        sendMessage(BLUE + "Enter a number to check its [odd/even] nature" + RESET, clientConnection, clientAddress, clientName);
                        continue;
                    }
                    case null, default -> {
                        System.out.print(RED + list.get(2) + " ⇒ " + RESET);
                        System.out.println(BLUE + userList.get(clientAddress + list.get(0)).get("name") + " : " + list.get(1));
                    }
                }
                String option = userList.get(clientAddress + clientName).get("option").toString();
                if (!option.isEmpty()) {
                    switch (option) {
                        case GAME_START -> prime_composite_game(list, clientConnection, clientAddress);
                        case PALINDROME -> palindrome_game(list, clientConnection, clientAddress);
                        case ODD_EVEN -> odd_even_game(list, clientConnection, clientAddress);
                        default -> userList.get(clientAddress + clientName).put("option", "");
                    }

                }
            } catch (IOException e) {
                coloredPrint("[UNABLE TO RECEIVE MESSAGE FROM (" + clientName + ")", RED);
                String current_timestamp = new SimpleDateFormat("HH:mm:ss").format(new Date());
                System.out.println(RED + current_timestamp + " ⇒ " + RESET + BLUE + clientName + " : DISCONNECTED" + RESET);
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
