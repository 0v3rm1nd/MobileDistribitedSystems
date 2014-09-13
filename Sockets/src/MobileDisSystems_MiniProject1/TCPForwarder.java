import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Created by Mind on 9/6/2014.
 */

/* TCPForwarder forwards traffic from a local TCP port to a destination host TCP port. There is support
 * for multiple client connections and a new thread (ClientThread) is created when a client connects,
 * that connects to the destination host and forwards data between the client and destination */
public class TCPForwarder {
    //the source port
    private static int source_port;
    //the destination host
    private static String dest_host;
    //the destination port
    private static int dest_port;

    public static int getDest_port() {
        return dest_port;
    }

    public static String getDest_host() {
        return dest_host;
    }

    public static void main(String[] args) throws IOException {

        if (args.length != 3) {
            System.err.println(
                    "Usage: java TCPForwarder <destination host> <source port> <destination port>");
            System.exit(1);
        }
//        get the data from the console and bind it to the appropriate variables
        dest_host = args[0];
        source_port = Integer.parseInt(args[1]);
        dest_port = Integer.parseInt(args[2]);

//        //hard code ports + dest host
//        dest_host = "itu.dk";
//        dest_port = 80;
//        source_port = 8080;

        ServerSocket serverSocket = new ServerSocket(source_port);
        while (true) {
            Socket clientSocket = serverSocket.accept();
            //create a new client thread to support multiple connections + pass the client socket as an argument
            ClientThread clientThread =
                    new ClientThread(clientSocket);
            clientThread.start();
        }
    }
}

/*Responsible for forwarding between client and server. The forwarding is
done by two ForwardThread instances which allows for bidirectional forwarding*/
class ClientThread extends Thread {
    private Socket clientSocket;
    private Socket serverSocket;
    private boolean enableForwarding = false;

    //constructor
    public ClientThread(Socket clientsocket) {
        clientSocket = clientsocket;
    }

    /*
    Connects to the destination server and starts the forwarding of data between client and server*/
    public void run() {
        InputStream clientInStream;
        OutputStream clientOutStream;
        InputStream serverInStream;
        OutputStream serverOutStream;
        try {
            // Connect to the destination server
            serverSocket = new Socket(TCPForwarder.getDest_host(), TCPForwarder.getDest_port());
            // get client an server input and output streams
            clientInStream = clientSocket.getInputStream();
            clientOutStream = clientSocket.getOutputStream();
            serverInStream = serverSocket.getInputStream();
            serverOutStream = serverSocket.getOutputStream();
        } catch (IOException ioe) {
            System.err.println("An error occured connecting to " +
                    TCPForwarder.getDest_host() + "on port " +
                    TCPForwarder.getDest_port());
            //close connections
            closeConnections();
            return;
        }

        // start the forwarding between the server and the client
        // --> 2 threads are needed for that purpose
        enableForwarding = true;
        //create a new client forward thread
        ForwardThread clientForward =
                new ForwardThread(this, clientInStream, serverOutStream);
        clientForward.start();
        //create a new server forward thread
        ForwardThread serverForward =
                new ForwardThread(this, serverInStream, clientOutStream);
        serverForward.start();

        System.out.println("TCPForward between " +
                clientSocket.getInetAddress().getHostAddress() +
                ":" + clientSocket.getPort() + " and " +
                serverSocket.getInetAddress().getHostAddress() +
                ":" + serverSocket.getPort() + " started.");
    }


    //indicates that a connection is terminated and both client and server sockets must be closed
    //synchronized because multiple threads will be accessing it potentially at the same time
    public synchronized void closeConnections() {
        try {
            serverSocket.close();
        } catch (Exception e) {
        }
        try {
            clientSocket.close();
        } catch (Exception e) {
        }

        if (enableForwarding) {
            System.out.println("TCPForward between " +
                    clientSocket.getInetAddress().getHostAddress()
                    + ":" + clientSocket.getPort() + " and " +
                    serverSocket.getInetAddress().getHostAddress()
                    + ":" + serverSocket.getPort() + " stopped.");
            enableForwarding = false;
        }
    }
}

/*Manages the TCP forwarding between a source input stream and destination output stream
the input stream is read and forwarded to the output stream  */
class ForwardThread extends Thread {
    private static final int BUFFER_SIZE = 8192;

    InputStream inputStream;
    OutputStream outputStream;
    ClientThread parentThread;

    //constructor
    public ForwardThread(ClientThread parent, InputStream
            inStream, OutputStream outStream) {
        parentThread = parent;
        inputStream = inStream;
        outputStream = outStream;
    }

    //reads input stream and writes to the output stream
    public void run() {
        byte[] buffer = new byte[BUFFER_SIZE];
        try {
            while (true) {
                int bytesRead = inputStream.read(buffer);
                if (bytesRead == -1)
                    // end of stream is reached + exit loop
                    break;
                outputStream.write(buffer, 0, bytesRead);
                outputStream.flush();
            }
        } catch (IOException e) {
            // e.printStackTrace();
        }

        // notify parent if the connection is broken for some reason
        parentThread.closeConnections();
    }
}
