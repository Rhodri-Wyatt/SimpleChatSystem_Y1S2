import java.io.IOException;
import java.io.BufferedReader;
import java.io.InputStreamReader;

/**
 * Thread is used to allow the server to check if the server is asked to exit.
 * When thread is closed it means the user wishes to exit the server
 *
 * Class help satisfy requirements 6 for the server
 */
public class ServerCheckExit implements Runnable {

    //class now has reference to the chatServer
    //this allows the user to call the terminateSession method
    private ChatServer chatServer;

    /**
     * Constructor.
     * purpose - sets this chatServer to chatServer passed in in parameter.
     *
     * @param chatServer
     */
    public ServerCheckExit(ChatServer chatServer) {
        this.chatServer = chatServer;
    }


    /**
     * Method name: Run
     * purpose - Checks if "Exit" has been entered and calls terminateSession method in chatServer if true.
     *
     * This keeps looping as it means multiple things can be inputted into the Server but it is only run if EXIT
     */
    public void run() {
        //takes input from the keyboard
        BufferedReader serverInput = new BufferedReader(new InputStreamReader(System.in));
        String userInput = "";

        while (true) {
            try {
                userInput = serverInput.readLine();
            } catch (IOException e) {

            }

            if (userInput.equals("EXIT")) {
                chatServer.terminateSession();
                //break out of while loop and ends thread
                break;
            } else {
                System.out.println("To close server type EXIT");
            }
        }
    }

}
