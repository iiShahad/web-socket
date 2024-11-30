
import java.io.*;
import java.net.*;

class ClientConnection {
    private String ipAddress;
    private int port;
    DataOutputStream dataOutputStream;
    DataInputStream dataInputStream;
    Socket socket;
    private boolean isConnected = false;

    public ClientConnection(String ipAddress, int port) {
        this.ipAddress = ipAddress;
        this.port = port;
    }

    public ClientConnection() {
        this("127.0.0.1", 5000);
    }

    public void connect() throws IOException {
        try {
            socket = new Socket(ipAddress, port);
            dataOutputStream = new DataOutputStream(socket.getOutputStream());
            dataInputStream = new DataInputStream(socket.getInputStream());
            isConnected = true;
            System.out.println("Connected to the server");
        } catch (IOException ex) {
            System.out.println("Connection Error: " + ex.getMessage());
            throw ex;
        }
    }

    public void disconnect() throws IOException {
        try {
            isConnected = false;
            if (dataInputStream != null) {
                dataInputStream.close();
            }
            if (dataOutputStream != null) {
                dataOutputStream.close();
            }
            if (socket != null) {
                socket.close();
            }
            System.out.println("Disconnected from the server");
        } catch (IOException ex) {
            throw ex;
        }
    }

    public void sendMessage(String message) throws IOException {
        try {
            dataOutputStream.writeUTF(message);
            System.out.println("Client: " + message);
        } catch (IOException ex) {
            isConnected = false;
            throw ex;
        }
    }

    public String receiveMessage() throws IOException { 
        try {
            return dataInputStream.readUTF();
        } catch (IOException ex) {
            isConnected = false;
            throw ex;
        }
    }

    public boolean isConnected() {
        return isConnected;
    }
}

class ListenerThread extends Thread {
    private final ClientConnection clientConnection;

    public ListenerThread(ClientConnection clientConnection) {
        this.clientConnection = clientConnection;
    }

    @Override
    public void run() {
        System.out.println("Listener thread started");
        try {
            while (!isInterrupted() && clientConnection.isConnected()) {
                if (clientConnection.dataInputStream != null && clientConnection.socket != null) {
                    try {
                        String message = clientConnection.receiveMessage();
                        System.out.println("Server: " + message);
                    } catch (SocketException e) {
                        if (!clientConnection.isConnected()) {
                            System.out.println("Listener thread stopping due to socket closure.");
                            break;
                        } else {
                            throw e;
                        }
                    }
                } else {
                    throw new Exception("The connection is not established");
                }
            }
        } catch (Exception ex) {
            if(ex instanceof InterruptedException){
                System.out.println("Listener thread stopping due to interruption.");
            }else{
                System.out.println("Error from listener: " + ex.getMessage());
                ex.printStackTrace();
            }
        } finally {
            System.out.println("Listener thread stopping...");
            try {
                if (clientConnection.dataInputStream != null) clientConnection.dataInputStream.close();
                if (clientConnection.dataOutputStream != null) clientConnection.dataOutputStream.close();
                if (clientConnection.socket != null) clientConnection.socket.close();
            } catch (IOException e) {
                System.out.println("Error closing resources: " + e.getMessage());
            }
        }
    }

    public void stopListening() {
        interrupt();
    }
}
class SenderThread extends Thread {
    private final ClientConnection clientConnection;

    public SenderThread(ClientConnection clientConnection) {
        this.clientConnection = clientConnection;
    }

    @Override
    public void run() {
        try {
            while (!isInterrupted() && clientConnection.isConnected()) {
                if (clientConnection.dataOutputStream != null && clientConnection.socket != null) {
                    String message = "Hello from the client";
                    clientConnection.sendMessage(message);
                    sleep(3000);
                } else {
                    throw new Exception("The connection is not established");
                }
            }
        } catch (Exception ex) {
            if(ex instanceof InterruptedException){
                System.out.println("Listener thread stopping due to interruption.");
            }else{
                System.out.println("Error from listener: " + ex.getMessage());
                ex.printStackTrace();
            }
        } finally {
            System.out.println("Sender thread stopping...");
            try {
                if (clientConnection.dataInputStream != null) clientConnection.dataInputStream.close();
                if (clientConnection.dataOutputStream != null) clientConnection.dataOutputStream.close();
                if (clientConnection.socket != null) clientConnection.socket.close();
            } catch (IOException e) {
                System.out.println("Error closing resources: " + e.getMessage());
            }
        }
    }

    public void stopSending() {
        interrupt();
    }
}

public class Client {
    public static void main(String[] args) {
        try {
            ClientConnection clientConnection = new ClientConnection();
            clientConnection.connect();

            ListenerThread listenerThread = new ListenerThread(clientConnection);
            listenerThread.start();

            SenderThread senderThread = new SenderThread(clientConnection);
            senderThread.start();

            //TODO: make the program run for 60 seconds
            Thread.sleep(10000); // Run for 10 seconds, then stop

            listenerThread.stopListening();
            senderThread.stopSending();

            System.out.println("Waiting for threads to finish...");
            listenerThread.join();
            senderThread.join();

            clientConnection.disconnect();
            System.out.println("Program exiting.");
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }
}