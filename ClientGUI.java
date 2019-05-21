class ClientGUI extends Client{
    private JButton register, login, logout, displayUsers, enter, send, loginAttempt;
    private JTextArea chat, msg;
    private JTextField username, password;
    private JLabel text;
    private JList<String> onlineUsers;
    private JPanel buttonPanel;:
    private String server_address, name, pw;
    private int port;
    
    public ClientGUI(String host, int port){
        super("Chat Application");
        this.server_address = host;
        this.port = port;
        
        try{
            this.register = new JButton("New User");
            this.login = new JButton("Existing User");
            this.logout = new JButton("Logout");
            this.displayUsers = new JButton("Display online users");
            this.buttonPanel = new JPanel();
            this.register.addActionListener(this);
            this.login.addActionListener(this);
            this.logout.addActionListener(this);
            this.logout.setEnabled(false);
            this.displayUsers.addActionListener(this);
            this.displayUsers.setEnabled(false);
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
        this.msg = new JTextArea(string);
    }
    
    public void actionPerformed(ActionEvent e){
        Object source = e.getSource();
        if(source == this.register)
            registerScreen(); 
        else if(source == this.login)
            login();
        else if(source == this.logout)
            logout();
        else if(source == this.displayUsers)
            userList();
        else if(source == this.enter)
            registerUser();
        
    }
    
    public void registerScreen(){
        this.login.setEnabled(false);
        this.register.setEnabled(false);
        this.username = new JTextField("Enter Username");
        this.password = new JTextField("Enter Password");
        this.username.setEditable(true);
        this.password.setEditable(true);
        this.enter = new JButton("Enter");
        this.enter.addActionListener(this);
        this.buttonPanel.add(this.username);
        this.buttonPanel.add(this.password);
        this.buttonPanel.add(this.enter);
        this.setvisible(true);
    }
    
    public void registerUser(){
        try{
            this.name = this.username.getText();
            this.pw = this.password.getText();
        }
        catch(Exception e)
            append("Error retrieving entered text");
        if(!register(this.name, this.pw))
            registerScreen();
    }
    
    public static void main(String args){
        new ClientGUI("localhost", 1300);
    }
}