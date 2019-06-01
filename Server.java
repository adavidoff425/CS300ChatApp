import java.io.*;
import java.util.*;
import java.net.*;

public class Server {
    private static ArrayList<String> usernames = new ArrayList<>();
    private static Set<User> users = new HashSet<>();
    //  private static Set<PrintWriter> writers = new HashSet<>();
    private static ArrayList<ClientThread> clients = new ArrayList<>();
    private ServerSocket socket;
    private boolean running;
    private int port;
    private File userfile;
    private FileWriter writer;
    private FileReader fileReader;
    private BufferedReader reader;

    public Server(int port) throws ClassNotFoundException {
        this.port = port;
        try {
            this.userfile = File.createTempFile("users", ".txt");
        }
        catch(IOException e){
            System.out.println("file error\n");
        }
        if (!this.userfile.exists())
            this.userfile = new File("users.txt");
        else {
            try {
                this.fileReader = new FileReader(userfile);
                this.reader = new BufferedReader(fileReader);
                this.writer = new FileWriter(userfile);
            } catch (Exception e) {
                System.out.println("Error setting up file read/write\n");
            }
            try {
                getUsers();
            } catch (Exception gue) {
                System.out.println("Error reading user file\n");
            }
        }
        connect();
    }

    public void connect() throws ClassNotFoundException {
        try {
            this.socket = new ServerSocket(this.port);
        } catch (IOException e) {
            System.out.println("Couldn't connect to socket\n");
        }

        Socket server = null;
        this.running = true;
        System.out.println("Waiting on clients to connect\n");
        while (running) {
            try {
                server = this.socket.accept();
            } catch (IOException e) {
                System.out.println("Error connecting socket\n");
            }
            if (!running)
                break;
            ClientThread newClient = new ClientThread(server);
            this.clients.remove(newClient);
        }
        // Server no longer running
        try {
            this.socket.close();
            for (ClientThread thread : this.clients) {
                try {
                    thread.sin.close();
                    thread.sout.close();
                    thread.clientSocket.close();
                } catch (Exception e) {
                    System.out.println("Error disconnecting clients from server");
                }
            }

        } catch (Exception e) {
        }
    }

    // Adds user object to list of objects
    public User addUser(String name, String pw) throws IOException {
        User newUser = new User(name, pw);
        this.users.add(newUser);
        this.usernames.add(name);
        this.writer.write(name + "\n" + pw + "\n");
        this.writer.close();
        // ADD WRITE TO FILE FOR UN, PW, AND CHAT HISTORY
        return newUser;
    }

    public void getUsers() throws IOException {
        String nextUser = new String();
        while ((nextUser = this.reader.readLine()) != null) {
            this.usernames.add(nextUser);
            String nextPW = this.reader.readLine();
            User newUser = new User(nextUser, nextPW);
            users.add(newUser);
        }
        this.reader.close();
        this.fileReader.close();
    }

    private class ClientThread extends Thread {
        private User user;
        private Socket clientSocket;
        private DataInputStream sin;
        private DataOutputStream sout;
        private ClientGUI gui;
        private boolean connected;

        public ClientThread(Socket socket) throws ClassNotFoundException {
            this.clientSocket = socket;
            try {
                this.sin = new DataInputStream(socket.getInputStream());
                this.sout = new DataOutputStream(socket.getOutputStream());
            } catch (IOException e) {
                System.out.println("Error connecting I/O\n");
            }
            run();
        }

        public void run() {
            clients.add(this);
            this.connected = true;
            String username = new String();
            String password = new String();
            String action = new String();

            while (connected) {
                try {
                    action = this.sin.readUTF();

                    if (action.equals("REGISTER") || action.equals("LOGIN")) {
                        username = this.sin.readUTF();
                        for (String u : usernames) {
                            if (u.equals(username) && action.equals("REGISTER")) {
                                this.sout.writeUTF(username + " already used. Please enter another username\n");
                                this.sout.flush();
                                this.sout.writeBoolean(false);
                                this.sout.flush();
                                break;
                            } else if (u.equals(username) && action.equals("LOGIN")) {
                                try {
                                    password = this.sin.readUTF();
                                    for (User aUser : users) {
                                        if (!aUser.find(username))
                                            continue;
                                        this.user = aUser.loginAttempt(username, password);
                                        if (this.user == null) {
                                            this.sout.writeUTF("Incorrect password\n");
                                            this.sout.flush();
                                            break;
                                        }
                                        break;
                                    }
                                }
                                catch(Exception e){
                                        System.out.println("Error logging in user\n");
                                    }
                            }


                        }
                        if (action.equals("REGISTER")) {
                            this.sout.writeBoolean(true);
                            this.sout.flush();
                            password = this.sin.readUTF();
                            if (password != null) {
                                this.user = addUser(username, password);
                            }
                        }
                    }
                } catch (Exception ae) {
                    ae.printStackTrace();
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