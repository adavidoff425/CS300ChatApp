import java.io.*;
import java.util.*;
import java.net.Socket;

public class Client {
    private DataInputStream sin;
    private DataOutputStream sout;
    private Socket clientSocket;
    private boolean connected;
    private ClientGUI gui;
    private String username;
    private String server_address;
    private int port;

    public Client(String server, int port, ClientGUI gui){
        this.server_address = server;
        this.port = port;
        this.gui = gui;
    }

    public boolean connect(){
        try{
            this.clientSocket = new Socket(server_address, port);
        }
        catch(Exception e){
            this.gui.append("Connection error" + e + "\n");
            return false;
        }
        this.gui.append("Connected to " + this.clientSocket.getInetAddress() + ":" + this.clientSocket.getPort() + "\n");

        try{
            this.sin = new DataInputStream(this.clientSocket.getInputStream());
            this.sout = new DataOutputStream(this.clientSocket.getOutputStream());
        }
        catch(IOException e){
            this.gui.append("Error creating I/O streams" + e + "\n");
            return false;
        }
        return true;
    }
}
