import java.io.*;
import java.util.*;
import java.net.Socket;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;

class ClientGUI extends Client implements ListSelectionListener, ActionListener, ChangeListener{
    private CardLayout layout, textLayout;
    private JPanel cards, textBox, BroadcastTab;
    private JButton register, login, login2, logout, displayUsers, displayHistory, enter, send, exit, exit2, clear, start;
    private JTextArea chat, msg, text, allmsgs;
    private JTextField username, password, username2, password2;
    private JList<String> onlineUsers;
    private DefaultListModel listModel;
    private JScrollPane users, history, chatScroll;
    private JPanel buttonPanel, registerPanel, loginPanel, runningPanel, chatPanel, historyPanel, userPanel;
    private JTabbedPane chatTabs;
    private ArrayList<JButton> sends, clears, exits;
    private ArrayList<JLabel> withs;
    private ArrayList<JTextArea> msgs, chats;
    private int tab;
    final static String BUTTONPANEL = "Chat Application";
    final static String REGISTERPANEL = "Register New User";
    final static String LOGINPANEL = "Please Login";
    final static String RUNNINGPANEL = "Chat App Running";
    final static String CHATPANEL = "Chat Area";
    final static String USERS = "Active Users";
    final static String HISTORY = "User Chat History";
    final static String TEXT = "Message Area";
    //private MouseListener selection = new MouseAdapter();
    private String name, pw, currentUser;

    public ClientGUI(String host, int port){
        super(host, port);
        this.gui = this;

        this.layout = new CardLayout();
        this.textLayout = new CardLayout();

        this.cards = new JPanel(this.layout);
        this.textBox = new JPanel(this.textLayout);
        this.chatTabs = new JTabbedPane();
        this.chatTabs.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
        this.chatTabs.addChangeListener(this);

        try{
            // Initial panel
            this.buttonPanel = new JPanel();
            this.text = new JTextArea("");
            this.text.setEditable(false);
            this.register = new JButton("New User");
            this.login = new JButton("Existing User");
            this.register.addActionListener(this);
            this.login.addActionListener(this);
            this.buttonPanel.add(this.register);
            this.buttonPanel.add(this.login);
            this.broadcastTab = new JPanel();

            registerScreen();
            loginScreen();
            runningScreen();
            usersScreen();
            historyScreen();
        }
        catch(Exception e){
            e.printStackTrace();
        }

        this.cards.add(this.buttonPanel, BUTTONPANEL);
        this.cards.add(this.registerPanel, REGISTERPANEL);
        this.cards.add(this.loginPanel, LOGINPANEL);
        this.cards.add(this.runningPanel, RUNNINGPANEL);
        this.cards.add(this.users, USERS);
        this.cards.add(this.historyPanel, HISTORY);
        this.textBox.add(this.text, TEXT);
        this.chatTabs.add(this.broadcastTab);
        this.sends = new ArrayList<>();
        this.clears = new ArrayList<>();
        this.exits = new ArrayList<>();
        this.withs = new ArrayList<>();
        this.msgs = new ArrayList<>();
        this.chats = new ArrayList<>();

        connect();
        this.add(this.cards, BorderLayout.CENTER);
        this.add(this.textBox, BorderLayout.NORTH);
        this.add(this.chatTabs, BorderLayout.EAST);
        this.pack();
        this.setSize(1000, 750);
        this.setVisible(true);
    }
    
    public void append(String string){
        this.text.append(string);
    }

