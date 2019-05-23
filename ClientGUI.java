import java.io.*;
import java.util.*;
import java.net.Socket;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;

class ClientGUI extends Client implements ListSelectionListener, ActionListener{
    private GroupLayout layout;
    private JButton register, login, logout, displayUsers, displayHistory, enter, send, exit, clear;
    private JTextArea chat, msg;
    private JTextField username, password;
    private JLabel text;
    private JList<String> onlineUsers;
    private JScrollPane users;
    private JPanel buttonPanel, registerPanel, loginPanel, runningPanel, chatPanel;
    private String name, pw;

    public ClientGUI(String host, int port){
        super(host, port);
        super.gui = this;
        this.buttonPanel = new JPanel();
        this.layout = new GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        this.layout.setAutoCreateGaps(true);
        this.layout.setAutoCreateContainerGaps(true);
        
        try{
            // Initial panel
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
        }
        catch(Exception e){
            e.printStackTrace();
        }

        this.layout.setHorizontalGroup(
                this.layout.createSequentialGroup()
                    .addComponent(buttonPanel)
                    .addComponent(registerPanel)
                    .addComponent(runningPanel)
                    .addComponent(users)
                    .addComponent(chatPanel)
                    .addComponent(loginPanel)
        );
this.layout.setVerticalGroup(
                this.layout.createSequentialGroup()
                    .addComponent(buttonPanel)
                    .addComponent(registerPanel)
                    .addComponent(runningPanel)
                    .addComponent(users)
                    .addComponent(chatPanel)
                    .addComponent(loginPanel)
        );
        this.setSize(400, 250);
        this.setVisible(true);
    }
    
    public void append(String string){
        this.msg.append(string);
    }
    
    public void actionPerformed(ActionEvent e){
        Object source = e.getSource();
    /*    if(source == this.register)
        {}
        else if(source == this.login)
            login();
        else if(source == this.logout)
            logout();
        else if(source == this.displayUsers)
            userList();
        else if(source == this.enter)
            registerUser();
     */
    }
    
    public void registerScreen(){
        this.registerPanel = new JPanel();
        this.username = new JTextField("Enter Username");
        this.password = new JTextField("Enter Password");
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
        this.loginPanel.add(this.username);
        this.loginPanel.add(this.password);
        this.loginPanel.add(this.login);
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
        this.chat = new JTextArea(55, 55);
        this.msg = new JTextArea("Enter Message", 30, 30);
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
        this.onlineUsers = new JList<String>();
        this.onlineUsers.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        this.onlineUsers.setLayoutOrientation(JList.VERTICAL);
        this.onlineUsers.setVisibleRowCount(10);
        this.users = new JScrollPane(this.onlineUsers);
        this.onlineUsers.addListSelectionListener(this);
    }

    public void registerUser(){
        String name = null, pw = null;
        try{
            name = this.username.getText();
            pw = this.password.getText();
        }
        catch(Exception e)
            {append("Error retrieving entered text");}
        if(!register(name, pw))
            registerScreen();
    }

    public void valueChanged(ListSelectionEvent event){}
}