import java.io.*;
import java.util.*;
import java.net.Socket;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;

public class Client extends JFrame{
    protected DataInputStream sin;
    protected DataOutputStream sout;
    protected Socket clientSocket;
    protected boolean connected;
    protected ClientGUI gui;
    protected String username;
    protected String server_address;
    protected Scanner scan;
    protected int port;

    public Client(String server, int port){
        super("Chat Application");
        this.server_address = server;
        this.port = port;
        this.scan = new Scanner(System.in);
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
    
    public boolean register(String name, String password){
        try {
            this.sout.writeChars(name);
            this.sout.flush();
            boolean available = this.sin.readBoolean();

            if (!available) {
                this.gui.append("Username already taken");
                return false;
            }

            this.sout.writeChars(password);
            this.clientSocket.close();
        }
        catch(Exception e) {
            e.printStackTrace();
        }
        return true;
    }
}
