import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.Socket;

/**
 * Created by Mind on 10/4/2014.
 */
public class Sink {

    public static void main(String[] args) throws Exception {

        if (args.length != 1) {
            System.err.println(
                    "Usage: java Sink <processing server address>");
            System.exit(1);
        }
        // get the dest host + port from console
        String dest_host = args[0];

        int dest_port = 7008;

        String sourceSentence;
        //create a sink socket
        Socket sinkSocket = new Socket(dest_host, dest_port);
        //data input stream to server
        BufferedReader inFromProcessingServer = new BufferedReader(new InputStreamReader(
                sinkSocket.getInputStream()));
        while (true) {
            sourceSentence = inFromProcessingServer.readLine();
            System.out.println(sourceSentence);

        }
    }
}
