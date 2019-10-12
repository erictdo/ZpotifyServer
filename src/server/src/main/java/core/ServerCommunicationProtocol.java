package main.java.core;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;

import java.io.*;
import java.net.*;

public class ServerCommunicationProtocol extends Thread {

    private static final int FRAGMENT_SIZE = 8192;
    private byte[] buffer = new byte[FRAGMENT_SIZE];

    private DatagramSocket serverSocket;
    private DatagramPacket inPacket;
    private DataInputStream inputStream;
    private DataOutputStream outputStream;
    private Dispatcher dispatcher;
    private int port;

    /**
     * Constructor for the ServerCommunicationProtocol
     *
     * @param num
     */
    public ServerCommunicationProtocol(int num) {
        this.port = num;
    }

    /**
     * Communication Initialization before listening for requests
     */
    private void connect() {
        try {
            //Connecting to port
            serverSocket = new DatagramSocket(this.port);
            System.out.println("Socket opened on port: " + this.port);

            //Register Objects & Methods on Dispatcher
            dispatcher = new Dispatcher();
            dispatcher.registerObject(new UserService(), "UserService");
            dispatcher.registerObject(new MusicService(), "MusicService");
            dispatcher.registerObject(new MP3Service(), "MP3Service");
            System.out.println("Initialized Dispatcher");

        } catch (IOException e) {
            System.out.println(e);
        }
    }

    /**
     * Listening for a request from client
     */
    private void listen() {
        System.out.println("Server listening.");
        try {
            while (true) {
                //Receiving Request Packet
                buffer = new byte[256];
                inPacket = new DatagramPacket(buffer, buffer.length);
                serverSocket.receive(inPacket);
                System.out.println("Request packet received: " + inPacket);

                //Handling Request on a thread
                CommunicationRunnable runnable = new CommunicationRunnable(buffer, serverSocket, inPacket, dispatcher);
                Thread t = new Thread(runnable);
                t.start();
            }
        } catch (IOException e) {
            System.out.println(e);
        }
        serverSocket.close();
        System.out.println("Server is no longer listening.");
    }

    /**
     * Override Thread's run method to run connect() and listen()
     */
    @Override
    public void run() {
        connect();
        listen();
    }

    public static class CommunicationRunnable implements Runnable {
        private byte[] buffer;
        private DatagramSocket serverSocket;
        private DatagramPacket inPacket;
        private Dispatcher dispatcher;
        private String inMessage;
        private InetAddress clientAddress;
        private int port;

        /**
         * Constructor of CommunicationRunnable
         *
         * @param buffer   The buffer being read
         * @param inPacket The DatagramPacket being received
         */
        public CommunicationRunnable(byte[] buffer, DatagramSocket serverSocket, DatagramPacket inPacket, Dispatcher dispatcher) {
            this.buffer = buffer;
            this.serverSocket = serverSocket;
            this.inPacket = inPacket;
            this.dispatcher = dispatcher;
            this.clientAddress = inPacket.getAddress();
            this.port = inPacket.getPort();
            this.inMessage = new String(inPacket.getData(), 0, inPacket.getLength());
        }

        public void run() {
            try {
                Gson gson = new GsonBuilder().setPrettyPrinting().create();

                // This is the Method Json Object. See Notes below.
                JsonObject inJsonMessage = gson.fromJson(inMessage, JsonObject.class);

                // Get the values of the "Method" Object
                String methodName = inJsonMessage.get("remoteMethod").getAsString();
                String serviceName = inJsonMessage.get("service").getAsString();
                String callSemanticsName = inJsonMessage.get("call-semantics").getAsString();

                // Call dispatcher to return an outMessage
                String outMessage = dispatcher.dispatch(methodName);
                byte[] outMessageBytes = outMessage.getBytes();

                // Send response to client
                DatagramPacket outPacket = new DatagramPacket(outMessageBytes, outMessageBytes.length, clientAddress, port);
                serverSocket.send(outPacket);
                JsonObject ret;

//      if (callSemantic.equals("at-most-one")) {
//        if (attendedReq.containsKey(jsonIn.get("requestID").getAsString())) {
//          send(attendedReq.get(jsonIn.get("requestID").getAsString()), clientAddress, clientPort);
//        } else {
//          ret = new Gson().fromJson((dispatcher.dispatch(messageIn)).get("ret").getAsString(),JsonObject.class);
//          attendedReq.put(jsonIn.get("requestID").toString(), ret);
//          send(ret, clientAddress, clientPort);
//        }
//      } else {
//        ret = new Gson().fromJson((dispatcher.dispatch(messageIn)).get("ret").getAsString(),JsonObject.class);
//        send(ret, clientAddress, clientPort);
//      }

                System.out.println(Thread.getAllStackTraces().keySet());
                System.out.println("Sending response to client: " + clientAddress + "\n");

            } catch (IOException e) {
                System.out.println(e);
                System.out.println("Error occurred while handling client's request. See stack trace above.");
            }
        }


    }

}

/* --- NOTES ---

"Method" Json Object (Check methods.json in client side)

Object: Method
Values:
  remoteMethod - The method (from one of the services) being called
  object - The name of the service (UserService, MusicService, or MP3 Service)
  call-semantics - The type of call-semantics (maybe, at-least-one, at-most-one)

 */