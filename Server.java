//Author: Dylan Crompton
//Date Modified: 2/11/2022
//Date Created: 1/22/2022
//Purpose: Geometry Solver Server
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Date;
import java.io.*;
import java.nio.file.Path;
import java.io.BufferedReader;
import java.io.FileReader;
import java.text.*;
import java.util.Scanner;
import java.io.FileNotFoundException;


public class Server {
    
    private static final int SERVER_PORT = 8728;
    
    private static String loginState = "";
    private static boolean serverUp = false;
    
    private static class LoginInfo {
        String username = "";
        String password = "";
    }
    
    private static LoginInfo[] loginList = new LoginInfo[4];
    
    
    public static void main(String[] args) {
        RetrieveLogins();
        createCommunicationLoop();
    }//end main
    

    
    public static void createCommunicationLoop() {
        try {
            //create server socket
            ServerSocket serverSocket = 
                    new ServerSocket(SERVER_PORT);
            
            System.out.println("Server started at " +
                    new Date() + "\n");
            //listen for a connection
            //using a regular *client* socket
            Socket socket = serverSocket.accept();
            serverUp = true;
            
            //now, prepare to send and receive data
            //on output streams
            DataInputStream inputFromClient = 
                    new DataInputStream(socket.getInputStream());
            
            DataOutputStream outputToClient =
                    new DataOutputStream(socket.getOutputStream());
            
            //server loop listening for the client 
            //and responding
            while(serverUp) {
                String strReceived = inputFromClient.readUTF();
                System.out.println("CLIENT COMMAND RECIEVED: " + strReceived);
                String[] strSplit = strReceived.split("\\s+");
                
                if (!loginState.isEmpty()) {
                    switch(strSplit[0]) {
                        case "SOLVE": SolveGeo(strSplit, outputToClient);
                            break;
                        case "LIST": 
                            if (strSplit.length > 1) {
                                ListAllSolutions(strSplit, outputToClient);
                            } 
                            else {
                                outputToClient.writeUTF(ListSolutions(loginState));
                            }
                            break;
                        case "SHUTDOWN":
                            if (strSplit.length > 1) {
                                System.out.println("Unknown command received: " + strReceived);
                                outputToClient.writeUTF("301 message format error");
                                break;
                            } 
                            else {
                                System.out.println("Shutting down server...");
                                outputToClient.writeUTF("200 OK");
                                serverSocket.close();
                                socket.close();
                                serverUp = false;
                            }                           
                            break;
                        case "LOGOUT":
                            if (strSplit.length > 1) {
                                System.out.println("Unknown command received: " + strReceived);
                                outputToClient.writeUTF("301 message format error");
                                break;
                            } 
                            else {
                                System.out.println("LOGGING OUT USER: " + loginState);
                                outputToClient.writeUTF("200 OK");
                                loginState = "";
                            }
                            break;
                        default:   
                            System.out.println("Unknown command received: " + strReceived);
                            outputToClient.writeUTF("300 invalid command");
                            break;
                    }
                    
                }
                else {
                    if (strSplit[0].equals("LOGIN")) {
                        if (strSplit.length == 3) {                 //check the client input format
                            LoginUser(strSplit[1], strSplit[2], outputToClient);
                        }
                        else {
                            System.out.println("Unknown command received: " + strReceived);
                            outputToClient.writeUTF("FAILURE: Please provide correct username and password. Try again.");                  
                        }
                    }
                    else {
                        System.out.println("Unknown command received: " + strReceived);
                        outputToClient.writeUTF("300 invalid command");                  
                    }
                }                                           
            }//end server loop
        }
        catch(IOException ex) {
            ex.printStackTrace();
        }//end try-catch
    }//end createCommunicationLoop
    
    
    public static void RetrieveLogins() {
        BufferedReader reader;
        try {
            reader = new BufferedReader(new FileReader("C:/Users/easyd/OneDrive/Documents/SCHOOL/Winter 2022/CIS 427/Project1/Dylan_Crompton_p1/logins.txt"));
            String line = reader.readLine();
            
            int i = 0;
            LoginInfo currLogin = new LoginInfo();
            while (line != null) {                      //populate login list with logins from logins.txt
                String[] strSplit = line.split("\\s+");
                currLogin.username = strSplit[0];
                currLogin.password = strSplit[1];
                loginList[i] = currLogin;
                currLogin = new LoginInfo();
                line = reader.readLine();
                i++; 
            }
            reader.close();
//            for (LoginInfo login : loginList)       //display current login list to console
//            {
//                System.out.println(login.username);
//                System.out.println(login.password);
//            }
        }
        catch(IOException ex) {
            ex.printStackTrace();
        }//end try-catch
    }
    
