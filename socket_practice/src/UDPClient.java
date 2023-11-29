import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class UDPClient {
    public static void main(String[] args) {
        try (DatagramSocket socket = new DatagramSocket()) {
            InetAddress address = InetAddress.getByName("localhost");
            int port = 8080;

            new Thread(() -> {
                try {
                    byte[] buffer = new byte[1024];
                    while (true) {
                        DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                        socket.receive(packet);
                        String response = new String(packet.getData(), 0, packet.getLength());
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
                        byte[] buffer = userInput.getBytes();
                        DatagramPacket packet = new DatagramPacket(buffer, buffer.length, address, port);
                        socket.send(packet);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }).start();

            Thread.sleep(Long.MAX_VALUE);
        } catch (Exception e) {
            System.out.println("Error handling client");
        }
    }
}
