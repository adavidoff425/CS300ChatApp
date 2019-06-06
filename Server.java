import java.io.*;
import java.util.*;
import java.net.*;

public class Server {
    private static ArrayList<String> usernames = new ArrayList<>();
    private static Set<User> users = new HashSet<>();
    private static ArrayList<ClientThread> clients = new ArrayList<>();
    private ServerSocket socket;
    private boolean running, test;
    private int port;
    private File userfile;
    private FileWriter writer;
    private FileReader fileReader;
    private BufferedReader reader;

    public Server(int port, boolean test) throws ClassNotFoundException, IOException {
        this.test = test;
        this.port = port;
        this.userfile = new File("users.txt");
        if (!this.userfile.exists()) {
            userfile.createNewFile();
            try {
                this.fileReader = new FileReader(this.userfile);
                this.reader = new BufferedReader(fileReader);
                this.writer = new FileWriter(this.userfile);
            } catch (Exception e) {
                System.out.println("Error setting up file read/write\n");
                e.printStackTrace();
            }
        } else {
            try {
                this.fileReader = new FileReader(this.userfile);
                this.reader = new BufferedReader(fileReader);
                this.writer = new FileWriter(this.userfile, true);
            } catch (Exception e) {
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
        if(test)
            System.out.println("Waiting on clients to connect\n");
        while (running) {
            try {
                server = this.socket.accept();
            } catch (IOException e) {
                System.out.println("Error connecting socket\n");
            }
            if(test)
                System.out.println("New client connected");
            try {
                DataInputStream in = new DataInputStream(server.getInputStream());
                DataOutputStream out = new DataOutputStream(server.getOutputStream());
                boolean check = in.readBoolean();
                if (!check) {
                    if(test)
                        System.out.println("New listenThread connected\n");
                    continue;
                }
                ClientThread newClient = new ClientThread(server, in, out);
                this.clients.add(newClient);
                newClient.start();
            } catch (Exception e) {
                try {
                    server.close();
                } catch (IOException ioe) {
                    System.out.println("Closing connection");
                }
                System.out.println("Error assigning new thread");
            }
        }
        // Server no longer running
        try {
            this.socket.close();
            for (ClientThread thread : this.clients) {
                try {
                    thread.sin.close();
                    thread.sout.close();
                    thread.clientSocket.close();
                    if(test)
                        System.out.println("All client threads disconnected\n");
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
        final private Socket clientSocket;
        final private DataInputStream sin;
        final private DataOutputStream sout;
        private boolean connected;

        public ClientThread(Socket socket, DataInputStream in, DataOutputStream out) throws ClassNotFoundException {
            this.clientSocket = socket;
            this.sin = new DataInputStream(in);
            this.sout = new DataOutputStream(out);
        }

        public synchronized boolean listen() throws IOException {
            String action = new String();

            action = this.sin.readUTF();
            if (action.equals("USERS")) {
                for (User u : users) {
                    if (u.isLoggedIn()) {
                        this.sout.writeUTF(u.get_name());
                        this.sout.flush();
                    }
                }
                this.sout.writeUTF("DONE");
                this.sout.flush();
                if(test)
                    System.out.println("Active user list sent to client\n");
                listen();
            } else if (action.equals("HISTORY")) {
                ArrayList<String> history = new ArrayList<>();
                history = this.user.retrieve_chatHistory();
                for (String msg : history) {
                    this.sout.writeUTF(msg);
                    this.sout.flush();
                }
                this.sout.writeUTF("DONE");
                this.sout.flush();
                if(test)
                    System.out.println("Chat history sent to client\n");
                listen();
            } else if (action.equals("SENDMSG")) {
                String usr = new String();
                String msg = new String();
                usr = this.sin.readUTF();
                msg = this.sin.readUTF();
                for (ClientThread ct : clients) {
                    if (ct.user.find(usr)) {
                        ct.sout.writeUTF("NEWMSG");
                        ct.sout.flush();
                        ct.sout.writeUTF(this.user.get_name());
                        ct.sout.flush();
                        ct.sout.writeUTF(msg);
                        ct.sout.flush();
                        ct.user.add_msg(this.user, msg);
                        this.user.add_msg(this.user, msg);
                        if(test)
                            System.out.println("Message written to both users chat history\n");
                        listen();
                    }
                }
                if(test)
                    System.out.println("No other clients found\n");
                return false;
            } else if (action.equals("BROADCAST")) {
                String msg = new String();
                while (!msg.equals("SENDMSG"))
                    msg = this.sin.readUTF();
                this.sin.readUTF();
                msg = this.sin.readUTF();
                if (!msg.equals("")) {
                    for (ClientThread ct : clients)
                        ct.getBroadcast(msg + "\n");
                    if (test)
                        System.out.println("Broadcast message sent to all active clients\n");
                }
                else if (test)
                    System.out.println("Broadcast test failed\n");
            } else if (action.equals("START")) {
                String usr = new String();
                usr = this.sin.readUTF();
                boolean found = false;
                for (ClientThread ct : clients) {
                    if (ct.user.find(usr)){
                        found = true;
                        ct.sout.writeUTF("CHAT");
                        ct.sout.flush();
                        ct.sout.writeUTF(ct.user.get_name());
                        ct.sout.flush();
                        Boolean correct = ct.sin.readBoolean();
                        if (correct) {
                            ct.sout.writeUTF(this.user.get_name());
                            ct.sout.flush();
                            if (test)
                                System.out.println("New chat initiated with " + usr);
                        }
                        else if (!correct && test)
                            System.out.println("New chat initiation failed\n");
                    }
                }
                if(!found && test)
                    System.out.println("New chat could not be initiated; other user not found\n");
            } else if (action.equals("LOGOUT")) {
                this.user.logout();
                clients.remove(this);
                this.connected = false;
                if(test)
                    System.out.println(this.user.get_name() + " logout successful\n");
                return false;
            } else if (action.equals("EXIT")) {
                String with = new String(this.sin.readUTF());
                User other = null;
                for (User u : users) {
                    if (u.find(with))
                        other = u;
                }
                if (other != null) {
                    if (this.user.writeHistory() && other.writeHistory()) {
                        this.sout.writeUTF("DONE");
                        this.sout.flush();
                        if(test)
                            System.out.println("History not written for one or both users\n");
                    }
                    for (ClientThread ct : clients) {
                        if (ct.user.get_name().equals(with)) {
                            ct.sout.writeUTF("EXITED");
                            ct.sout.flush();
                            ct.sout.writeUTF(this.user.get_name());
                            ct.listen();
                        }
                    }
                }
                else if (test)
                    System.out.println("Exiting chat failed; other user not found\n");
            }
            return false;
        }

        public synchronized void run() {
            if (!clients.contains(this))
                clients.add(this);
            this.connected = true;
            String action = new String();
            Boolean LOGIN = false;
            Boolean REGISTER = true;

            while (connected) {
                try {
                    action = this.sin.readUTF();
                    if (action.equals("REGISTER") || action.equals("LOGIN")) {
                        String username = new String();
                        String password = new String();
                        username = this.sin.readUTF();
                        for (String u : usernames) {
                            if (action.equals("REGISTER") && u.equals(username)) {
                                REGISTER = false;
                                this.sout.writeBoolean(REGISTER);
                                this.sout.flush();
                                if(test)
                                    System.out.println("Username already exits\n");
                                continue;
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
                                            if(test)
                                                System.out.println("Incorrect Password. Try again\n");
                                            continue;
                                        } else {
                                            this.sout.writeBoolean(true);
                                            if(test)
                                                System.out.println("User " + username + " succesfully logged in\n");
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
                                System.out.println("Registered user " + username + " with password " + password + "\n");
                                connected = listen();
                            }
                        } else if (action.equals("LOGIN") && !LOGIN) {
                            this.sout.writeBoolean(false);
                            this.sout.flush();
                            if(test)
                                System.out.println("Username not found in database. Try again\n");
                            continue;
                        }
                    }

                } catch (Exception ae) {
                    System.out.println("Client Thread exception caught\n");
                    return;
                }
            }

        }

        public synchronized void getBroadcast(String msg) throws IOException {
            this.sout.writeUTF("BROADCAST");
            this.sout.flush();
            this.sout.writeUTF(msg);
            this.sout.flush();
            if(test)
                System.out.println("Broadcast message " + msg + "sent to user " + this.user.get_name() + "\n");
        }
    }


    public static void main(String[] args) {
        try {
            if (args[0].equals("test")) {
                Server server = new Server(3000, true);
            } else {
                Server server = new Server(3000, false);
            }
        }
        catch(Exception e){
            e.printStackTrace();
            }

    }
}