    public static void LoginUser(String user, String pass, DataOutputStream outputToClient) {
        try {
            for(int i = 0; i < loginList.length; ++i) {     // search for user info in login list
                if (loginList[i].username.equals(user)) {
                    if (loginList[i].password.equals(pass)) {
                        loginState = user;
                        System.out.println("SUCCESS");
                        outputToClient.writeUTF("SUCCESS");
                    }
                }
            }
        
            if (loginState.equals("")) {   //checks if the login attempt was successful
                System.out.println("FAILURE: Unidentified login credentials.");
                outputToClient.writeUTF("FAILURE: Please provide correct username and password. Try again.");
            }
        }
        catch(IOException ex) {
            ex.printStackTrace();
        }//end try-catch
        
        
    }
    
    
    public static void SolveGeo(String[] commands, DataOutputStream outputToClient) {
        DecimalFormat df = new DecimalFormat("#.##");
        double area;
        double perimeter;
        double mes1;
        double mes2;
        
        
        try {
            FileWriter myWriter = new FileWriter(loginState + "_solutions.txt", true);
            
            if (commands.length == 1) {  //
                System.out.println("Error:  301 message format error");
                outputToClient.writeUTF("Error:  301 message format error");
                myWriter.write("Error:  301 message format error\n");
            }
            else if (commands.length == 2) {  //no sides or radius found
                if (commands[1].equals("-c")){
                    System.out.println("Error:  No radius found");
                    outputToClient.writeUTF("Error:  No radius found");
                    myWriter.write("Error:  No radius found\n");
                }
                else if (commands[1].equals("-r")) {        
                    System.out.println("Error:  No sides found");
                    outputToClient.writeUTF("Error:  No sides found");
                    myWriter.write("Error:  No sides found\n");
                }
                else {      
                    System.out.println("Error:  301 message format error");
                    outputToClient.writeUTF("Error:  301 message format error");
                    myWriter.write("Error:  301 message format error\n");
                }
            }
            else if (commands.length > 4) {  //input was incorrect
                if (commands[1].equals("-c")){
                    System.out.println("Error:  301 message format error");
                    outputToClient.writeUTF("Error:  301 message format error");
                    myWriter.write("Error:  301 message format error\n");
                }
                else if (commands[1].equals("-r")) {        
                    System.out.println("Error:  301 message format error");
                    outputToClient.writeUTF("Error:  301 message format error");
                    myWriter.write("Error:  301 message format error\n");
                }
                else {      
                    System.out.println("Error:  301 message format error");
                    outputToClient.writeUTF("Error:  301 message format error");
                    myWriter.write("Error:  301 message format error\n");
                }            
            }
            else if (commands.length == 4 && commands[1].equals("-c")) {    //input was incorrect based on type
                System.out.println("Error:  301 message format error");
                outputToClient.writeUTF("Error:  301 message format error");
                myWriter.write("Error:  301 message format error\n");
            }
            else {
                if (commands[1].equals("-c")){ //this solves correctly formatted circle area
                    mes1 = Integer.parseInt(commands[2]);
                    area = Math.PI * Math.pow(mes1, 2);
                    perimeter = 2 * Math.PI * mes1;
                    outputToClient.writeUTF("Circle's circumference is " + df.format(perimeter) + " and area is " + df.format(area));
                    myWriter.write("radius " + commands[2] + ":  Circle's circumference is " + df.format(perimeter) + " and area is " + df.format(area) + "\n");
                }
                else if (commands[1].equals("-r")) {    //this solves correctly formatted rectangle area
                    mes1 = Integer.parseInt(commands[2]);
                    if (commands.length == 4) {
                        mes2 = Integer.parseInt(commands[3]);
                        area = mes1 * mes2;
                        perimeter = 2 * (mes1 + mes2);
                        myWriter.write("sides " + commands[2] + " " + commands[3] + ":  Rectangle's perimeter is " + df.format(perimeter) + " and area is " + df.format(area) + "\n");
                    }
                    else {  //rectangle is a square
                        mes2 = mes1;
                        area = mes1 * mes2;
                        perimeter = 2 * (mes1 + mes2);
                        myWriter.write("sides " + commands[2] + " " + commands[2] + ":  Rectangle's perimeter is " + df.format(perimeter) + " and area is " + df.format(area) + "\n");
                    }
                    area = mes1 * mes2;
                    perimeter = 2 * (mes1 + mes2);
                    
                    outputToClient.writeUTF("Rectangle's perimeter is " + df.format(perimeter) + " and area is " + df.format(area));
                    
                }
                else {
                    System.out.println("Error:  No shape type found");
                    outputToClient.writeUTF("Error:  301 message format error");
                    myWriter.write("Error:  301 message format error\n");
                }    
            }
            
            myWriter.close();
        }
        catch (NumberFormatException nfe) {
            System.out.println("Error:  Shape measurements must be numeric");
            try {
                FileWriter myWriter = new FileWriter(loginState + "_solutions.txt", true);
                outputToClient.writeUTF("Error:  301 message format error");
                myWriter.write("Error:  301 message format error\n");
                myWriter.close();
            }
            catch(IOException ex) {
                ex.printStackTrace();
            }//end try-catch
        }
        catch(IOException ex) {
            ex.printStackTrace();
        }//end try-catch
        

        
    }
    public static String ListSolutions(String currUser) { 
        String fileData = "";
        
        try {
            System.out.println(currUser);
            fileData = fileData + currUser + "\n";
            
            File myObj = new File(currUser + "_solutions.txt");
            Scanner myReader = new Scanner(myObj);
            while (myReader.hasNextLine()) {  //append each line from file to return string
              String data = myReader.nextLine();
              System.out.println("\t" + data);
              fileData = fileData + "\t\t" + data + "\n";
            }
            
            
            myReader.close();
            return fileData;
        }
        catch (FileNotFoundException e) {
            System.out.println("\tNo interactions yet");
            return currUser + "\n" + "\t\tNo interactions yet\n";
        }
        catch(IOException ex) {
            ex.printStackTrace();
            return "\tError: Exception thrown\n";
        }
    }
    
    public static void ListAllSolutions(String[] commands, DataOutputStream outputToClient) {
        String fullList = "";
        
        try {
            if (commands.length == 2) {
                if (commands[1].equals("-all")) {
                    if (loginState.equals("root")) {
                        for (int i = 0; i < loginList.length; ++i) {
                            fullList += ListSolutions(loginList[i].username) + "\t";
                        }
                        outputToClient.writeUTF(fullList);
                    }
                    else {
                        System.out.println("Error: you are not the root user");
                        outputToClient.writeUTF("Error: you are not the root user");
                    }
                }
                else {
                    System.out.println("Error: Command not recognized");
                    outputToClient.writeUTF("301 message format error");
                }
            }
            else {
                System.out.println("Error: Command not recognized");
                outputToClient.writeUTF("301 message format error");
            }
        }
        catch(IOException ex) {
            ex.printStackTrace();
        }
    }
    
}
