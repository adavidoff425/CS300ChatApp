import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;
import java.io.*;

class ClientGUI extends JFrame implements ActionListener{
    private JButton register, login, logout, displayUsers;
    private JTextArea chat;
    private JTextField username, password;
    private JLabel text;
    private JList<String> onlineUsers;
    private JPanel buttonPanel;
    private Client client;
    private String server_address;
    private int port;
    
    public ClientGUI(String host, int port){
        super("Chat Application");
        this.server_address = host;
        this.port = port;
        this.client = new Client(host, port, this);
        
        try{
            this.register = new JButton("New User");
            this.login = new JButton("Existing User");
            this.buttonPanel = new JPanel();
            this.register.addActionListener(this);
            this.login.addActionListener(this);
            this.buttonPanel.add(this.register);
            this.buttonPanel.add(this.login);   
            this.add(buttonPanel);
        }
        catch(Exception e){
            e.printStackTrace();
        }
        this.setSize(400, 250);
        this.setVisible(true);
    }
    
    public void append(String string){
        
    }
    
    public void actionPerformed(ActionEvent e){
        
    }
    public static void main(String args){
        new ClientGUI("localhost", 1300);
    }
}