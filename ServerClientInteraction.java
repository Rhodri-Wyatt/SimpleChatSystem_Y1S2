import java.io.IOException;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

/**
 * Aim of thread is to take in data from the client and send it to all the clients from the arrayList.
 *
 * interface called runnable, it is pre defined and allows threads.
 * Expects you to implement run method as it is an abstract method.
 */
public class ServerClientInteraction implements Runnable {
    private Socket socket;
    private ChatServer chatServer;

    private boolean clientLeftChat = false;

    /**
     * Constructor.
     * Purpose - Sets the field values.
     *
     * @param socket
     */
    ServerClientInteraction(Socket socket, ChatServer chatServer) {
        //this. refers to the socket defined in the class.
        this.socket = socket;
        this.chatServer = chatServer;
    }


    /**
     * purpose - gets nickname, calls a method to notify all the clients that a new client has joined.
     * calls the getTextAndSendToClient.
     *
     * Sets up inputStreamReader and Buffered reader to get input from the socket.
     * Other methods are called.
     */
    public void run() {
        try {
            String nickname = "";
            //you get the input stream from the socket rather than the keyboard.
            InputStreamReader reader = new InputStreamReader(socket.getInputStream());
            // client In stored the data from the input stream
            BufferedReader clientIn = new BufferedReader(reader);
            //client out allows the thread to print to the client.
            PrintWriter clientOut;
            clientOut = new PrintWriter(socket.getOutputStream(), true);

            //get nickname for the client
            nickname = getNickname(clientIn, clientOut);
            //send message to all other clients that a new client has joined.
            sendNewClientMessage(nickname, clientOut);
            //this method gets text from the client and sends it to all the other clients.
            getTextAndSendToClients(nickname, clientIn,clientOut);

        } catch (IOException e) {

        }
    }


    /**
     * Method Purpose - Get a nickname for the new client that has joined.
     *
     * @param clientIn
     * @param clientOut
     * @return inputNickname
     */
    private String getNickname(BufferedReader clientIn,PrintWriter clientOut){
        String inputNickname = "";
        clientOut.println("* System * - Please enter a nickname for a client:");

        //input nickname from the client
        try{
            inputNickname = clientIn.readLine();
        } catch (Exception e){
            //Realistically, this should never happen.
            System.out.println("* System * - Could not get Nickname");
        }

        //if the user enters nothing then the user's nickname is set to anonymous client
        if (inputNickname.equals("")) {
            clientOut.println("* System * - No nickname entered.");
            inputNickname = "Anonymous Client";
        }
        clientOut.println("* System * - Nickname Set as " + inputNickname);
        clientOut.println("* System * - Welcome to the chat! To leave chat type *EXIT");

        return inputNickname;
    }


    /**
     * Method Purpose - send a message to all the clients on the chat that a new user has joined.
     *
     * @param nickname
     * @param clientOut
     */
    private void sendNewClientMessage(String nickname, PrintWriter clientOut){
        for (int i = 0; i < chatServer.getListOfClientSockets().size(); i++) {
            try {
                //instantiate client out for each socket
                clientOut = new PrintWriter(chatServer.getListOfClientSockets().get(i).getOutputStream(), true);
                //send message to everyone
                clientOut.println("* System * - " + nickname + " has joined the chat.");
            } catch (IOException e){
                //must be in a try catch to allow client out to work.
            }
        }
    }


    /**
     *
     * Loop keeps getting input from the socket.
     * If user input "*EXIT" user inputs then a boolean flag checking if client wants to leave chat is set to true.
     * The inputted text is then sent to all the clients in the listOfClients sockets
     * if the clientLeftChat boolean is true then the disconnect method is called.
     *
     * This method satisfies server requirements 3 and 4
     *
     * @param nickname
     * @param clientIn
     * @param clientOut
     */
    private void getTextAndSendToClients(String nickname, BufferedReader clientIn, PrintWriter clientOut){
        try {
            while (true) {
                String userInput = "";

                //.ready will return true if the client socket has data to read
                if (clientIn.ready()) {
                    //userInput is the text sent from the client.
                    userInput = clientIn.readLine();

                    if (userInput.equals("*EXIT")) {
                        clientLeftChat = true;
                    }

                    //loops through all the client sockets in the arrayList.
                    //getListOfClientSockets is a method in chatserver
                    for (int i = 0; i < chatServer.getListOfClientSockets().size(); i++) {

                        //Client out writes it back to the client. Server does not print anything out.
                        //client out is different for the different sockets.
                        clientOut = new PrintWriter(chatServer.getListOfClientSockets().get(i).getOutputStream(), true);

                        if (clientLeftChat) {
                            //output a closing message to all the users on the system.
                            clientOut.println("* System * - " + nickname + " has left the chat");
                        } else {
                            //This sends the message to the client.
                            clientOut.println(nickname + ": " + userInput);
                        }

                    }

                    //once all clients have been sent closing message, disconnect thread
                    if (clientLeftChat) {
                        System.out.println(nickname + " using " + socket + " has left the chat.");
                        disconnect();
                    }
                }
            }
        } catch (Exception e){
            //try catch is needed for getting data from the Client In
        }
    }


    /**
     * Method Purpose - Removes socket from the array list, removes server client interaction
     * threads from the arraylist and calls the close socket thread.
     *
     * it is called when the chat client wants to disconnect.
     *
     * This method allows for one or more clients to disconnect from the server satisfying requirement 5 for the server
     */
    public void disconnect() {
            //remove socket and server client interactions from the list by calling the appropriate methods in the chat server
            chatServer.removeListOfClientSockets(socket);
            chatServer.removeServerClientInteraction(this);
            //closes the socket
            closeSocket();
    }


    /**
     * Method Purpose - to close the socket
     */
    public void closeSocket(){
        try {
            socket.close();
            //print to server
            System.out.println(socket + " has been closed");

        } catch (IOException e){
            System.err.println("Server could not disconnect");
        }
    }

}
