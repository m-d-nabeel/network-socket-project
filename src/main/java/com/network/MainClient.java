package com.network;

import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;


public class MainClient {

    // ANSI escape codes for text colors
    private static final String RESET = "\u001B[0m";
    private static final String RED = "\u001B[31m";
    private static final String GREEN = "\u001B[32m";
    private static final String YELLOW = "\u001B[33m";
    private static final String BLUE = "\u001B[34m";
    private static final String BRIGHT_BLACK = "\u001B[90m";
    private static final String DISCONNECT_MESSAGE = "!DISCONNECTED!";
    private static final String FIRST_CONNECTION = "!FIRST_CONNECTION!";
    private static final String GAME_START = "start_the_game_please";
    private static final String PALINDROME = "check_for_palindrome_please";
    private static final String ODD_EVEN = "check_for_odd_even_please";
    private static final int PORT = 4000;
    private static final String SERVER = "localhost";
    private final HashMap<String, String> hashMap = new HashMap<>();
    private final Socket clientSocket;
    private final PrintWriter out;
    private final BufferedReader in;
    //    KEYS
    private long P; // (Same for both server and client) will be received from server
    private long G; // (Same for both server and client) will be received from server
    private long sharedSecretKey;
    private static final long clientPrivateKey = 3;
    private String NAME;

    public MainClient() throws IOException {
        hashMap.put("p", "PRIME");
        hashMap.put("P", "PRIME");
        hashMap.put("c", "COMPOSITE");
        hashMap.put("C", "COMPOSITE");
        this.clientSocket = new Socket(SERVER, PORT);
        this.out = new PrintWriter(clientSocket.getOutputStream(), true);
        this.in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
    }

    public static void main(String[] args) {
        try {
            MainClient client = new MainClient();
            client.setInitialConnection();
            client.sendAndReceiveMessages();
        } catch (IOException e) {
            System.out.println("Unable to connect to the server: " + e.getMessage());
        }
    }

    private static String input(String message) {
        System.out.print(message);
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        try {
            return reader.readLine();
        } catch (IOException ignored) {
            System.out.println(RED + "IOException");
        }
        return "";
    }

    public static String encryptRailFence(String text, int key) {

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

    private void setInitialConnection() throws IOException {
        String keyPairs = in.readLine();
        coloredPrint("Server : Keys Received = " + keyPairs, GREEN);
        MainServer.KeyExchangeData clientObject = new Gson().fromJson(keyPairs, MainServer.KeyExchangeData.class);
        this.G = clientObject.getG();
        this.P = clientObject.getP();
        String clientPublicKey = String.valueOf(power(G, clientPrivateKey, P));
        out.println(clientPublicKey);
        String serverPublicKey = in.readLine();
        coloredPrint("Server : Server Public Key = " + serverPublicKey, GREEN);
        sharedSecretKey = power(Long.parseLong(serverPublicKey), clientPrivateKey, P);
        coloredPrint("Shared Secret Key = " + sharedSecretKey, BRIGHT_BLACK);
    }

    public void coloredPrint(String text, String color) {
        System.out.println(color + text + RESET);
    }

    public void sendAndReceiveMessages() throws IOException {
        boolean connected = true;
        this.NAME = input(BLUE + "Enter your name or alias : ");
        String msgToSend = new Gson().toJson(new Message(FIRST_CONNECTION, NAME));
        String encryptedText = encryptRailFence(msgToSend, (int) sharedSecretKey);
        out.println(encryptedText);
        while (connected) {
            String msg;
            coloredPrint("\n——————————————————[PICK_YOUR_CHOICE]——————————————————\n", GREEN);
            printOptions();
            msg = input(BRIGHT_BLACK + "Choose one of the stated option : " + RESET);
            switch (msg) {
                case "0" -> {
                    sendMessage(DISCONNECT_MESSAGE);
                    connected = false;
                }
                case "1" -> {
                    sendMessage(GAME_START);
                    receiveMessage();
                    connected = prime_composite_game();
                }
                case "2" -> {
                    sendMessage(PALINDROME);
                    receiveMessage();
                    connected = palindrome_game();
                }
                case "3" -> {
                    sendMessage(ODD_EVEN);
                    receiveMessage();
                    connected = odd_even_game();
                }
                default ->
                    coloredPrint("Enter a VALID option from set {0, 1, 2, 3}", RED);
            }
        }
        coloredPrint("Connection Closed!", RED);
        closeClientConnection();
        System.exit(0);
    }

    public boolean prime_composite_game() {
        boolean connected = true;
        while (true) {
            String msg = input(YELLOW + "Prime or Composite [p/c] : ");
            if (!(msg.equalsIgnoreCase("p") || msg.equalsIgnoreCase("c"))) {
                coloredPrint("Invalid Option", YELLOW);
            } else {
                sendMessage(hashMap.get(msg));
                receiveMessage();
                break;
            }
        }
        if (input(BRIGHT_BLACK + "Enter EXIT to exit the game or press ENTER to continue." + RESET).equalsIgnoreCase("exit")) {
            sendMessage(DISCONNECT_MESSAGE);
            connected = false;
        }
        return connected;
    }

    public boolean palindrome_game() {
        String msg = input(RESET + YELLOW + "Input = ");
        boolean connected = true;
        sendMessage(msg);
        receiveMessage();
        System.out.println();
        if (input(BRIGHT_BLACK + "Enter EXIT to exit the game or press ENTER to continue." + RESET).equalsIgnoreCase("exit")) {
            sendMessage(DISCONNECT_MESSAGE);
            connected = false;
        }
        return connected;
    }

    public boolean odd_even_game() {
        boolean connected = true;
        String msg = input(RESET + YELLOW + "Input = ");
        sendMessage(msg);
        receiveMessage();
        System.out.println();
        if (input(BRIGHT_BLACK + "Enter EXIT to exit the game or press ENTER to continue." + RESET).equalsIgnoreCase("exit")) {
            sendMessage(DISCONNECT_MESSAGE);
            connected = false;
        }
        return connected;
    }

    public void printOptions() {
        coloredPrint("1. Play [Prime/Composite] guessing game.", YELLOW);
        coloredPrint("2. Enter a number and check its Palindrome nature.", YELLOW);
        coloredPrint("3. Enter a number and check its [Even/Odd] nature.", YELLOW);
        coloredPrint("0. Exit.\n", YELLOW);
    }

    public void sendMessage(String inputMessage) {
        String msgToSend = new Gson().toJson(new Message(inputMessage, NAME));
        String encryptedText = encryptRailFence(msgToSend, (int) sharedSecretKey);
        out.println(encryptedText);
    }

    public void receiveMessage() {
        try {
            String serverMessage = in.readLine();
            String current_timestamp = new SimpleDateFormat("HH:mm:ss").format(new Date());
            System.out.println(RED + current_timestamp + " ⇒ " + RESET + BLUE + "Server : " + serverMessage);
        } catch (IOException e) {
            coloredPrint("[UNABLE TO RECEIVE MESSAGE FROM THE SERVER]: " + e.getMessage(), RED);
        }
    }

    public void closeClientConnection() {
        try {
            in.close();
            out.close();
            clientSocket.close();
        } catch (IOException e) {
            coloredPrint("Error while closing the connection: " + e.getMessage(), RED);
        }
    }

    private static class Message {
        private final String msg;
        private final String name;
        private final String timestamp;

        public Message(String msg, String name) {
            this.msg = msg;
            this.name = name;
            this.timestamp = new SimpleDateFormat("HH:mm:ss").format(new Date());
        }
    }
}
