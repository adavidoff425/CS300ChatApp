import java.io.*;
import java.util.*;
import java.net.Socket;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;

class ClientGUI extends Client implements ListSelectionListener, ActionListener{
    private CardLayout layout, textLayout;
    private JPanel cards, active, textBox;
    private JButton register, login, login2, logout, displayUsers, displayHistory, enter, send, exit, exit2, clear, start;
    private JTextArea chat, msg, text, allmsgs;
    private JTextField username, password, username2, password2;
    private JList<String> onlineUsers;
    private DefaultListModel listModel;
    private JScrollPane users, history;
    private JPanel buttonPanel, registerPanel, loginPanel, runningPanel, chatPanel, historyPanel;
    final static String BUTTONPANEL = "Chat Application";
    final static String REGISTERPANEL = "Register New User";
    final static String LOGINPANEL = "Please Login";
    final static String RUNNINGPANEL = "Chat App Running";
    final static String CHATPANEL = "Active Chat";
    final static String USERS = "Active Users";
    final static String HISTORY = "User Chat History";
    final static String TEXT = "Message Area";
    final static String ACTIVE = "Logged into Application";
    //private MouseListener selection = new MouseAdapter();
    private String name, pw, currentUser;

    public ClientGUI(String host, int port){
        super(host, port);
        this.gui = this;

        this.layout = new CardLayout();
        this.textLayout = new CardLayout();

        this.cards = new JPanel(this.layout);
        this.textBox = new JPanel(this.textLayout);

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

            registerScreen();
            loginScreen();
            runningScreen();
            chatScreen();
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
        this.cards.add(this.chatPanel, CHATPANEL);
        this.cards.add(this.users, USERS);
        this.cards.add(this.historyPanel, HISTORY);
        this.textBox.add(this.text, TEXT);

        connect();
        this.add(this.cards, BorderLayout.CENTER);
        this.add(this.textBox, BorderLayout.NORTH);
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
                while(!name.equals("DONE")) {
                    this.listModel.addElement(name);
                    name = this.sin.readUTF();
                }
                this.layout.show(cards, USERS);
                // userList();
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
            }
        }
        catch(IOException ioe){
            append("Error sending action information to server\n");
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
        this.runningPanel.add(this.logout);
        this.runningPanel.add(this.displayUsers);
    }

    public void chatScreen(){
        this.chat = new JTextArea(20, 20);
        this.msg = new JTextArea("Enter Message", 20, 20);
        this.send = new JButton("Send");
        this.clear = new JButton("Clear");
        this.exit = new JButton("End Chat");
        this.send.addActionListener(this);
        this.clear.addActionListener(this);
        this.exit.addActionListener(this);
        this.chat.setEditable(false);
        this.msg.setEditable(true);
        this.chat.setLineWrap(true);
        this.msg.setLineWrap(true);
        this.chatPanel = new JPanel();
        this.chatPanel.add(this.msg);
        this.chatPanel.add(this.chat);
        this.chatPanel.add(this.send);
        this.chatPanel.add(this.clear);
        this.chatPanel.add(this.exit);
    }

    public void usersScreen(){
        this.listModel = new DefaultListModel();
        this.onlineUsers = new JList<String>(this.listModel);
        this.onlineUsers.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        this.onlineUsers.setLayoutOrientation(JList.VERTICAL);
        this.onlineUsers.setVisibleRowCount(10);
        this.onlineUsers.addListSelectionListener(this);
        this.start = new JButton("Start Chat");
        this.userPanel = new JPanel();
        this.userPanel.add(this.onlineUsers);
        this.userPAnel.add(this.start);
        //this.onlineUsers.addMouseListener(selection);
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
        this.currentUser = this.users.getSelectedValue();
    }
}