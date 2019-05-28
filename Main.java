public class Main{
    public static void main(String[] args){
        Server server = new Server(2222);
        ClientGUI app = new ClientGUI("localhost", 2222);
    }
}