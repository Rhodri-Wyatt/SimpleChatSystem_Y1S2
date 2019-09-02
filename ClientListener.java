import java.io.IOException;
import java.net.Socket;
import java.io.BufferedReader;
import java.io.InputStreamReader;


/**
 * This thread is used to retrieve data from the server and print it out in the client.
 */
public class ClientListener implements Runnable {
    private Socket socket;
    private BufferedReader serverIn;

    /**
     * Constructor.
     *
     * Sets socket to socket passed into constructor and sets up server in.
     * serverIn gets the text from the socket sent from the server.
     *
     * @param socket
     */
    public ClientListener(Socket socket) {
        this.socket = socket;
        try {
            serverIn = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        } catch(IOException e) {
            System.err.println("failed to get input stream");
        }
    }


    /**
     * Method Purpose - get text from the server and print it out.
     *
     * note: ((line = serverIn.readLine()) != null) can also be written as
     * while(true) {
     *   line = serverIn.readLine();
     *   if (line == null) {
     *     break;
     *   }
     * }
     *
     * Keep getting text from serverIn and keep looping while the data from server in isn't null.
     * If flow has exited loop it means the server has been closed or the client has left the chat.
     *
     * This method helps satisfy client requirement 3 and 4
     *
     */
    public void run() {
        String line;
        try {
            //if line is null it means the server is closed.
            while ((line = serverIn.readLine()) != null) {
                System.out.println(line);
            }
            System.out.println("* System * - Either you have left the chat or server has been closed.");
            System.exit(0);
        } catch(IOException e) {
            System.out.println("* System * - Server has quit.");
            System.exit(0);
        }
    }

}
