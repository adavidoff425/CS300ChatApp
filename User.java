import java.io.*;
import java.util.*;

public class User{
    private String username, password;
    private boolean loggedIn;
    private ArrayList<String> chatHistory = new ArrayList<>();
    private File historyfile;
    private FileWriter writer;
    private FileReader fileReader;
    private BufferedReader reader;
    
    public User(String name, String pw){
        this.username = name;
        this.password = pw;
        this.historyfile = new File(this.username + ".txt");
        if(!historyfile.exists()) {
            try{
                this.historyfile.createNewFile();
                this.writer = new FileWriter(this.historyfile);
                this.fileReader = new FileReader(this.historyfile);
                this.reader = new BufferedReader(fileReader);
            }
            catch(IOException e){
                System.out.println("Error reading/writing new user history file\n");
            }
        }
        else{
            try{
                this.writer = new FileWriter(this.historyfile, true);
                this.fileReader = new FileReader(this.historyfile);
                this.reader = new BufferedReader(fileReader);
            }
            catch(IOException e){
                System.out.println("Error reading/writing user history file\n");
            }
        }
        this.loggedIn = false;
    }
    
    public User loginAttempt(String name, String pw) throws FileNotFoundException{
        if(this.username.equals(name) && this.password.equals(pw)){
            getHistory();
            this.loggedIn = true;
            return this;
        }
        return null;
    }

    public boolean login(String name, String pw){
        return this.loggedIn = true;
    }
    
    public void logout(){
        this.loggedIn = false;
    }
    
    public ArrayList<String> retrieve_chatHistory(){
        return this.chatHistory;
    }

    public String get_name(){return this.username;}

    public void getHistory() throws FileNotFoundException{
        this.fileReader = new FileReader(historyfile);
        this.reader = new BufferedReader(fileReader);
    }
    
    public boolean isLoggedIn(){
        return this.loggedIn;
    }
    
    public void add_msg(User user, String msg){
        msg = user.username + ": " + msg;
        this.chatHistory.add(msg);
    }

    public boolean writeHistory() throws IOException{
        if(this.chatHistory.isEmpty())
            return false;
        for(String msgs : this.chatHistory){
            this.writer.write(msgs);
            this.writer.flush();
        }
        this.chatHistory.clear();
        return true;
    }
    
    public boolean find(String name){
        return this.username.equals(name);
    }

}