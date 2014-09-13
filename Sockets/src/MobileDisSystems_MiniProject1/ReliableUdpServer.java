import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;


/**
 * Created by Mind on 9/11/2014.
 */
/*supports multiple reliable UDP connections by acknowledging packets*/
public class ReliableUdpServer {
    public static void main(String[] args) throws Exception {
        if (args.length != 1) {
            System.err.println(
                    "Usage: java ReliableUdpServer <listening port>");
            System.exit(1);
        }
        int serverPort = Integer.parseInt(args[0]);
        //create new datagram socket with a specific port number binded to it
        DatagramSocket dataSocket = new DatagramSocket(serverPort);

        new ReliableUdpThread(dataSocket).start();
    }
}

/*will make it possible for multiple clients to connect at the same time*/
class ReliableUdpThread extends Thread {

    private DatagramSocket datagramSocket = null;

    public ReliableUdpThread(DatagramSocket datagramSocket) {
        super("ReliableUDPThread");
        this.datagramSocket = datagramSocket;
    }

    public void run() {
        while (true) {
            try {
                //get the data from the client
                byte[] receivedData = new byte[1024];
                DatagramPacket receivePacket = new DatagramPacket(receivedData, receivedData.length);
                datagramSocket.receive(receivePacket);
                String clientRequest = new String(receivePacket.getData());
                System.out.println(clientRequest);

                //send the client the size of the message
                byte[] sendData = new byte[1024];
                sendData = ("" + clientRequest.length()).getBytes();
                //get the ip and port
                InetAddress clientIpAddress = receivePacket.getAddress();
                int port = receivePacket.getPort();
                DatagramPacket sendPacket =
                        new DatagramPacket(sendData, sendData.length, clientIpAddress, port);
                datagramSocket.send(sendPacket);

                //start sending the message
                int offset = 0;
                int length = (int) clientRequest.length();
                sendData = new byte[1024];
                datagramSocket.setSoTimeout(500);
                while (offset <= length) {
                    offset += 1024;

                    byte[] ack = new byte[0];
                    while (ack.length == 0) {
                    /*
                     * this is going to try and send the same data packet over
					 * and over again until it receives an "acknowledgment" from
					 * the client.  The ack, in this case, is simply a non-empty
					 * byte array; If nothing is received from the
					 * client within the specified timeout (ms), we catch an
					 * exception and try again.
					 */
                        try {
                            sendPacket = new DatagramPacket(sendData, sendData.length, clientIpAddress, port);
                            datagramSocket.send(sendPacket);
                            System.out.println("packet#: " + offset / 1024);
                            datagramSocket.receive(receivePacket);
                            ack = receivePacket.getData();
                        } catch (SocketTimeoutException ste) {
                            System.out.println("Ack not received, resending...");
                        }
                    }
                }
                datagramSocket.setSoTimeout(0);
                System.out.println("Message transfer complete: " + clientRequest);
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }

    }
}
