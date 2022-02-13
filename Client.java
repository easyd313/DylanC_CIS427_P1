//Author: Dylan Crompton
//Date Modified: 2/11/2022
//Date Created: 1/22/2022
//Purpose: Geometry Solver Client
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Scanner;


public class Client {
    
    private static final int SERVER_PORT = 8728;

    public static void main(String[] args) {
        
        DataOutputStream toServer;
        DataInputStream fromServer;
        Scanner input = 
                new Scanner(System.in);
        String messageC;
        String messageS;
        
        //attempt to connect to the server
        try {
            Socket socket = 
                    new Socket("localhost", SERVER_PORT);
            
            //create input stream to receive data
            //from the server
            fromServer = 
                    new DataInputStream(socket.getInputStream());
            
            toServer =
                    new DataOutputStream(socket.getOutputStream());
            
             
             while(true) {
                System.out.print("C:\t");
                messageC = input.nextLine();
                toServer.writeUTF(messageC);
                
                
                //received message:
                messageS = fromServer.readUTF();
                System.out.println("S:\t" + messageS);
                if(messageC.equals("SHUTDOWN") && messageS.equals("200 OK")) {
                    socket.close();
                    break;
                }
             }
             
        }
        catch(IOException ex) {
            ex.printStackTrace();
        }//end try-catch
        
        
    }//end main
}
