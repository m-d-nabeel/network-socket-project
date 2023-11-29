import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class Client {
    public static void main(String[] args) {
        try (Socket socket = new Socket("localhost", 8080)) {
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            new Thread(() -> {
                try {
                    String response;
                    while ((response = in.readLine()) != null) {
                        System.out.println(response);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }).start();
            new Thread(() -> {
                try {
                    while (true) {
                        String userInput = System.console().readLine();
                        out.println(userInput);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }).start();
            Thread.sleep(Long.MAX_VALUE);
        } catch (Exception e) {
            System.out.println("Error handling client");
        }
    }
}
