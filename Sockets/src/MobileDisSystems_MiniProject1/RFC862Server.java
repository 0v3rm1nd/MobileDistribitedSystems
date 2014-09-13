import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.*;

/**
 * Created by Mind on 9/11/2014.
 */

/*Echo server that supports multiple UDP and TCP connections*/
public class RFC862Server {
    //flag
    private static boolean listening = true;

    public static void main(String[] args) throws IOException {
        //make a TCP server socket
        final ServerSocket TCP_Socket = new ServerSocket(7007);
        //make UDP server socket
        final DatagramSocket UDP_Socket = new DatagramSocket(7007);
        //thread to handle TCP specific connections
        Thread tcpListenThread = new Thread(new Runnable() {
            @Override
            public void run() {

                while (listening) {
                    // creates a new TcpSendThread when a new tcp connection is received
                    try {
                        new TcpSendThread(TCP_Socket.accept()).start();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                }
            }
        });
        //thread to handle UDP specific requests
        Thread udpListenThread = new Thread(new Runnable() {
            @Override
            public void run() {

                new UdpSendThread(UDP_Socket).start();

            }
        });
        //start the 2 listening threads for UDP + TCP
        tcpListenThread.start();
        udpListenThread.start();
    }


}

/*represents udp requests (UDP is connectionless) from a client*/
class UdpSendThread extends Thread {
    private DatagramSocket datagramSocket = null;
    //message sent form the client
    private static String clientString;

    public UdpSendThread(DatagramSocket datagramSocket) {
        super("UdpSendThread");
        this.datagramSocket = datagramSocket;
    }

    public void run() {
        try {
            handleUDP(datagramSocket);
        } catch (IOException ex) {
            ex.printStackTrace();

        }
    }

    /*responsible for handling all UDP traffic to the server*/
    static void handleUDP(DatagramSocket udpSocket) throws IOException {
        while (true) {
            //will represent the received data as bytes
            byte[] receiveData = new byte[1024];
            //sent data as bytes
            byte[] sendData = new byte[1024];
            //represent received packet
            DatagramPacket receivePacket = new DatagramPacket(receiveData,
                    receiveData.length);
            //waits until a packet is received form the client
            udpSocket.receive(receivePacket);
            //receive client string by extracting the data from the received packet
            clientString = new String(receivePacket.getData());
            System.out.println(receivePacket.getAddress().toString() + ":" + receivePacket.getPort() + " sent this message: " + clientString);
            //get the client ip and port
            InetAddress clientIPAddress = receivePacket.getAddress();
            int port = receivePacket.getPort();
            //build the data to be sent to the client
            sendData = clientString.getBytes();
            DatagramPacket sendPacket = new DatagramPacket(sendData,
                    sendData.length, clientIPAddress, port);
            //send data to client
            udpSocket.send(sendPacket);
        }
    }

}

/*represents a single tcp connection from a client*/
class TcpSendThread extends Thread {
    //message sent form the client
    private static String clientString;
    private Socket socket = null;

    public TcpSendThread(Socket socket) {
        super("TcpSendThread");
        this.socket = socket;
    }

    public void run() {
        try {
            handleTCP(socket);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    /*responsible for handling all TCP connections to the server*/
    static void handleTCP(Socket clientSocket) throws IOException {
        //get socket
        Socket connectionSocket = clientSocket;
        //input stream from client
        BufferedReader inFromClient = new BufferedReader(
                new InputStreamReader(connectionSocket.getInputStream()));
        //output stream to client
        DataOutputStream outToClient = new DataOutputStream(
                connectionSocket.getOutputStream());
        //message received from the client
        clientString = inFromClient.readLine() + "\n";
        System.out.println(connectionSocket.getInetAddress().toString() + ":" + connectionSocket.getPort() + " sent this message: " + clientString);
        //sent the modified message to client
        outToClient.writeBytes(clientString);
    }

}