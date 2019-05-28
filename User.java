import java.io.*;
import java.util.*;

public class User{
    private String username, password;
    private boolean loggedIn;
    private ArrayList<String> chatHistory;
    
    public User(String name, String pw){
        this.username = name;
        this.password = pw;
        this.chatHistory = new ArrayList<>();
        this.loggedIn = false;
    }
    
    public boolean login(String name, String pw){
        if(this.username.equals(name) && this.password.equals(pw)){
            this.loggedIn = true;
            return true;
        }
        return false;
    }
    
    public void logout(){
        this.loggedIn = false;
    }
    
    public ArrayList<String> retrieve_chatHistory(){
        return this.chatHistory;
    }
    
    public boolean isLoggedIn(){
        return this.loggedIn;
    }
    
    public void add_msg(User user, String msg){
        msg = user.username + ": " + msg;
        this.chatHistory.add(msg);
    }
    
    public boolean find(String name){
        return this.username.equals(name);
    }
    
}