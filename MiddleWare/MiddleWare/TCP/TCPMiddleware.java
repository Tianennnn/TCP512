package MiddleWare.TCP;

import MiddleWare.Customers.CustomerManager;
import Server.Interface.*;
import Server.Common.TCPMessage;
import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;
import java.rmi.RemoteException;

public class TCPMiddleware implements IResourceManager {

    private static String s_middleWareName = "MiddleWare";
    private static int s_middleWarePort = 1046;  // Port for client connections
    private static int s_serverPort = 1045;     // Base port for resource managers

    // Server connection details
    private static String s_server_car = "localhost";
    private static String s_server_room = "localhost";
    private static String s_server_flight = "localhost";

    private static String s_carRM = "Cars";
    private static String s_roomRM = "Rooms";
    private static String s_flightRM = "Flights";

    // Resource Manager connections
    public ResourceManagerConnection carRM;
    public ResourceManagerConnection roomRM;
    public ResourceManagerConnection flightRM;

    private CustomerManager customerManager;
    private HashMap<Integer, Integer> m_Flights_available;
    private HashMap<String, Integer> m_Cars_available;
    private HashMap<String, Integer> m_Rooms_available;

    // TCP Server components
    private ServerSocket serverSocket;
    private ExecutorService threadPool;
    private volatile boolean running = false;

    // Inner class to handle persistent connections to resource managers
    private class ResourceManagerConnection {
        private String serverHost;
        private int serverPort;
        private String serverName;
        private Socket socket;
        private ObjectOutputStream out;
        private ObjectInputStream in;
        private boolean connected = false;

        public ResourceManagerConnection(String host, int port, String name) {
            this.serverHost = host;
            this.serverPort = port;
            this.serverName = name;
        }

        public boolean connect() {
            try {
                if (connected) {
                    System.out.println("Connected to " + serverName + " at " + serverHost + ":" + serverPort);
                    return true;
                }
                System.out.println("Connecting to " + serverName + " at " + serverHost + ":" + serverPort);
                socket = new Socket(serverHost, serverPort);
                out = new ObjectOutputStream(socket.getOutputStream());
                in = new ObjectInputStream(socket.getInputStream());
                connected = true;
                System.out.println("Connected to " + serverName + " resource manager");
                return true;
            } catch (IOException e) {
                connected = false;
                return false;
            }
        }

        public synchronized Object sendMessage(TCPMessage message) throws RemoteException {
            if (!connected) {
                throw new RemoteException("Not connected to " + serverName);
            }

            try {
                System.out.println("Sending to " + serverName + ": " + message);
                out.writeObject(message);
                out.flush();

                Object response = in.readObject();

                if (response instanceof String && ((String) response).startsWith("ERROR:")) {
                    throw new RemoteException(((String) response).substring(7));
                }

                return response;

            } catch (IOException | ClassNotFoundException e) {
                connected = false;
                throw new RemoteException("Communication error with " + serverName + ": " + e.getMessage());
            }
        }

        public void disconnect() {
            connected = false;
            try {
                if (in != null) in.close();
                if (out != null) out.close();
                if (socket != null) socket.close();
            } catch (IOException e) {
                System.err.println("Error closing connection to " + serverName + ": " + e.getMessage());
            }
        }
    }

    // Client Handler for incoming client connections
    private class ClientHandler implements Runnable {
        private Socket clientSocket;
        private ObjectInputStream in;
        private ObjectOutputStream out;
        private String clientId;

        public ClientHandler(Socket socket) {
            this.clientSocket = socket;
            this.clientId = socket.getRemoteSocketAddress().toString();
        }

