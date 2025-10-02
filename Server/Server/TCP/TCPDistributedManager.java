package Server.TCP;

import Server.DistributedCommon.DistributedResourceManager;
import Server.Common.TCPMessage;
import java.io.*;
import java.net.*;
import java.util.Vector;

public class TCPDistributedManager extends DistributedResourceManager {

    public static String s_serverName = "Server";
    public int s_serverPort = 1145;
    private ServerSocket serverSocket;
    private volatile boolean isRunning = false;

    public static void main(String args[]) {
        int serverPort = 1145;
        if (args.length > 0) {
            TCPDistributedManager.s_serverName = args[0];
        }

        try {
            TCPDistributedManager server = new TCPDistributedManager(TCPDistributedManager.s_serverName);

            if (args.length > 1) {
                try {
                    serverPort = Integer.parseInt(args[1]);
                } catch (NumberFormatException e) {
                    System.err.println("Invalid port number, using default: " + serverPort);
                }
            }
            server.s_serverPort = serverPort;
            server.startServer();
        } catch (Exception e) {
            System.err.println("Server exception: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }

    public TCPDistributedManager(String name) {
        super(name);
    }

    public void startServer() throws IOException {
        serverSocket = new ServerSocket(this.s_serverPort);
        isRunning = true;

        System.out.println("'" + s_serverName + "' TCP server started on port " + this.s_serverPort);
        System.out.println("Waiting for middleware connection...");

        // Add shutdown hook
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("Shutting down '" + TCPDistributedManager.s_serverName + "' server...");
            stopServer();
        }));

        // Accept one connection from middleware
        try {
            Socket middlewareSocket = serverSocket.accept();
            System.out.println("Middleware connected from: " + middlewareSocket.getRemoteSocketAddress());
            handleMiddlewareConnection(middlewareSocket);
        } catch (IOException e) {
            if (isRunning) {
                System.err.println("Error accepting middleware connection: " + e.getMessage());
            }
        }
    }

    public void stopServer() {
        isRunning = false;
        try {
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
            }
        } catch (IOException e) {
            System.err.println("Error closing server socket: " + e.getMessage());
        }
    }

    private void handleMiddlewareConnection(Socket socket) {
        ObjectInputStream in = null;
        ObjectOutputStream out = null;

        try {
            out = new ObjectOutputStream(socket.getOutputStream());
            in = new ObjectInputStream(socket.getInputStream());

            System.out.println("Ready to process messages from middleware");

            while (isRunning && !socket.isClosed()) {
                try {
                    TCPMessage message = (TCPMessage) in.readObject();
                    System.out.println("Received: " + message);
                    Object result = processMessage(message);
                    out.writeObject(result);
                    out.flush();
                } catch (EOFException e) {
                    // Middleware disconnected
                    System.out.println("Middleware disconnected");
                    break;
                }catch (Exception e) {
                    System.err.println("Error processing message: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        } catch (IOException e) {
            System.err.println("Connection error: " + e.getMessage());
        } finally {
            //exit program after connection closes
            try {
                if (in != null) in.close();
                if (out != null) out.close();
                if (socket != null) socket.close();
            } catch (IOException e) {
                System.err.println("Error closing connection: " + e.getMessage());
            }
            System.out.println("Connection closed");
        }
    }

    private Object processMessage(TCPMessage message) throws Exception {
        String operation = message.getOperation();
        Object[] params = message.getParameters();

        switch (operation) {
            case "addFlight":
                return addFlight((Integer) params[0], (Integer) params[1], (Integer) params[2]);

            case "addCars":
                return addCars((String) params[0], (Integer) params[1], (Integer) params[2]);

            case "addRooms":
                return addRooms((String) params[0], (Integer) params[1], (Integer) params[2]);

            case "deleteFlight":
                return deleteFlight((Integer) params[0]);

            case "deleteCars":
                return deleteCars((String) params[0]);

            case "deleteRooms":
                return deleteRooms((String) params[0]);

            case "queryFlight":
                return queryFlight((Integer) params[0]);

            case "queryCars":
                return queryCars((String) params[0]);

            case "queryRooms":
                return queryRooms((String) params[0]);

            case "queryFlightPrice":
                return queryFlightPrice((Integer) params[0]);

            case "queryCarsPrice":
                return queryCarsPrice((String) params[0]);

            case "queryRoomsPrice":
                return queryRoomsPrice((String) params[0]);

            case "queryCustomerInfo":
                return queryCustomerInfo((Integer) params[0]);

            case "newCustomer":
                return newCustomer();

            case "newCustomerWithID":
                return newCustomer((Integer) params[0]);

            case "deleteCustomer":
                return deleteCustomer((Integer) params[0]);

            case "reserveFlight":
                return reserveFlight((Integer) params[0], (Integer) params[1]);

            case "reserveCar":
                return reserveCar((Integer) params[0], (String) params[1]);

            case "reserveRoom":
                return reserveRoom((Integer) params[0], (String) params[1]);

            case "bundle":
                Vector<String> locations;
                if (params[1] instanceof Vector<?>) {
                    locations = new Vector<>();
                    for (Object obj : (Vector<?>) params[1]) {
                        locations.add((String) obj);
                    }
                } else {
                    throw new IllegalArgumentException("Expected Vector<String> for bundle locations");
                }
                return bundle((Integer) params[0], locations,
                        (String) params[2], (Boolean) params[3], (Boolean) params[4]);

            case "getName":
                return getName();

            default:
                throw new IllegalArgumentException("Unknown operation: " + operation);
        }
    }
}