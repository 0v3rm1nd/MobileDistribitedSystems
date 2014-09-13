import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

/**
 * Created by Mind on 9/12/2014.
 */
public class RFC862_UdpClient {
    public static void main(String[] args) throws Exception {
        if (args.length != 2) {
            System.err.println(
                    "Usage: java RFS862_UdpClient <destination host> <destination port>");
            System.exit(1);
        }
        //get the dest host + port from console
        String dest_host = args[0];
        int dest_port = Integer.parseInt(args[1]);

        //input stream from the console
        BufferedReader inFromUserConsole = new BufferedReader(new InputStreamReader(
                System.in));
        //new udp socket
        DatagramSocket clientSocket = new DatagramSocket();
        InetAddress IPAddress = InetAddress.getByName(dest_host);
        //sizes of sent and received data
        byte[] sendData = new byte[1024];
        byte[] receiveData = new byte[1024];
        //the message from the client
        System.out.println("Please input a message:");
        String sentence = inFromUserConsole.readLine();

        sendData = sentence.getBytes();
        //build the datagram package
        DatagramPacket sendPacket = new DatagramPacket(sendData,
                sendData.length, IPAddress, dest_port);
        //send it to the server
        clientSocket.send(sendPacket);
        DatagramPacket receivePacket = new DatagramPacket(receiveData,
                receiveData.length);
        //get data from the server
        clientSocket.receive(receivePacket);
        //cast the received message as string
        String modifiedSentence = new String(receivePacket.getData());
        System.out.println(modifiedSentence);
        //close the client socket
        clientSocket.close();

    }
}