        @Override
        public void run() {
            try {
                out = new ObjectOutputStream(clientSocket.getOutputStream());
                in = new ObjectInputStream(clientSocket.getInputStream());

                System.out.println("Client handler started for: " + clientId);

                while (!clientSocket.isClosed()) {
                    try {
                        TCPMessage message = (TCPMessage) in.readObject();
                        System.out.println("Received from client " + clientId + ": " + message);
                        Object result = processClientMessage(message);

                        out.writeObject(result);
                        out.flush();

                    } catch (EOFException e) {
                        // Client disconnected
                        System.out.println("Client " + clientId + " disconnected");
                        break;
                    } catch (Exception e) {
                        try {
                            //REturn the error message, doesn't make much sense when expected output is an integer
                            out.writeObject("ERROR: " + e.getMessage());
                            out.flush();
                        } catch (IOException ioEx) {
                            System.err.println("Error sending error response: " + ioEx.getMessage());
                            break;
                        }
                    }
                }
            } catch (IOException e) {
                System.err.println("Client handler error for " + clientId + ": " + e.getMessage());
            } finally {
                cleanup();
            }
        }

        private Object processClientMessage(TCPMessage message) throws Exception {
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
                    Vector<String> flightNumbers;
                    if (params[1] instanceof Vector<?>) {
                        flightNumbers = new Vector<>();
                        for (Object obj : (Vector<?>) params[1]) {
                            flightNumbers.add(String.valueOf(obj));
                        }
                    } else {
                        throw new IllegalArgumentException("Expected Vector<String> for flightNumbers");
                    }
                    return bundle((Integer) params[0], flightNumbers,
                            (String) params[2], (Boolean) params[3], (Boolean) params[4]);

                case "getName":
                    return getName();

                default:
                    throw new IllegalArgumentException("Unknown operation: " + operation);
            }
        }

