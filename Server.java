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
    private File userfile;
    private FileWriter writer;
    
    public Server(int port) throws ClassNotFoundException{
        this.port = port;
        this.users = null;
        this.userfile = null;
        this.writer = null;
        connect();
    }
    
    public void connect() throws ClassNotFoundException{
        try {
            this.socket = new ServerSocket(this.port);
        }
        catch(IOException e){System.out.println("Couldn't connect to socket\n");}
        Socket server = null;
        this.running = true;
        System.out.println("Waiting on clients to connect\n");
        while(running){
            try {
                server = this.socket.accept();
            }
            catch(IOException e){System.out.println("Error connecting socket\n");}
            if(!running)
                break;
            ClientThread newClient = new ClientThread(server);
            System.out.println("Running client thread");
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
    public User addUser(String name, String pw) throws IOException{
        User newUser = new User(name, pw);
        this.users.add(newUser);
        this.writer.write(name + "\n" + pw + "\n");
        this.writer.close();
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
    
    public ClientThread(Socket socket) throws ClassNotFoundException{
        this.clientSocket = socket;
        try {
            this.sin = new DataInputStream(socket.getInputStream());
            this.sout = new DataOutputStream(socket.getOutputStream());
        }
        catch(IOException e){System.out.println("Error connecting I/O\n");}
        run();
    }
    
    public void run(){
        this.connected = true;
        String username = "";
        String password = "";
        String action = "";

        while(connected){
            try{
                action = this.sin.readUTF();
                username = this.sin.readUTF();
                if(username != null) {
                    this.sout.writeBoolean(true);
                    this.sout.flush();
                }

                if(userfile == null){
                    userfile = new File("users.txt");
                    writer = new FileWriter(userfile);
                    password = this.sin.readUTF();
                    if(password != null) {
                       try {
                            writer.write(username + "\n" + password + "\n");
                            writer.close();
                        } catch (IOException we) {
                            System.out.println("Error writing to file\n");
                        }
                       this.user = addUser(username, password);
                    }
                }

                else {
                    for (User u : users) {
                        if (u.find(username) && action.equals("REGISTER")) {
                            this.sout.writeUTF(username + " already used. Please enter another username\n");
                            this.sout.flush();
                            break;
                        } else if (u.find(username) && action.equals("LOGIN")){
                            try {
                                this.user = u;
                                password = this.sin.readUTF();
                                if (!this.user.login(username, password)) {
                                    this.sout.writeUTF("Incorrect password\n");
                                    this.sout.flush();
                                    this.user = null;
                                    break;
                                }
                                else {
                                    this.sout.flush();
                                    this.gui.append("Logged in as " + username + "\n");
                                }
                            }
                            catch(Exception e){
                                this.gui.append("Error logging in user\n");
                            }
                        }

                    }
                }
            }
            catch(IOException e){
                this.gui.append("Error reading username from client\n");
            }

            catch(Exception e){
                this.gui.append("Error creating new user\n");
            }
        }
    }
}

    public static void main(String[] args){
        try {
            Server server = new Server(2222);
        }
        catch(Exception e){
            e.printStackTrace();
        }
    }
}