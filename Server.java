import java.io.*;
import java.util.*;
import java.net.*;

public class Server {
    private static ArrayList<String> usernames = new ArrayList<>();
    private static Set<User> users = new HashSet<>();
    private static ArrayList<ClientThread> clients = new ArrayList<>();
    private ServerSocket socket;
    private boolean running;
    private int port;
    private File userfile;
    private FileWriter writer;
    private FileReader fileReader;
    private BufferedReader reader;

    public Server(int port) throws ClassNotFoundException, IOException {
        this.port = port;
        this.userfile = new File("users.txt");
        if(!this.userfile.exists()) {
            userfile.createNewFile();
            try {
                this.fileReader = new FileReader(this.userfile);
                this.reader = new BufferedReader(fileReader);
                this.writer = new FileWriter(this.userfile);
            } catch (Exception e) {
                System.out.println("Error setting up file read/write\n");
                e.printStackTrace();
            }
        }
        else{
            try{
                this.fileReader = new FileReader(this.userfile);
                this.reader = new BufferedReader(fileReader);
                this.writer = new FileWriter(this.userfile, true);
            }
            catch(Exception e){
                System.out.println("Error setting up file read/write\n");
                e.printStackTrace();
            }
        }
            try {
                getUsers();
            } catch (Exception gue) {
                System.out.println("Error reading user file\n");
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
        this.writer.flush();
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

        public synchronized boolean listen() throws IOException{
            String action = new String();

            action = this.sin.readUTF();
            if (action.equals("USERS")){
                for(User u : users){
                    if(u.isLoggedIn()){
                        this.sout.writeUTF(u.get_name());
                        this.sout.flush();
                    }
                }
                this.sout.writeUTF("DONE");
                this.sout.flush();
                listen();
            }

            else if(action =="HISTORY"){

            }

            else if(action == "SENDMSG"){

            }

            else if(action == "LOGOUT") {
                this.user.logout();
                // Refresh all user lists
                return false;
            }

            return true;
        }

        public synchronized void run() {
            clients.add(this);
            this.connected = true;
            String username = new String();
            String password = new String();
            String action = new String();
            Boolean LOGIN = false;
            Boolean REGISTER = true;

            while (connected) {
                try {
                    action = this.sin.readUTF();

                    if (action.equals("REGISTER") || action.equals("LOGIN")) {
                        username = this.sin.readUTF();
                        for (String u : usernames) {
                            if (action.equals("REGISTER") && u.equals(username)) {
                                REGISTER = false;
                                this.sout.writeBoolean(REGISTER);
                                this.sout.flush();
                                break;
                            } else if (action.equals("LOGIN") && u.equals(username)) {
                                try {
                                    LOGIN = true;
                                    this.sout.writeBoolean(LOGIN);
                                    password = this.sin.readUTF();
                                    for (User aUser : users) {
                                        if (!aUser.find(username))
                                            continue;
                                        this.user = aUser.loginAttempt(username, password);
                                        if (this.user == null) {
                                            this.sout.writeBoolean(false);
                                            this.sout.flush();
                                            break;
                                        } else {
                                            this.sout.writeBoolean(true);
                                            connected = listen();
                                        }
                                    }
                                } catch (Exception e) {
                                    System.out.println("Error logging in user in server\n");
                                    return;
                                }
                            }
                        }

                        if (action.equals("REGISTER") && REGISTER) {
                            this.sout.writeBoolean(true);
                            this.sout.flush();
                            password = this.sin.readUTF();
                            if (password != null) {
                                this.user = addUser(username, password);
                                connected = listen();
                            }
                        } else if (action.equals("LOGIN") && !LOGIN) {
                            this.sout.writeBoolean(false);
                            this.sout.flush();
                        }
                    }

                } catch (Exception ae) {
                    System.out.println("Client Thread exception caught\n");
                    return;
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