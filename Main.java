public class Main{
    public static void main(String[] args){
        try {
            ClientGUI app = new ClientGUI("localhost", 3000);
        }
        catch(Exception e){}
    }
}