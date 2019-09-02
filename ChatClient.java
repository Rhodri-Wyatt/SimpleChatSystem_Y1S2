//java.net contains network related methods

import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;

/**
 * Class is used to set up the ChatClient, send of the thread to listen for data from the server and to send messages
 * from the client to the server.
 */
public class ChatClient {
    //Socket is a an endpoint of a two way communication between two programs.
    //It is like a can in a tin-can-string telephone
    private Socket socket;

    // Printwriter is defined here to allow the sendMessage method to ;be
    private PrintWriter serverOut;

    //Create a nickname string to identify the client.
    //upon start up the client will be asked to enter in a nickname
    private String nickname = "";


    /**
     * Constructor.
     * Purpose - Create new socket based on given port and address. Send of client thread and define server out.
     *
     * A new socket is created from the port and address.
     * the client needs to know the address of server and the port to connect to.
     *
     * Before implementing threads i had a loop. this is now replaced by sending off threads that's sole job is to
     * listen to incoming data from the server and to print it out.
     *
     * This method helps satisfy client requirement 5
     *
     * @param address
     * @param port
     */
    public ChatClient(String address, int port) {
        try {
            socket = new Socket(address, port);

            //create clientListener object
            ClientListener cl = new ClientListener(socket);
            //send off thread
            new Thread(cl).start();

            //prints information out. This is used to send the data to the server.
            serverOut = new PrintWriter(socket.getOutputStream(), true);
        } catch (UnknownHostException e) {
            System.out.println("* System Error * - Host could not be found");
            // e.printStackTrace();
        } catch (IOException e) {
            System.out.println("* System Error * - Could not connect to server");
            // e.printStackTrace();
        }
    }


    /**
     * Method name: begin
     * Purpose - Gets text from keyboard and calls the sendMessage subroutine with that.
     *
     * While loop will continue to loop as long as they data retrieved from the keyboard isn't null.
     *
     * this method helps satisfy client requirement 1,2 and 6
     */
    public void begin() {
        try {
            //this means this code won't run if the client has not successfully be connected to the server.
            //This is all in order to make the code more robust.
            if (socket.isConnected()) {
                //takes input from the keyboard
                BufferedReader userIn = new BufferedReader(new InputStreamReader(System.in));
                //string is used to store the data from the keyboard
                String line;

                //while line contains data inputted from the keyboard are not null, sent it to the send message method.
                while ((line = userIn.readLine()) != null) {
                    sendMessage(line);
                }
            }else {
                System.out.println("* System * - Socket has not been connected");
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                //This will be executed only if the client was not able to connect to the server correctly.
                //In the rare example that the client receives null from the keyboard. The socket will be closed.
                //This is in order to make the code more robust.
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    /**
     * Method name: sendMessage
     * purpose - this method sends data to the server
     *
     * this method satisfies client requirement 2 and 6
     *
     * @param input
     */
    public void sendMessage(String input) {
        serverOut.println(input);
    }


    /**
     * Method name: main
     * purpose - gets the address and port passed in at runtime and calls the constructor with these arguments.
     *
     * It checks if extra argument added at runtime is "-ccp" or "-cca" and if the next argument isn't null then the
     * port is changed to be the next argument included.
     * If this is not the case or if the argument for the port is not an integer then the default port is set to 14001
     * and address to localhost.
     * If there is a problem with the construction of the chat client then the program is stopped in order to allow the
     * user to connect with a different port.
     *
     * This method satisfies client requirement 8,9,10,11 and 12
     *
     * @param args
     */
    public static void main(String[] args) {
         int port = 14001;
         String address = "localhost";

        if (args.length != 0) {
            //loops through all the arguments
            for (int i = 0; i < args.length; i++) {
                //if argument is "-ccp" and the next argument is not null then change port to following argument.
                if (args[i].equals("-ccp") && !args[i + 1].isEmpty()) {
                    try {
                        port = Integer.parseInt(args[i + 1]);
                    } catch (Exception e) {

                    }
                }
                //if the argument is "-cca" and next argument isn't null then change address to next argument given.
                if (args[i].equals("-cca") && !args[i + 1].isEmpty()) {
                    address = args[i + 1];
                }
            }
        }
        try{
            //call constructor
            new ChatClient(address, port).begin();
        } catch (Exception e){
            System.out.println("Please try to connect again with a different address and/or port. This can be done by passing the arguments -cca for address and -ccp for server.");
            System.exit(0);
        }

    }
}
