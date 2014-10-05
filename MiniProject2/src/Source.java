import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.Socket;

/**
 * Created by Mind on 9/12/2014.
 */
public class Source {

    public static void main(String[] args) throws Exception {

        if (args.length != 1) {
            System.err.println(
                    "Usage: java Source <processing server address>");
            System.exit(1);
        }
//        get the destination host + port from console
        String dest_host = args[0];

        int dest_port = 7007;
        //increment source count
        String message;
        //input stream from the console
        BufferedReader inFromSourceConsole = new BufferedReader(new InputStreamReader(
                System.in));
        //create the source socket
        Socket sourceSocket = new Socket(dest_host, dest_port);
        //data output stream to server
        DataOutputStream outToProcessingServer = new DataOutputStream(
                sourceSocket.getOutputStream());

        while (true) {
            System.out.println("Source message:");
            message = inFromSourceConsole.readLine();
            //if we force close the connection the input string will be null so we don't want to send that
            if (message != null) {
                outToProcessingServer.writeBytes(message + '\n');
            } else {
                break;
            }
        }
    }
}