        private void cleanup() {
            try {
                if (in != null) in.close();
                if (out != null) out.close();
                if (clientSocket != null) clientSocket.close();
                System.out.println("Client exited: " + clientId);
            } catch (IOException e) {
                System.err.println("Error during cleanup for client " + clientId + ": " + e.getMessage());
            }
        }
    }

    public static void main(String args[]) {
        if (args.length > 0) {
            s_server_flight = args[0];
            s_server_car = args[1];
            s_server_room = args[2];
            System.out.println("DEBUG: Starting middleware with servers - Flight: " + s_server_flight + ", Car: " + s_server_car + ", Room: " + s_server_room);
        }

        try {
            TCPMiddleware middleware = new TCPMiddleware();

            //Connect to RMs
            middleware.connectToResourceManagers();

            // Start TCP server for clients
            middleware.startTCPServer();

        } catch (Exception e) {
            System.err.println("Middleware exception: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }

    public TCPMiddleware() {
        //Initalize, or get singleton instance
        customerManager = CustomerManager.getInstance();
        m_Flights_available = new HashMap<>();
        m_Cars_available = new HashMap<>();
        m_Rooms_available = new HashMap<>();

        //All clientHandler goes into this pool
        threadPool = Executors.newFixedThreadPool(30); //30 shoudl be enough lol
        System.out.println("DEBUG: TCPMiddleware constructor - initialized data structures");
    }

    public void connectToResourceManagers() {
        // Create connections
        flightRM = new ResourceManagerConnection(s_server_flight, s_serverPort, s_flightRM);
        carRM = new ResourceManagerConnection(s_server_car, s_serverPort, s_carRM);
        roomRM = new ResourceManagerConnection(s_server_room, s_serverPort, s_roomRM);

        // Start connection threads
        Thread flightThread = new Thread(() -> connectWithRetry(flightRM));
        Thread carThread = new Thread(() -> connectWithRetry(carRM));
        Thread roomThread = new Thread(() -> connectWithRetry(roomRM));

        flightThread.start();
        carThread.start();
        roomThread.start();

        // Wait for all connections to be established
        try {
            flightThread.join();
            carThread.join();
            roomThread.join();
        } catch (InterruptedException e) {
            System.err.println("Interrupted while waiting for connections");
        }

        System.out.println("Middleware connected to all resource managers.");
    }

    private void connectWithRetry(ResourceManagerConnection rm) {
        boolean first = true;
        while (!rm.connect()) {
            if (first) {
                System.out.println("Waiting for " + rm.serverName + " server...");
                first = false;
            }
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                return;
            }
        }
    }

    public void startTCPServer() throws IOException {
        serverSocket = new ServerSocket(s_middleWarePort);
        running = true;

        System.out.println("'" + s_middleWareName + "' TCP middleware server ready on port " + s_middleWarePort);
        System.out.println("DEBUG: Middleware fully initialized and ready for client connections");

        // Add shutdown hook
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("Shutting down '" + s_middleWareName + "' middleware...");
            stopServer();
        }));

        while (running) {
            try {
                Socket clientSocket = serverSocket.accept();
                System.out.println("New client connected: " + clientSocket.getRemoteSocketAddress());

                ClientHandler handler = new ClientHandler(clientSocket);
                threadPool.submit(handler);

            } catch (IOException e) {
                if (running) {
                    System.err.println("Error accepting client connection: " + e.getMessage());
                }
            }
        }
    }

    public void stopServer() {
        running = false;
        try {
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
            }
            threadPool.shutdown();

            if (flightRM != null) flightRM.disconnect();
            if (carRM != null) carRM.disconnect();
            if (roomRM != null) roomRM.disconnect();

        } catch (IOException e) {
            System.err.println("Error closing server socket: " + e.getMessage());
        }
    }

    // Many method here are labeled synchronized since there is a change it would be called by multiple thread
    // "synchonized" ensures thread safty, common resources such as the maps and RMs, are thread safe


    public synchronized boolean addFlight(int flightNum, int flightSeats, int flightPrice) throws RemoteException {
        System.out.println("DEBUG: addFlight called - Flight: " + flightNum + ", Seats: " + flightSeats + ", Price: " + flightPrice);
        m_Flights_available.merge(flightNum, flightSeats, Integer::sum);
        TCPMessage message = new TCPMessage("addFlight", flightNum, flightSeats, flightPrice);
        boolean result = (Boolean) flightRM.sendMessage(message);
        System.out.println("DEBUG: addFlight result: " + result + ", Total available seats: " + m_Flights_available.get(flightNum));
        return result;
    }

    @Override
    public synchronized boolean addCars(String location, int numCars, int price) throws RemoteException {
        System.out.println("DEBUG: addCars called - Location: " + location + ", Cars: " + numCars + ", Price: " + price);
        m_Cars_available.merge(location, numCars, Integer::sum);
        TCPMessage message = new TCPMessage("addCars", location, numCars, price);
        boolean result = (Boolean) carRM.sendMessage(message);
        System.out.println("DEBUG: addCars result: " + result + ", Total available cars: " + m_Cars_available.get(location));
        return result;
    }

    @Override
    public synchronized boolean addRooms(String location, int numRooms, int price) throws RemoteException {
        System.out.println("DEBUG: addRooms called - Location: " + location + ", Rooms: " + numRooms + ", Price: " + price);
        m_Rooms_available.merge(location, numRooms, Integer::sum);
        TCPMessage message = new TCPMessage("addRooms", location, numRooms, price);
        boolean result = (Boolean) roomRM.sendMessage(message);
        System.out.println("DEBUG: addRooms result: " + result + ", Total available rooms: " + m_Rooms_available.get(location));
        return result;
    }

    @Override
    public synchronized int newCustomer() throws RemoteException {
        System.out.println("DEBUG: newCustomer called (auto-generate ID)");
        int customerId = customerManager.newCustomer();
        System.out.println("DEBUG: newCustomer created with ID: " + customerId);
        return customerId;
    }

    @Override
    public synchronized boolean newCustomer(int cid) throws RemoteException {
        System.out.println("DEBUG: newCustomer called with specific ID: " + cid);
        boolean result = customerManager.newCustomer(cid);
        System.out.println("DEBUG: newCustomer with ID " + cid + " result: " + result);
        return result;
    }

    @Override
    public synchronized boolean deleteFlight(int flightNum) throws RemoteException {
        System.out.println("DEBUG: deleteFlight called - Flight: " + flightNum);
        if (customerManager.isFlightReserved(flightNum)) {
            System.out.println("DEBUG: deleteFlight failed - flight " + flightNum + " has reservations");
            return false;
        }
        TCPMessage message = new TCPMessage("deleteFlight", flightNum);
        boolean result = (Boolean) flightRM.sendMessage(message);
        System.out.println("DEBUG: deleteFlight result: " + result);
        if (result) {
            m_Flights_available.remove(flightNum);
        }
        return result;
    }

    @Override
    public synchronized boolean deleteCars(String location) throws RemoteException {
        System.out.println("DEBUG: deleteCars called - Location: " + location);
        if (customerManager.isCarReserved(location)) {
            System.out.println("DEBUG: deleteCars failed - location " + location + " has reservations");
            return false;
        }
        TCPMessage message = new TCPMessage("deleteCars", location);
        boolean result = (Boolean) carRM.sendMessage(message);
        System.out.println("DEBUG: deleteCars result: " + result);
        if (result) {
            m_Cars_available.remove(location);
        }
        return result;
    }

    @Override
    public synchronized boolean deleteRooms(String location) throws RemoteException {
        System.out.println("DEBUG: deleteRooms called - Location: " + location);
        if (customerManager.isRoomReserved(location)) {
            System.out.println("DEBUG: deleteRooms failed - location " + location + " has reservations");
            return false;
        }
        TCPMessage message = new TCPMessage("deleteRooms", location);
        boolean result = (Boolean) roomRM.sendMessage(message);
        System.out.println("DEBUG: deleteRooms result: " + result);
        if (result) {
            m_Rooms_available.remove(location);
        }
        return result;
    }

    @Override
    public synchronized boolean deleteCustomer(int customerID) throws RemoteException {
        System.out.println("DEBUG: deleteCustomer called - Customer ID: " + customerID);
        Map<Integer, Integer> reservedFlights = customerManager.getCustomerFlights(customerID);
        Map<String, Integer> reservedCars = customerManager.getCustomerCars(customerID);
        Map<String, Integer> reservedRooms = customerManager.getCustomerRooms(customerID);

        for (Integer flight_num : reservedFlights.keySet()) {
            m_Flights_available.merge(flight_num, reservedFlights.get(flight_num), Integer::sum);
        }
        for (String location : reservedRooms.keySet()) {
            m_Rooms_available.merge(location, reservedRooms.get(location), Integer::sum);
        }
        for (String location : reservedCars.keySet()) {
            m_Cars_available.merge(location, reservedCars.get(location), Integer::sum);
        }

        boolean result = customerManager.deleteCustomer(customerID);
        System.out.println("DEBUG: deleteCustomer result: " + result + ", Released " + reservedFlights.size() + " flights, " + reservedCars.size() + " cars, " + reservedRooms.size() + " rooms");
        return result;
    }

    @Override
    public synchronized int queryFlight(int flightNumber) throws RemoteException {
        System.out.println("DEBUG: queryFlight called - Flight: " + flightNumber);
        int seat = 0;
        Integer seats = m_Flights_available.get(flightNumber);
        if (seats != null) {
            seat = seats.intValue();
        }
        System.out.println("DEBUG: queryFlight result: " + seat + " seats available");
        return seat;
    }

    @Override
    public synchronized int queryCars(String location) throws RemoteException {
        System.out.println("DEBUG: queryCars called - Location: " + location);
        int cars = 0;
        Integer seats = m_Cars_available.get(location);
        if (seats != null) {
            cars = seats.intValue();
        }
        System.out.println("DEBUG: queryCars result: " + cars + " cars available");
        return cars;
    }

    @Override
    public synchronized int queryRooms(String location) throws RemoteException {
        System.out.println("DEBUG: queryRooms called - Location: " + location);
        int rooms = 0;
        Integer seats = m_Rooms_available.get(location);
        if (seats != null) {
            rooms = seats.intValue();
        }
        System.out.println("DEBUG: queryRooms result: " + rooms + " rooms available");
        return rooms;
    }

    @Override
    public synchronized String queryCustomerInfo(int customerID) throws RemoteException {
        System.out.println("DEBUG: queryCustomerInfo called - Customer ID: " + customerID);
        Map<Integer, Integer> reservedFlights = customerManager.getCustomerFlights(customerID);
        Map<String, Integer> reservedCars = customerManager.getCustomerCars(customerID);
        Map<String, Integer> reservedRooms = customerManager.getCustomerRooms(customerID);

        String s = "Bill for customer " + customerID + "\n";

        if (reservedFlights.isEmpty() && reservedCars.isEmpty() && reservedRooms.isEmpty()) {
            s += "No reservations found for this customer.\n";
            System.out.println("DEBUG: queryCustomerInfo - No reservations found for customer " + customerID);
            return s;
        }

        if (!reservedFlights.isEmpty()) {
            for (Map.Entry<Integer, Integer> entry : reservedFlights.entrySet()) {
                Integer flightNum = entry.getKey();
                Integer quantity = entry.getValue();
                Integer price = queryFlightPrice(flightNum);
                s += quantity + " flight-" + flightNum + " " + price + "\n";
            }
        }

        if (!reservedCars.isEmpty()) {
            for (Map.Entry<String, Integer> entry : reservedCars.entrySet()) {
                String carLocation = entry.getKey();
                Integer quantity = entry.getValue();
                Integer price = queryCarsPrice(carLocation);
                s += quantity + " car-" + carLocation + " " + price + "\n";
            }
        }

        if (!reservedRooms.isEmpty()) {
            for (Map.Entry<String, Integer> entry : reservedRooms.entrySet()) {
                String roomLocation = entry.getKey();
                Integer quantity = entry.getValue();
                Integer price = queryRoomsPrice(roomLocation);
                s += quantity + " room-" + roomLocation + " " + price + "\n";
            }
        }

        System.out.println("DEBUG: queryCustomerInfo generated bill with " + reservedFlights.size() + " flights, " + reservedCars.size() + " cars, " + reservedRooms.size() + " rooms");
        return s;
    }

    @Override
    public int queryFlightPrice(int flightNumber) throws RemoteException {
        System.out.println("DEBUG: queryFlightPrice called - Flight: " + flightNumber);
        TCPMessage message = new TCPMessage("queryFlightPrice", flightNumber);
        int price = (Integer) flightRM.sendMessage(message);
        System.out.println("DEBUG: queryFlightPrice result: $" + price);
        return price;
    }

    @Override
    public int queryCarsPrice(String location) throws RemoteException {
        System.out.println("DEBUG: queryCarsPrice called - Location: " + location);
        TCPMessage message = new TCPMessage("queryCarsPrice", location);
        int price = (Integer) carRM.sendMessage(message);
        System.out.println("DEBUG: queryCarsPrice result: $" + price);
        return price;
    }

    @Override
    public int queryRoomsPrice(String location) throws RemoteException {
        System.out.println("DEBUG: queryRoomsPrice called - Location: " + location);
        TCPMessage message = new TCPMessage("queryRoomsPrice", location);
        int price = (Integer) roomRM.sendMessage(message);
        System.out.println("DEBUG: queryRoomsPrice result: $" + price);
        return price;
    }

    @Override
    public synchronized boolean reserveFlight(int customerID, int flightNumber) throws RemoteException {
        System.out.println("DEBUG: reserveFlight called - Customer: " + customerID + ", Flight: " + flightNumber);
        Integer seat = m_Flights_available.get(flightNumber);

        if (seat != null && seat > 0) {
            boolean result = customerManager.reserveFlight(customerID, flightNumber);
            if (result) {
                m_Flights_available.merge(flightNumber, -1, Integer::sum);
                System.out.println(
                        "DEBUG: reserveFlight successful, remaining seats: " + m_Flights_available.get(flightNumber));
            } else {
                System.out.println("DEBUG: reserveFlight Falied!");
            }
            return result;
        } else {
            System.out.println(
                    "DEBUG: reserveFlight failed - insufficient seats available: " + (seat != null ? seat : "null"));
            return false;
        }
    }

    @Override
    public synchronized boolean reserveCar(int customerID, String location) throws RemoteException {
        System.out.println("DEBUG: reserveCar called - Customer: " + customerID + ", Location: " + location);
        Integer seat = m_Cars_available.get(location);

        if (seat != null && seat > 0) {
            boolean result = customerManager.reserveCar(customerID, location);
            if (result) {
                m_Cars_available.merge(location, -1, Integer::sum);
                System.out.println("DEBUG: reserveCar successful, remaining cars: " + m_Cars_available.get(location));
            } else {
                System.out.println("DEBUG: reserveCar Falied!");
            }
            return result;
        } else {
            System.out.println(
                    "DEBUG: reserveCar failed - insufficient cars available: " + (seat != null ? seat : "null"));
            return false;
        }
    }

    @Override
    public synchronized boolean reserveRoom(int customerID, String location) throws RemoteException {
        System.out.println("DEBUG: reserveRoom called - Customer: " + customerID + ", Location: " + location);
        Integer seat = m_Rooms_available.get(location);

        if (seat != null && seat > 0) {
            boolean result = customerManager.reserveRoom(customerID, location);
            if (result) {
                m_Rooms_available.merge(location, -1, Integer::sum);
                System.out
                        .println("DEBUG: reserveRoom successful, remaining rooms: " + m_Rooms_available.get(location));
            } else {
                System.out.println("DEBUG: reserveRoom Falied!");
            }
            return result;
        } else {
            System.out.println(
                    "DEBUG: reserveRoom failed - insufficient rooms available: " + (seat != null ? seat : "null"));
            return false;
        }
    }

    @Override
    /**
     * Reserve a bundle for the trip.
     *
     * @return Success
     */
    public boolean bundle(int customerID, Vector<String> flightNumbers, String location, boolean car, boolean room)
            throws RemoteException {
        System.out.println("DEBUG: bundle called - Customer: " + customerID + ", Flights: " + flightNumbers
                + ", Location: " + location + ", Car: " + car + ", Room: " + room);

        // Check is the customer is valid
        if (customerManager.getCustomer(customerID) == null) {
            System.out.println("DEBUG: bundle failed - customer does not exist");
            return false;
        }

        // Check if all requested items are available
        HashMap<Integer, Integer> to_be_reserved_flights = new HashMap<>();
        for (String flightNumber : flightNumbers) {
            int flightNumberInt = Integer.parseInt(flightNumber);
            to_be_reserved_flights.merge(flightNumberInt, 1, Integer::sum);
        }
        for (Integer flight : to_be_reserved_flights.keySet()) {
            if (!m_Flights_available.containsKey(flight)
                    || m_Flights_available.get(flight) < to_be_reserved_flights.get(flight)) {
                System.out.println("DEBUG: bundle failed - flight " + Integer.toString(flight) + " not available");
                return false;
            }
        }
        if (car) {
            if (!m_Cars_available.containsKey(location) || m_Cars_available.get(location) <= 0) {
                System.out.println("DEBUG: bundle failed - car at " + location + " not available");
                return false;
            }
        }
        if (room) {
            if (!m_Rooms_available.containsKey(location) || m_Rooms_available.get(location) <= 0) {
                System.out.println("DEBUG: bundle failed - room at " + location + " not available");
                return false;
            }
        }

        for (String flightNumber : flightNumbers) {
            int flightNumberInt = Integer.parseInt(flightNumber);
            customerManager.reserveFlight(customerID, flightNumberInt);
            m_Flights_available.merge(flightNumberInt, -1, Integer::sum);
        }
        if (car) {
            customerManager.reserveCar(customerID, location);
            m_Cars_available.merge(location, -1, Integer::sum);
        }
        if (room) {
            customerManager.reserveRoom(customerID, location);
            m_Rooms_available.merge(location, -1, Integer::sum);
        }

        System.out.println("DEBUG: bundle successful for customer " + customerID);
        return true;
    }

    @Override
    public String getName() throws RemoteException {
        return s_middleWareName;
    }

    public String removeBillHeader(String bill) {
        if (bill == null || bill.isEmpty()) {
            return "";
        }
        int idx = bill.indexOf("\n");
        if (idx == -1) {
            return "";
        }
        return bill.substring(idx + 1).trim();
    }
}