    public void actionPerformed(ActionEvent e) {
        try {
            Object source = e.getSource();
            if (source == this.register) {
                this.layout.show(cards, REGISTERPANEL);

            } else if (source == this.login) {
                this.sout.writeUTF("LOGIN");
                this.sout.flush();
                this.layout.show(cards, LOGINPANEL);

            } else if (source == this.logout) {
                this.sout.writeUTF("LOGOUT");
                this.sout.flush();
                this.layout.show(cards, BUTTONPANEL);

            } else if (source == this.displayUsers) {
                String name = new String();
                this.sout.writeUTF("USERS");
                this.sout.flush();
                name = this.sin.readUTF();
                while (!name.equals("DONE")) {
                    this.listModel.addElement(name);
                    name = this.sin.readUTF();
                }
                this.layout.show(cards, USERS);

            } else if (source == this.displayHistory){
                String msg = new String();
                this.sout.writeUTF("HISTORY");
                this.sout.flush();
                msg = this.sin.readUTF();
                while(!msg.equals("DONE")){
                    this.allmsgs.append(msg);
                    msg = this.sin.readUTF();
                }
                this.layout.show(cards, HISTORY);

            } else if (source == this.enter) {
                this.sout.writeUTF("REGISTER");
                this.sout.flush();
                try {
                    this.name = new String(this.username.getText());
                    this.pw = new String(this.password.getText());
                } catch (Exception te) {
                    append("Error retrieving register screen text\n");
                }
                if (registerUser(this.name, this.pw)) {
                    System.out.println("Registered" + this.name + "\n");
                    this.username.setText("Enter username: ");
                    this.password.setText("Enter password: ");
                    this.layout.show(this.cards, RUNNINGPANEL);
                }

            } else if (source == this.login2){
                try{
                    this.name = new String(this.username2.getText());
                    this.pw = new String(this.password2.getText());
                }
                catch (Exception le){
                    append("Error retrieving login screen text\n");
                }
                if (loginAttempt(this.name, this.pw)) {
                    append("Logged in as " + this.name + "\n");
                    this.username2.setText("Enter username: ");
                    this.password2.setText("Enter password: ");
                    this.layout.show(this.cards, RUNNINGPANEL);
                }

            } else if (source == this.start){
                if(this.currentUser.equals(this.name) || this.onlineUsers.isSelectionEmpty()) {
                    this.sout.writeUTF("BROADCAST");
                    this.sout.flush();
                }
                else {
                    this.sout.writeUTF("START");
                    this.sout.flush();
                    this.sout.writeUTF(this.currentUser);
                    this.sout.flush();
                }
                JScrollPane tab = newChat(this.currentUser);
                this.chatTabs.add(this.currentUser, tab);
                this.currentUser = null;

            } else if (source == this.exit2) {
                this.sout.writeUTF("EXIT");
                this.sout.flush();
                this.layout.show(this.cards, RUNNINGPANEL);

            } else if (source == this.sends.get(this.tab)){
                this.sout.writeUTF("SENDMSG");
                this.sout.flush();
                this.sout.writeUTF(this.withs.get(this.tab).getText());
                this.sout.flush();
                String message = new String(this.msgs.get(this.tab).getText());
                if(message.equals("Enter Message"))
                    message = "";
                else
                    message = this.name + ": " + message;
                this.sout.writeUTF(message);
                this.sout.flush();

            } else if (source == this.clears.get(this.tab)){
                this.msgs.get(this.tab).setText("Enter Message");

            } else if (source == this.exits.get(this.tab)){
                this.sout.writeUTF("WRITE");
                this.sout.flush();
                String done = new String(this.sin.readUTF());
                if(done.equals("DONE")) {
                    this.withs.remove(this.tab);
                    this.msgs.remove(this.tab);
                    this.sends.remove(this.tab);
                    this.clears.remove(this.tab);
                    this.exits.remove(this.tab);
                    this.chatTabs.remove(this.tab + 1);
                }
            }
        }
        catch(IOException ioe){
            append("Error sending action information to server\n");
        }
        try {
            listen();
        }
        catch(IOException be){
            append("Error listening on server\n");
        }
    }
    
    public void registerScreen(){
        this.registerPanel = new JPanel();
        this.username = new JTextField("Enter Username");
        this.password = new JTextField("Enter Password");
        this.username.addActionListener(this);
        this.password.addActionListener(this);
        this.username.setEditable(true);
        this.password.setEditable(true);
        this.enter = new JButton("Enter");
        this.enter.addActionListener(this);
        this.registerPanel.add(this.username);
        this.registerPanel.add(this.password);
        this.registerPanel.add(this.enter);
    }

    public void loginScreen(){
        this.loginPanel = new JPanel();
        this.username2 = new JTextField("Enter Username");
        this.password2 = new JTextField("Enter Password");
        this.login2 = new JButton("Login");
        this.username2.addActionListener(this);
        this.password2.addActionListener(this);
        this.username2.setEditable(true);
        this.password2.setEditable(true);
        this.login2.addActionListener(this);
        this.loginPanel.add(this.username2);
        this.loginPanel.add(this.password2);
        this.loginPanel.add(this.login2);
    }

    public void runningScreen() {
        this.runningPanel = new JPanel();
        this.logout = new JButton("Logout");
        this.displayUsers = new JButton("Display Online Users");
        this.displayHistory = new JButton("Chat History");
        this.logout.addActionListener(this);
        this.displayUsers.addActionListener(this);
        this.displayHistory.addActionListener(this);
        this.runningPanel.add(this.logout);
        this.runningPanel.add(this.displayUsers);
        this.runningPanel.add(this.displayHistory);
    }

