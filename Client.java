import java.io.*;
import java.net.*;

public class Client {
    private String ipAddress;
    private int port;
    private DataOutputStream dataOutputStream;
    private DataInputStream dataInputStream;

    //Socket instance variable
    Socket socket;

    public Client(String ipAddress, int port) {
        this.ipAddress = ipAddress; 
        this.port = port;
    }
    
    public Client() {
        //127.0.0.1:5000 is localhost:5000 
        this.ipAddress = "127.0.0.1";
        this.port = 5000;
    }

    public void connect() throws IOException {
        try {
            socket = new Socket(ipAddress, port); // Connect to the server using the ipAddress and port
            dataOutputStream = new DataOutputStream(socket.getOutputStream());
            dataInputStream = new DataInputStream(socket.getInputStream());
        } catch (IOException ex) {
            System.out.println("Error: " + ex.getMessage());
            throw ex;
        }
    }

}
