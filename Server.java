import java.io.*;
import java.util.*;
import java.net.*;

public class Server{
    private static Set<User> users = new HashSet<>();
    private static Set<PrintWriter> writers = new HashSet<>();
    private static ArrayList<ClientThread> clients = new ArrayList<>();
    private ServerSocket socket;
    private boolean running;
    private int port;
    
    public Server(int port){
        this.port = port;
        connect();
    }
    
    public void connect(){
        try {
            this.socket = new ServerSocket(this.port);
        }
        catch(IOException e){}
        Socket server = null;
        this.running = true;
        while(running){
            try {
                server = this.socket.accept();
            }
            catch(IOException e){}
            if(!running)
                break;
            ClientThread newClient = new ClientThread(server);
            newClient.run();
        }
        // Server no longer running
        try{
            this.socket.close();
            for(ClientThread thread : this.clients){
                try{
                    thread.sin.close();
                    thread.sout.close();
                    thread.clientSocket.close();
                }
                catch(Exception e){
                    System.out.println("Error disconnecting clients from server");
                }
            }
            
        }
        catch(Exception e){}
    }

    // Adds user object to list of objects
    public User addUser(String name, String pw){
        User newUser = new User(name, pw);
        this.users.add(newUser);
        // ADD WRITE TO FILE FOR UN, PW, AND CHAT HISTORY
        newUser.login(name, pw);
        return newUser;
    }
    
private class ClientThread extends Thread{
    private User user;
    private Socket clientSocket;
    private DataInputStream sin;
    private DataOutputStream sout;
    private ClientGUI gui;
    private boolean connected;
    
    public ClientThread(Socket socket){
        this.clientSocket = socket;
        try {
            this.sin = new DataInputStream(this.clientSocket.getInputStream());
            this.sout = new DataOutputStream(this.clientSocket.getOutputStream());
        }
        catch(IOException e){this.gui.append("Error connecting I/O");}
    }
    
    public void run(){
        this.connected = true;
        String username = "";

        while(connected){
            try{
                username = this.sin.readUTF();
                if(username == null)
                    continue;
                for(User u : users){
                    if(u.find(username)){
                        this.sout.writeUTF(username + " already used. Please enter another username");
                        this.sout.flush();
                    }
                }
            }
            catch(IOException e){
                this.gui.append("Error reading username from client");
            }
            try{
                String password = this.sin.readUTF();
                this.user = addUser(username, password);
            }
            catch(Exception e){
                this.gui.append("Error creating new user");
            }
        }
    }
}
}