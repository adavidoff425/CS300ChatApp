import java.io.*;
import java.util.*;
import java.net.Socket;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;

class ClientGUI extends Client implements ListSelectionListener, ActionListener{
    private GroupLayout layout;
    private JButton register, login, login2, logout, displayUsers, displayHistory, enter, send, exit, clear;
    private JTextArea chat, msg, text;
    private JTextField username, password, username2, password2;
    private JList<String> onlineUsers;
    private JScrollPane users, history;
    private JPanel buttonPanel, registerPanel, loginPanel, runningPanel, chatPanel;
    private String name, pw;

    public ClientGUI(String host, int port){
        super(host, port);
        this.gui = this;

        this.buttonPanel = new JPanel();
        this.layout = new GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        this.layout.setAutoCreateGaps(true);
        this.layout.setAutoCreateContainerGaps(true);
        
        try{
            // Initial panel
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
        }
        catch(Exception e){
            e.printStackTrace();
        }

        this.layout.setHorizontalGroup(layout.createSequentialGroup()
                    .addGroup(layout.createParallelGroup()
                        .addComponent(buttonPanel))
                    .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING))
                    .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                        .addComponent(registerPanel))
                    .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING))
                    .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                        .addComponent(loginPanel)
                        .addComponent(runningPanel))
                    .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                        .addComponent(users))
                    .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                        .addComponent(chatPanel))
                  //      .addComponent(history))
                    .addGroup(layout.createParallelGroup(GroupLayout.Alignment.CENTER)
                        .addComponent(text))
        );
        
        this.layout.setVerticalGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup()
                    .addComponent(buttonPanel))
                    .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                        .addComponent(registerPanel)
                        .addComponent(loginPanel))
                    .addGroup(layout.createParallelGroup(GroupLayout.Alignment.CENTER)
                        .addComponent(runningPanel))
                    .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                        .addComponent(users)
                        .addComponent(chatPanel))
                    //    .addComponent(history))
                    .addGroup(layout.createParallelGroup(GroupLayout.Alignment.CENTER)
                        .addComponent(text))
        );
        
        this.setSize(1000, 800);
        this.layout.setHonorsVisibility(true);
        connect();
        this.setVisible(true);
    }
    
    public void append(String string){
        this.text.append(string);
    }
    
    public void actionPerformed(ActionEvent e) {
        try {
            Object source = e.getSource();
            if (source == this.register) {
                this.buttonPanel.setVisible(false);
                this.registerPanel.setVisible(true);
                this.layout.replace(this.buttonPanel, this.registerPanel);
            } else if (source == this.login) {
                this.sout.writeUTF("LOGIN");
                this.sout.flush();
                this.loginPanel.setVisible(true);
                this.buttonPanel.setVisible(false);
                this.layout.replace(this.buttonPanel, this.loginPanel);
                //login();
            } else if (source == this.logout) {
                //logout();
                this.layout.replace(this.runningPanel, this.buttonPanel);
                this.chatPanel.setVisible(false);
                this.users.setVisible(false);
                this.history.setVisible(false);
                this.buttonPanel.setVisible(true);
            } else if (source == this.displayUsers) {
                this.users.setVisible(true);
                // userList();
            } else if (source == this.enter) {
                this.sout.writeUTF("REGISTER");
                this.sout.flush();
                try{
                    String name = this.username.getText();
                    String pw = this.password.getText();
                }
                catch(Exception te)
                    {append("Error retrieving entered text");}
                if(registerUser(name, pw)) {
                    this.registerPanel.setVisible(false);
                    this.runningPanel.setVisible(true);
                    this.layout.replace(this.registerPanel, this.runningPanel);
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
        this.registerPanel.setVisible(false);
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
        this.loginPanel.setVisible(false);
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
        this.runningPanel.setVisible(false);
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
        this.chatPanel.setVisible(false);
    }

    public void usersScreen(){
        this.onlineUsers = new JList<String>();
        this.onlineUsers.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        this.onlineUsers.setLayoutOrientation(JList.VERTICAL);
        this.onlineUsers.setVisibleRowCount(10);
        this.users = new JScrollPane(this.onlineUsers);
        this.onlineUsers.addListSelectionListener(this);
        this.users.setVisible(false);
    }

    public boolean registerUser(String name, String pw){

        System.out.println(name+pw);
        if(register(name, pw))
            return true;
        return false;
    }

    public void valueChanged(ListSelectionEvent event){}
}