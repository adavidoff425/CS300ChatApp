import java.io.*;
import java.util.*;
import java.net.*;

public class Server{
    private static Set<User> users = new HashSet<>();
    private static Set<PrintWriter> writers = new HashSet<>();
    private static ArrayList<ClientThread> clients = new ArrayList<>();
    private ServerSocket socket;
    private boolean running;
    private ServerGUI gui;
    private int port;
    
    public Server(int port, ServerGUI gui){
        this.port = port;
        this.gui = gui;
    }
    
    public void connect(){
        this.socket = new ServerSocket(this.port);
        this.running = true;
        while(running){
            Socket server = serverSocket.accept();
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
                    this.gui.append("Error disconnecting clients from server");
                }
            }
            
        }
    }
    
    public User addUser(String name, String pw){
        User newUser = new User(name, pw);
        this.users.add(newUser);
        newUser.login();
        return newUser;
    }
    
class ClientThread extends Thread{
    private User user;
    private Socket clientSocket;
    private DataInputStream sin;
    private DataOutputStream sout;
    private boolean connected;
    
    public ClientThread(Socket socket){
        this.clientSocket = socket;
        this.sin = new DataInputStream(this.clientSocket.getInputStream());
        this.sout = new DataOutputStream(this.clientSocket.getOutputStream());
    }
    
    public void run(){
        this.connected = true;
        while(connected){
            try{
                String username = this.sin.readUTF();
                if(username == null)
                    continue;
                for(User u : this.users){
                    if(u.find(username)){
                        this.gui.append(username + " already used. Please enter another username");
                        continue;
                    }
                }
            }
            catch(IOException e){"Error reading username from client"};
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