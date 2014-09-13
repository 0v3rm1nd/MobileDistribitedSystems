import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.Socket;

/**
 * Created by Mind on 9/12/2014.
 */
public class RFC862_TcpClient {
    public static void main(String[] args) throws Exception {

        if (args.length != 2) {
            System.err.println(
                    "Usage: java RFS826_TcpClient <destination host> <destination port>");
            System.exit(1);
        }
//        get the dest host + port from console
        String dest_host = args[0];
        int dest_port = Integer.parseInt(args[1]);

        String sentence;
        String modifiedSentence;
        //input stream from the console
        BufferedReader inFromUserConsole = new BufferedReader(new InputStreamReader(
                System.in));

        Socket clientSocket = new Socket(dest_host, dest_port);

        DataOutputStream outToServer = new DataOutputStream(
                clientSocket.getOutputStream());

        BufferedReader inFromServer = new BufferedReader(new InputStreamReader(
                clientSocket.getInputStream()));

        System.out.println("Please input a message:");

        sentence = inFromUserConsole.readLine();

        outToServer.writeBytes(sentence + '\n');

        modifiedSentence = inFromServer.readLine();
        System.out.println(modifiedSentence);
        //close client socket
        clientSocket.close();

    }
}
