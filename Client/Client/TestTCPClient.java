package Client;

import Server.Interface.*;

import java.io.*;
import java.net.*;
import java.rmi.RemoteException;
import java.util.Vector;
import Server.Common.TCPMessage;

import java.util.concurrent.CountDownLatch;

public class TestTCPClient extends TestClient
{
    private static String s_serverHost = "localhost";
    private static int s_serverPort = 1046;
    private static String s_serverName = "MiddleWare";

    // TCP connection components
    private Socket socket;
    private ObjectOutputStream out;
    private ObjectInputStream in;
    private boolean connected = false;

    public static void main(String args[])
    {
        if (args.length > 0)
        {
            s_serverHost = args[0];
        }
        if (args.length > 1)
        {
            s_serverName = args[1];
        }
        if (args.length > 2)
        {
            System.err.println((char)27 + "[31;1mClient exception: " + (char)27 + "[0mUsage: java Client.TCPClient [server_hostname [server_name]]");
            System.exit(1);
        }

        try {
            CountDownLatch latch = new CountDownLatch(1);

            // Thread 1
            Thread thread1 = new Thread(() -> {
                try {
                    TestTCPClient client1 = new TestTCPClient();
                    client1.connectServer();

                    latch.await();

                    client1.start();
                    System.out.println("Client 1 started at: " + System.nanoTime());
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });

            Thread thread2 = new Thread(() -> {
                try {
                    TestTCPClient client2 = new TestTCPClient();
                    client2.connectServer();

                    latch.await(); // Wait for the signal

                    client2.start();
                    System.out.println("Client 2 started at: " + System.nanoTime());
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });

            thread1.start();
            thread2.start();

            // Give threads time to connect and reach the await() call
            Thread.sleep(200);

            // Release both threads simultaneously
            System.out.println("Releasing both clients...");
            latch.countDown();

            // Optional: wait for threads to complete
            thread1.join();
            thread2.join();

        }
        catch (Exception e) {
            System.err.println((char)27 + "[31;1mClient exception: " + (char)27 + "[0mUncaught exception");
            e.printStackTrace();
            System.exit(1);
        }
    }

    public TestTCPClient()
    {
        super();
        // Create TCP-based resource manager proxy
        m_resourceManager = new TCPResourceManagerProxy();
    }

    public void connectServer()
    {
        connectServer(s_serverHost, s_serverPort, s_serverName);
    }

    public void connectServer(String server, int port, String name)
    {
        try {
            boolean first = true;
            while (true) {
                try {
                    System.out.println("Connecting to " + server + ":" + port);
                    socket = new Socket(server, port);
                    out = new ObjectOutputStream(socket.getOutputStream());
                    in = new ObjectInputStream(socket.getInputStream());
                    connected = true;
                    System.out.println("Connected to '" + name + "' server [" + server + ":" + port + "]");
                    break;
                }
                catch (IOException e) {
                    if (first) {
                        System.out.println("Waiting for '" + name + "' server [" + server + ":" + port + "]");
                        first = false;
                    }
                }
                Thread.sleep(500);
            }
        }
        catch (Exception e) {
            System.err.println((char)27 + "[31;1mServer exception: " + (char)27 + "[0mUncaught exception");
            e.printStackTrace();
            System.exit(1);
        }
    }

    // TCP-based implementation of IResourceManager
    private class TCPResourceManagerProxy implements IResourceManager {

        private Object sendMessage(String operation, Object... parameters) throws RemoteException {
            if (!connected) {
                throw new RemoteException("Cannot send operation '" + operation + "': Not connected to server. Call connectServer() first.");
            }

            try {
                TCPMessage message = new TCPMessage(operation, parameters);

//				System.out.println("DEBUG: Sending operation '" + operation + "' with " +
//						(parameters != null ? parameters.length : 0) + " parameters");

                out.writeObject(message);
                out.flush();

                //System.out.println("DEBUG: Message sent, waiting for response...");

                Object response = in.readObject();

//				System.out.println("DEBUG: Received response of type: " + response.getClass().getName());

                if (response instanceof String && ((String) response).startsWith("ERROR:")) {
                    String errorMsg = ((String) response).substring(7);
                    throw new RemoteException("Server error for operation '" + operation + "': " + errorMsg);
                }

                return response;

            } catch (Exception e) {
                connected = false;
                throw new RemoteException("Something is wrong", e);
            }
        }

        @Override
        public boolean addFlight(int flightNum, int flightSeats, int flightPrice) throws RemoteException {
            return (Boolean) sendMessage("addFlight", flightNum, flightSeats, flightPrice);
        }

        @Override
        public boolean addCars(String location, int numCars, int price) throws RemoteException {
            return (Boolean) sendMessage("addCars", location, numCars, price);
        }

        @Override
        public boolean addRooms(String location, int numRooms, int price) throws RemoteException {
            return (Boolean) sendMessage("addRooms", location, numRooms, price);
        }

        @Override
        public boolean deleteFlight(int flightNum) throws RemoteException {
            return (Boolean) sendMessage("deleteFlight", flightNum);
        }

        @Override
        public boolean deleteCars(String location) throws RemoteException {
            return (Boolean) sendMessage("deleteCars", location);
        }

        @Override
        public boolean deleteRooms(String location) throws RemoteException {
            return (Boolean) sendMessage("deleteRooms", location);
        }

        @Override
        public int queryFlight(int flightNum) throws RemoteException {
            return (Integer) sendMessage("queryFlight", flightNum);
        }

        @Override
        public int queryCars(String location) throws RemoteException {
            return (Integer) sendMessage("queryCars", location);
        }

        @Override
        public int queryRooms(String location) throws RemoteException {
            return (Integer) sendMessage("queryRooms", location);
        }

        @Override
        public int queryFlightPrice(int flightNum) throws RemoteException {
            return (Integer) sendMessage("queryFlightPrice", flightNum);
        }

        @Override
        public int queryCarsPrice(String location) throws RemoteException {
            return (Integer) sendMessage("queryCarsPrice", location);
        }

        @Override
        public int queryRoomsPrice(String location) throws RemoteException {
            return (Integer) sendMessage("queryRoomsPrice", location);
        }

        @Override
        public String queryCustomerInfo(int customerID) throws RemoteException {
            return (String) sendMessage("queryCustomerInfo", customerID);
        }

        @Override
        public int newCustomer() throws RemoteException {
            return (Integer) sendMessage("newCustomer");
        }

        @Override
        public boolean newCustomer(int customerID) throws RemoteException {
            return (Boolean) sendMessage("newCustomerWithID", customerID);
        }

        @Override
        public boolean deleteCustomer(int customerID) throws RemoteException {
            return (Boolean) sendMessage("deleteCustomer", customerID);
        }

        @Override
        public boolean reserveFlight(int customerID, int flightNum) throws RemoteException {
            return (Boolean) sendMessage("reserveFlight", customerID, flightNum);
        }

        @Override
        public boolean reserveCar(int customerID, String location) throws RemoteException {
            return (Boolean) sendMessage("reserveCar", customerID, location);
        }

        @Override
        public boolean reserveRoom(int customerID, String location) throws RemoteException {
            return (Boolean) sendMessage("reserveRoom", customerID, location);
        }

        @Override
        public boolean bundle(int customerId, Vector<String> flightNumbers, String location, boolean car, boolean room) throws RemoteException {
            return (Boolean) sendMessage("bundle", customerId, flightNumbers, location, car, room);
        }

        @Override
        public String getName() throws RemoteException {
            return (String) sendMessage("getName");
        }
    }
}