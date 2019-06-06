public class Main{
    public static void main(String[] args){
        try {
            if(args[0].equals("test")) {
                new ClientGUI("localhost", 3000, true);
                System.out.println("Test starting\n");
            }
            else
                new ClientGUI("localhost", 3000, false);
        }
        catch(Exception e){e.printStackTrace();}
    }
}