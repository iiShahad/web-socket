
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
            System.out.println("Disconnection Error: " + ex.getMessage());
        }
    }

    public void sendMessage(String message) throws IOException {
        try {
            if (isConnected) {
                dataOutputStream.writeUTF(message);
                System.out.println("Client: " + message);
            } else {
                throw new IOException("The connection is not established");
            }
        } catch (IOException ex) {
            isConnected = false;
            System.out.println("Error sending message: " + ex.getMessage());
        }
    }

    public String receiveMessage() throws IOException {
        try {
            if(isConnected) {
                return dataInputStream.readUTF();
            } else {
                throw new IOException("The connection is not established");
            }
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
                    String message = clientConnection.receiveMessage();
                    System.out.println("Server: " + message);
                } else {
                    System.out.println("The connection is not established");
                    break;
                }
            }
        } catch (SocketException e) {
            if (!clientConnection.isConnected()) {
                System.out.println("Listener thread stopping due to socket closure.");
            } else {
                System.out.println("SocketException in listener: " + e.getMessage());
                e.printStackTrace();
            }
        } catch (IOException e) {
            System.out.println("IOException in listener: " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            System.out.println("Error from listener: " + e.getMessage());
            e.printStackTrace();
        } finally {
            System.out.println("Listener thread stopping...");
            closeResources();
        }
    }

    private void closeResources() {
        try {
            if (clientConnection.dataInputStream != null) {
                clientConnection.dataInputStream.close();
            }
            if (clientConnection.dataOutputStream != null) {
                clientConnection.dataOutputStream.close();
            }
            if (clientConnection.socket != null) {
                clientConnection.socket.close();
            }
        } catch (IOException e) {
            System.out.println("Error closing resources: " + e.getMessage());
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
        System.out.println("Sender thread started");
        try {
            while (!isInterrupted() && clientConnection.isConnected()) {
                if (clientConnection.dataOutputStream != null && clientConnection.socket != null) {
                    String message = "Hello from the client";
                    clientConnection.sendMessage(message);
                    System.out.println("Sent message: " + message);
                    sleep(3000);
                } else {
                    System.out.println("The connection is not established");
                    break;
                }
            }
        } catch (InterruptedException e) {
            System.out.println("Sender thread interrupted");
        } catch (IOException e) {
            System.out.println("Error from sender: " + e.getMessage());
            e.printStackTrace();
        } finally {
            System.out.println("Sender thread stopping...");
            closeResources();
        }
    }

    private void closeResources() {
        try {
            if (clientConnection.dataInputStream != null) {
                clientConnection.dataInputStream.close();
            }
            if (clientConnection.dataOutputStream != null) {
                clientConnection.dataOutputStream.close();
            }
            if (clientConnection.socket != null) {
                clientConnection.socket.close();
            }
        } catch (IOException e) {
            System.out.println("Error closing resources: " + e.getMessage());
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
