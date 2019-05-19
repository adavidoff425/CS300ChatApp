import java.io.*;
import java.util.*;
import java.net.Socket;

public class Client {
    private DataInputStream sin;
    private DateOutputStream sout;
    private Socket clientSocket;
    private boolean connected;
    private ClientGUI gui;
    private String username;
    private String server_address;
    private int port;

    public Client(String server_address, int port, String username, ClientGUI gui){
        this.server_address = server;
        this.port = port;
        this.username = username;
        this.gui = gui;
    }

    public boolean connect(){
        try{
            this.socket = new Socket(server_address, port);
        }
        catch(Exception e){
            System.out.println("Connection error" + e);
            return false;
        }
        System.out.println("Connected to " + this.socket.getInetAddress() + ":" + this.socket.getPort());

        try{
            this.sin = new DataInputStream(this.socket.getInputStream());
            this.sout = new DateOutputStream(socket.getOutputStream());
        }
        catch(IOException e){
            System.out.println("Error creating I/O streams" + e);
            return false;
        }
    }
}
