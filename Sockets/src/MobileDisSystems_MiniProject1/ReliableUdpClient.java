import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

/**
 * Created by Mind on 9/11/2014.
 */
/*reliable udp client*/
public class ReliableUdpClient {
    public static void main(String[] args) throws Exception {
        if (args.length != 3) {
            System.err.println(
                    "Usage: java ReliableUdpClient <destination host> <destination port> <string message>");
            System.exit(1);
        }
        //server name
        String serverName = args[0];
        InetAddress serverAddress = InetAddress.getByName(serverName);
        //server port
        int port = Integer.parseInt(args[1]);
        String message = args[2];

        //create udp socket
        DatagramSocket clientSocket = new DatagramSocket();
        byte[] sendData = new byte[1024];

        //convert my message to bytes
        sendData = message.getBytes();
        //create datagram packet from the string, ip address and port
        DatagramPacket sendPacket =
                new DatagramPacket(sendData, sendData.length, serverAddress, port);
        //send the packet through the datagram socket
        clientSocket.send(sendPacket);

        //receive data from the server
        byte[] receiveData = new byte[1024];
        DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
        clientSocket.receive(receivePacket);
        //get the message size
        int requestedMessageSize = Integer.parseInt(new String(receivePacket.getData()).trim());
        System.out.println("Message Size: " + requestedMessageSize / 1024.0 + "Kb");
        BufferedOutputStream fileBytes = new BufferedOutputStream(new FileOutputStream(message));

        //offset
        int offset = 0;
        //packet size
        int packetSize = 1024;

        while (offset <= requestedMessageSize) {
            //check for end of message
            if (offset + 1024 > requestedMessageSize) {
                //change size of the last packet
                packetSize = requestedMessageSize - offset;
            }

            byte[] packetBytes = new byte[packetSize];
            DatagramPacket messagePacket = new DatagramPacket(packetBytes, packetBytes.length);
            //represent the acknowledgement
            byte[] acknowledgeData = ("y").getBytes();
            DatagramPacket ackPack = new DatagramPacket(acknowledgeData, acknowledgeData.length, serverAddress, port);
            clientSocket.receive(messagePacket);

            fileBytes.write(messagePacket.getData(), 0, packetSize);
            //cumulative byte count
            offset += 1024;
            //send acknowledgments
            clientSocket.send(ackPack);

            System.out.println("Received packet: " + offset / 1024);

        }
        //flush and close the socket
        fileBytes.flush();
        fileBytes.close();
        clientSocket.close();
    }

}
