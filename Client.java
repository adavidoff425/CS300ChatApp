import java.io.*;
import java.util.*;
import java.net.Socket;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;

public class Client extends JFrame implements ActionListener{
    private DataInputStream sin;
    private DataOutputStream sout;
    private Socket clientSocket;
    private boolean connected;
    private ClientGUI gui;
    private String username;
    private String server_address;
    private Scanner scan;
    private int port;

    public Client(String server, int port, ClientGUI gui){
        this.server_address = server;
        this.port = port;
        this.gui = gui;
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
        try{
            this.sout.writeChars(name);            
            this.sout.flush();
            boolean available = this.sin.readBoolean();
            
        if(!available){
            this.gui.append("Username already taken");
            return false;
        }
        
            this.sout.writeChars(password);
            this.clientSocket.close();
        }
        catch(Exception e)
            e.printStackTrace;
    }
}
