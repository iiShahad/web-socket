import java.io.*;
import java.net.*;

public class SimpleServer {
    private static final int PORT = 5000;

    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Simple Server is running on port " + PORT);

            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("New client connected");

                // Use a separate thread to handle each client connection
                new Thread(() -> {
                    try (
                        DataOutputStream output = new DataOutputStream(clientSocket.getOutputStream());
                        DataInputStream input = new DataInputStream(clientSocket.getInputStream())
                    ) {
                        // Continue listening for messages from the client
                        while (!clientSocket.isClosed()) {
                            try {
                                // Read a message from the client
                                String clientMessage = input.readUTF();
                                System.out.println("Received message from client: " + clientMessage);

                                // Send a message to the client
                                String serverMessage = "Hello from the server!";
                                output.writeUTF(serverMessage);
                                System.out.println("Sent message to client: " + serverMessage);
                            } catch (EOFException e) {
                                // Client disconnected
                                break;
                            }
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}