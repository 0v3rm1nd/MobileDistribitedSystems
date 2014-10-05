/**
 * Created by Mind on 10/4/2014.
 */

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

public class ProcessingServer {
    //define executor
    //NOTE JAVA 8 IS REQUIRED TO USE newWorkStealingPool Factory method
    private static final ExecutorService exec = Executors.newWorkStealingPool();
    //flag
    private static boolean listening = true;
    //will hold messages from source to sink NOTE THIS IS A CONCURRENT COLLECTION + NOTE String is immutable
    private static final CopyOnWriteArrayList<String> messages = new CopyOnWriteArrayList<String>();
    //will keep track of the sink count. atomic so it ensures visibility + atomicity
    private static final AtomicInteger sinkCount = new AtomicInteger();
    //wait/notify lock object
    private static final Object lock = new Object();

    public static void main(String[] args) throws IOException {
        System.out.println("The Processing server handles source connection on port 7007 and sink connections on port 7008");
        //source socket
        final ServerSocket source_Socket = new ServerSocket(7007);
        //sink socket
        final ServerSocket sink_Socket = new ServerSocket(7008);
        //listen for incoming source connections
        Thread sourceThread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (listening) {
                    try {
                        processSource(source_Socket.accept());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        //listen for incoming sink connections
        Thread sinkThread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (listening) {
                    try {
                        processSink(sink_Socket.accept());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        //start the 2 listening threads for source and sinks
        sourceThread.start();
        sinkThread.start();
    }

    /*responsible for processing all source messages*/
    private static void processSource(final Socket connectionSocket) {
        //create a new source task
        Runnable sourceTask = new Runnable() {
            @Override
            public void run() {
                //get source socket
                Socket sourceSocket = connectionSocket;
                while (listening) {
                    String sourceMessage = null;
                    //input stream from source
                    BufferedReader inFromSource = null;
                    try {
                        inFromSource = new BufferedReader(
                                new InputStreamReader(sourceSocket.getInputStream()));
                        //message received from source
                        sourceMessage = inFromSource.readLine() + "\n";
                        System.out.println(sourceSocket.getInetAddress().toString() + ":" + sourceSocket.getPort() + " sent this message: " + sourceMessage);
                        //only if we have sinks connected we add the messages from the sources
                        if (sinkCount.get() > 0) {
                            //add the source message to the array list of messages
                            messages.addIfAbsent(sourceMessage);
                            synchronized (lock) {
                                //we notify that there are messages in the array list
                                lock.notifyAll();
                            }
                        }
                    } catch (IOException e) {
                        break;
                    }
                }
            }
        };
        //run the source task via executor
        exec.execute(sourceTask);
    }

    private static void processSink(final Socket connectionSocket) {
        //increment the sink count
        sinkCount.addAndGet(1);
        //create a new sink task
        Runnable sinkTask = new Runnable() {
            @Override
            public void run() {
                //get sink socket
                Socket sinkSocket = connectionSocket;
                while (listening) {
                    //output stream to sink
                    DataOutputStream outToSink = null;
                    try {
                        outToSink = new DataOutputStream(
                                sinkSocket.getOutputStream());

                        if (!messages.isEmpty()) {
                            //send source messages to sink
                            for (int i = 0; i < messages.size(); i++) {
                                outToSink.writeBytes(messages.get(i));
                               
                            }
                             messages.clear();
                        } else {
                            synchronized (lock) {
                                //we wait for messages to be produced by the sources
                                lock.wait();
                            }
                        }
                    } catch (IOException e) {
                        break;
                    } catch (InterruptedException e) {
                        break;
                    }
                }
            }
        };
        //run the source task via executor
        exec.execute(sinkTask);
    }

    /*close silently a connection if needed*/
    public static void closeSocketSilently(Socket s) {
        if (s != null) {
            try {
                s.close();
            } catch (IOException e2) {
                e2.printStackTrace();
            }
        }
    }
}




