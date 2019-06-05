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
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.server_address = server;
        this.port = port;
    }

    public boolean connect(){
        try{
            this.clientSocket = new Socket(server_address, port);
        }
        catch(Exception e){
            this.gui.append("Connection error" + e + "\n");
            return false;
        }

        this.connected = true;
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
            this.sout.writeUTF(name);
            this.sout.flush();
            boolean available = this.sin.readBoolean();
            if (!available) {
                this.gui.append("Username already taken\n");
                return false;
            }

            this.sout.writeUTF(password);
        }
        catch(Exception e) {
            System.out.println("Error registering user\n");
        }
        return true;
    }

    public boolean login(String name, String password){
        try {
            this.sout.writeUTF(name);
            this.sout.flush();
            boolean exists = this.sin.readBoolean();
            if (!exists) {
                this.gui.append("Username not found\n");
                return false;
            }
            this.sout.writeUTF(password);
            this.sout.flush();
            boolean correct = this.sin.readBoolean();
            if (!correct) {
                this.gui.append("Incorrect password\n");
                return false;
            }
        }
            catch(Exception e){
                System.out.println("Error logging in\n");
            }

            return true;
        }
    }
