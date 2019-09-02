import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

/**
 * Class creates server and sends off threads to check for exit on server and a thread for
 * the interaction with each client.
 */
public class ChatServer {

    private ServerSocket serverSocket;
    private ArrayList<Socket> listOfClientSockets = new ArrayList<>();
    //ServerClientInteraction threads are added to this list.
    private  ArrayList<ServerClientInteraction> listOfServerClientInteractions = new ArrayList<>();

    /**
     * Constructor.
     * Purpose - sets up server socket with the port passed into constructor.
     *
     * It instantiates the serverSocket for a specific port.
     * If port is already in use then the constructor will not be created and the Catch in the main method will be used.
     *
     * @param port
     */
    public ChatServer(int port) {
        try {
            //instantiate socket with given port
            serverSocket = new ServerSocket(port);
        } catch (IOException e) {
            //Exception if port is already in use.
        }
    }


    /**
     * Method Purpose - Send off the exitThread, Allow multiple clients to be accepted by the server socket and send of the
     * ServerClientInteraction thread.
     *
     * An exit thread is sent off. This thread allows the user to type "EXIT" into the server.
     * An exit thread is needed as serverSocket.accept() makes the server waits until another socket is accepted.
     * This means it would not recognise the user type "EXIT" on this thread, hence why the exit thread is sent off.
     *
     * A loop is created to allow multiple clients to connect to the server.
     * In this loop the serverSocket is kept waiting until it accepts a new client.
     * A thread of the ServerClientInteraction is created and is then sent out.
     * This thread handles all the messages sent from the clients and sends it to all the other clients.
     *
     * this method satisfies server requirements 1 and 2
     */
    public void begin() {
        //create thread to check if user wants to exit.
        //this = instance of chatServer so that the ServerCheckExit can call the ChatServer methods.
        Thread exitThread = new Thread(new ServerCheckExit(this));
        exitThread.start();

        try {
            //notifies user if the server has been set up correctly
            if (serverSocket.isBound()) {
                System.out.println("Server is bound on port: " + serverSocket.getLocalPort());
            }

            System.out.println("Server Listening for Client...");

            while (true) {
                //The server is listening, unless a connection it will not go past the line.
                //The socket waits for a connection on the port.
                Socket socket = serverSocket.accept();

                //if this line is reached this means there is a connection.
                System.out.println("Server accepted connection on " + serverSocket.getLocalPort() + " ; " + socket.getPort());

                //Creating a thread out of the instance of the server client interaction object
                //Server client interaction implements runnable so the thread constructor can accept it as an argument.
                //setUpClientInteraction() method is called to create the server client interaction object to be sent out as a thread.
                Thread clientInteractionThread = new Thread( setUpServerClientInteraction(socket));

                //when starting a thread call start method. this automatically calls the run method of that thread.
                clientInteractionThread.start();
            }
        } catch (IOException e) {
            //This exception is caught when the server is closed. As the serverSocket is stopped from being on accept.
            //Nothing needs to be here as it will allow the server to close cleanly.
        }
    }


    /**
     * Method purpose - Add new socket to arrayList and set up ServerClientInteraction Object to send out.
     *
     * @param socket
     * @return server client interaction object
     */
    private ServerClientInteraction setUpServerClientInteraction(Socket socket){

        //adds socket to the arraylist
        listOfClientSockets.add(socket);

        //object of Server Client Interaction is created
        //once you've made an instance of a thread you can run the thread itself.
        ServerClientInteraction sci = new ServerClientInteraction(socket, this);
        //add the new Server Client Interaction object to the arraylist
        addServerClientInteraction(sci);
        return sci;
    }


    /**
     * Method Purpose - remove a client interaction object from the arraylist
     * Method is synchronised to stop multiple threads trying to remove an item from the array list at the same time.
     * the synchronising of methods make the server thread-safe
     * @param sci
     */
    public synchronized void removeServerClientInteraction(ServerClientInteraction sci){
        listOfServerClientInteractions.remove(sci);
    }


    /**
     * Method Purpose - add a client interaction object from the arraylist
     * Note that method is synchronized.
     * @param sci
     */
    private synchronized void addServerClientInteraction(ServerClientInteraction sci){
        listOfServerClientInteractions.add(sci);
    }


    /**
     * Method Purpose - Retrieve the list of Client Sockets
     * Synchronizing a method means an error wont happen if one thread tries to access the list while another tries to
     * remove itself from the list.
     * @param
     */
    public synchronized ArrayList<Socket> getListOfClientSockets(){
        return listOfClientSockets;
    }


    /**
     * Method Purpose - remove a socket from the arraylist
     * Note that method is synchronized.
     * @param socket
     */
    public synchronized void removeListOfClientSockets(Socket socket){
        listOfClientSockets.remove(socket);
    }


    /**
     * Purpose - Disconnect all the client's from the server. Close the Server Socket and end the program.
     *
     * Loops through all the clients in the arrayList storing the ServerClientInteraction threads.
     * It calls the closeSocket method of the client interaction thread accessed from the arraylist to close the thread.
     * The serverSocket is then closed.
     * After this is done System.exit shuts down the program.
     *
     * This method helps satisfies requirement 6 as the server shuts down cleanly.
     *
     */
    public synchronized void terminateSession() {
        System.out.println("Closing Server");
        try {
            for (int i = 0; i < listOfServerClientInteractions.size(); i++) {
                listOfServerClientInteractions.get(i).closeSocket();
            }
            serverSocket.close();
        } catch (IOException e) {
            System.err.println("Error in terminateSession method caught here");
            e.printStackTrace();
        }

        System.out.println("Server Successfully Shut Down");
        //this cleanly shuts down the system.
        //meaning it should leave the while(true) loop in begin method and close
        System.exit(0);
    }


    /**
     * Purpose - call the constructor based on the port defined passed into the program via arguments.
     *
     * It checks if extra argument added at runtime is "-csp" and if the next argument isn't null then the port is
     * changed to be the next argument included.
     * If this is not the case or if the argument for the port is not an integer then the default port is set to 14001.
     * If there is a problem with the construction of the chat server then the program is stopped in order to allow the
     * user to connect with a different port.
     *
     * This method satisfies requirement 7 and 9 from the Server Specification.
     *
     * @param args
     */
    public static void main(String[] args) {
        int port = 14001;

        if (args.length != 0) {
            //loops through every argument passed at runtime
            for (int i = 0; i < args.length; i++) {
                //if the argument is "-csp" and the following argument is not empty
                if (args[i].equals("-csp") && !args[i + 1].isEmpty()) {
                    try{
                        //changes port to be the argument passed after -csp
                        port = Integer.parseInt(args[i + 1]);
                    } catch (Exception e){
                        System.out.println("Insufficient port. Server will be created with default port.");
                        port = 14001;
                    }
                }
            }
        }
        try{
            //call the ChatServer constructor
            new ChatServer(port).begin();
        } catch (Exception e){
            //allows user to try a different port if the server could not be set up correctly.
            System.out.println("Could not set up server as port already in use. Please run server again but with a different port. This can be done via the argument -csp");
            System.exit(0);
        }
    }
}