    public JScrollPane newChat(String chatWith){
        if(chatWith.equals(""))
            chatWith = "ALL";
        JPanel panel = new JPanel();
        JTextArea chat = new JTextArea(20, 20);
        JTextArea msg = new JTextArea("Enter Message", 20, 20);
        JButton send = new JButton("Send");
        JButton clear = new JButton("Clear");
        JButton exit = new JButton("End Chat");
        JLabel with = new JLabel(chatWith);

        send.addActionListener(this);
        clear.addActionListener(this);
        exit.addActionListener(this);
        this.withs.add(with);
        this.msgs.add(msg);
        this.sends.add(send);
        this.clears.add(clear);
        this.exits.add(exit);
        this.chats.add(chat);

        chat.setEditable(false);
        msg.setEditable(true);
        chat.setLineWrap(true);
        msg.setLineWrap(true);

        panel.add(msg);
        panel.add(chat);
        panel.add(with);
        panel.add(send);
        panel.add(clear);
        panel.add(exit);
        JScrollPane chatScroll = new JScrollPane(panel);
        return chatScroll;
    }

    public void usersScreen(){
        this.listModel = new DefaultListModel();
        this.onlineUsers = new JList<String>(this.listModel);
        this.onlineUsers.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        this.onlineUsers.setLayoutOrientation(JList.VERTICAL);
        this.onlineUsers.setVisibleRowCount(10);
        this.onlineUsers.addListSelectionListener(this);
        this.start = new JButton("Start Chat");
        this.start.addActionListener(this);
        this.userPanel = new JPanel();
        this.userPanel.add(this.onlineUsers);
        this.userPanel.add(this.start);
        this.currentUser = new String();
        this.users = new JScrollPane(this.userPanel);
    }

    public void historyScreen(){
        this.historyPanel = new JPanel();
        this.exit2 = new JButton("Exit history");
        this.allmsgs = new JTextArea(80, 60);
        this.allmsgs.setEditable(false);
        this.allmsgs.setLineWrap(true);
        this.history = new JScrollPane(this.allmsgs);
        this.exit2.addActionListener(this);
        this.historyPanel.add(this.exit2);
        this.historyPanel.add(this.history);

    }

    public boolean registerUser(String name, String pw){

        if(register(name, pw))
            return true;
        return false;
    }

    public boolean loginAttempt(String name, String pw){
       if(login(name, pw))
           return true;
       return false;
    }

    public void valueChanged(ListSelectionEvent event){
        this.currentUser =  this.onlineUsers.getSelectedValue();
    }

    public class ListenThread extends Thread{
        final private Socket clientSocket;
        final private DataInputStream sin;
        final private DataOutputStream sout;

        public ListenThread(Socket socket, DataInputStream in, DataOutputStream out) throws ClassNotFoundException {
                this.clientSocket = socket;
                this.sin = new DataInputStream(in);
                this.sout = new DataOutputStream(out);
                String action = new String();

            while(connected) {
                action = this.sin.readUTF();
                listen(action);
            }
        }

        public synchronized void listen(String msg) throws IOException{
            if(msg.equals("BROADCAST")) {
                msg = this.sin.readUTF();
                append("New broadcasted message: Check broadcast tab\n");
                broadcastTab.append(msg);
            }
            else if(msg.equals("CHAT")){
                msg = this.sin.readUTF();
                if(msg.equals(name)) {
                    this.sout.writeBoolean(true);
                    this.sout.flush();
                    msg = this.sin.readUTF();
                    append("New chat starting with " + msg + "\n");
                    JScrollPane tab = newChat(msg);
                    chatTabs.add(msg, tab);
                }
                else{
                    this.sout.writeBoolean(false);
                    this.sout.flush();
                    this.sin.readUTF();
                }
            }
            else if(msg.equals("NEWMSG")){
                int index = -1;
                String user = new String();
                user = this.sin.readUTF();
                if(user.equals(name)){
                    user = this.sin.readUTF();
                    msg = this.sin.readUTF();
                    for(JLabel w : withs) {
                        if (w.getText().equals(user)) {
                            index = withs.indexOf(w);
                            break;
                        }
                    }
                    if(index > -1)
                        chats.get(index).append(user + ": " + msg + "\n");
                }
                else{
                    this.sin.readUTF();
                    this.sin.readUTF();
                }
            }
            this.socket.close();
            this.sin.close();
            this.sout.close();
        }
    }

    public void stateChanged(ChangeEvent e){
        this.tab = this.chatTabs.getSelectedIndex() - 1;
    }

}