package Client;

import java.util.*;
import java.io.*;
import java.net.Socket;

public class TCPClient
{
	private static String s_serverHost = "localhost";
	private static int s_serverPort = 1045;

    private Socket socket;
    private PrintWriter outToServer;
    private BufferedReader inFromServer;

	public static void main(String args[])
	{	
		if (args.length > 0)
		{
			s_serverHost = args[0];
		}
		if (args.length > 1)
		{
			System.err.println((char)27 + "[31;1mClient exception: " + (char)27 + "[0mUsage: java client.RMIClient [server_hostname [server_rmiobject]]");
			System.exit(1);
		}

        TCPClient client = new TCPClient();

        client.start();

        client.disconnectServer();
	}
    
	public TCPClient()
	{
		connectServer();
	}

	public void connectServer()
	{
		try {
            // establish a socket with the server
            socket = new Socket(s_serverHost, s_serverPort);

            // open an output stream to the server
            outToServer = new PrintWriter(socket.getOutputStream(), true);
            // open an input stream from the server
            inFromServer = new BufferedReader(new InputStreamReader(socket.getInputStream()));

        } catch (java.net.UnknownHostException e) {
            System.out.println("Unknown host: " + s_serverHost);
            e.printStackTrace();
            System.exit(1);
        } catch (IOException e) {
            System.out.println("IO error while connecting to server: " + s_serverHost + ":" + s_serverPort);
            e.printStackTrace();
            System.exit(1);
        }
	}

    public void disconnectServer()
	{
        try{
            socket.close();
        }
        catch(IOException e){
            System.out.println("Unable to close socket. ");
            e.printStackTrace();
            System.exit(1);
        }
    }

    public void start()
	{
		// Prepare for reading commands
		System.out.println();
		System.out.println("Location \"help\" for list of supported commands");

		BufferedReader stdin = new BufferedReader(new InputStreamReader(System.in));

		while (true)
		{
			// Read the next command
			String command = "";
			Vector<String> arguments = new Vector<String>();
			try {
				System.out.print((char)27 + "[32;1m\n>] " + (char)27 + "[0m");
				command = stdin.readLine().trim();
			}
			catch (IOException io) {
				System.err.println((char)27 + "[31;1mClient exception: " + (char)27 + "[0m" + io.getLocalizedMessage());
				io.printStackTrace();
				System.exit(1);
			}

			try {
				arguments = parse(command);
				Command cmd = Command.fromString((String)arguments.elementAt(0));
				
                execute(cmd, arguments, command);
			}
			catch (Exception e) {
				System.err.println((char)27 + "[31;1mCommand exception: " + (char)27 + "[0mUncaught exception");
				e.printStackTrace();
			}
		}

	}


    public void execute(Command cmd, Vector<String> arguments, String commandStr) {
        switch (cmd) {
            case Help: {
                if (arguments.size() == 1) {
                    System.out.println(Command.description());
                } else if (arguments.size() == 2) {
                    Command l_cmd = Command.fromString((String) arguments.elementAt(1));
                    System.out.println(l_cmd.toString());
                } else {
                    System.err.println((char) 27 + "[31;1mCommand exception: " + (char) 27
                            + "[0mImproper use of help command. Location \"help\" or \"help,<CommandName>\"");
                }
                break;
            }
            case AddFlight: {
                checkArgumentsCount(4, arguments.size());

                System.out.println("Adding a new flight ");
                System.out.println("-Flight Number: " + arguments.elementAt(1));
                System.out.println("-Flight Seats: " + arguments.elementAt(2));
                System.out.println("-Flight Price: " + arguments.elementAt(3));

                outToServer.println(commandStr); // send the user's input to the server

                try{ 
                    String res = inFromServer.readLine(); // receive the result from the server

                    if (toBoolean(res)) {
                        System.out.println("Flight added");
                    } else {
                        System.out.println("Flight could not be added");
                    }
                }
                catch(IOException e){
                    System.out.println("Flight could not be added");
                }

                break;
            }
            case AddCars: {
                checkArgumentsCount(4, arguments.size());

                System.out.println("Adding new cars");
                System.out.println("-Car Location: " + arguments.elementAt(1));
                System.out.println("-Number of Cars: " + arguments.elementAt(2));
                System.out.println("-Car Price: " + arguments.elementAt(3));

                outToServer.println(commandStr); // send the user's input to the server

                try{ 
                    String res = inFromServer.readLine(); // receive the result from the server

                    if (toBoolean(res)) {
                        System.out.println("Cars added");
                    } else {
                        System.out.println("Cars could not be added");
                    }
                } catch (IOException e) {
                    System.out.println("Cars could not be added");
                }

                break;
            }
            case AddRooms: {
                checkArgumentsCount(4, arguments.size());

                System.out.println("Adding new rooms");
                System.out.println("-Room Location: " + arguments.elementAt(1));
                System.out.println("-Number of Rooms: " + arguments.elementAt(2));
                System.out.println("-Room Price: " + arguments.elementAt(3));

                outToServer.println(commandStr); // send the user's input to the server

                try{ 
                    String res = inFromServer.readLine(); // receive the result from the server

                    if (toBoolean(res)) {
                        System.out.println("Rooms added");
                    } else {
                        System.out.println("Rooms could not be added");
                    }
                } catch (IOException e) {
                    System.out.println("Rooms could not be added");
                }

                break;
            }
            case AddCustomer: {
                checkArgumentsCount(1, arguments.size());

                System.out.println("Adding a new customer:=");

                outToServer.println(commandStr); // send the user's input to the server

                String res = "";
                try{ 
                    res = inFromServer.readLine(); // receive the result from the server
                } catch (IOException e) {
                    System.out.println("Customer could not be added");
                    break;
                }

                int customer = toInt(res);

                System.out.println("Add customer ID: " + customer);
                break;
            }
            case AddCustomerID: {
                checkArgumentsCount(2, arguments.size());

                System.out.println("Adding a new customer");
                System.out.println("-Customer ID: " + arguments.elementAt(1));

                outToServer.println(commandStr); // send the user's input to the server

                try{ 
                    String res = inFromServer.readLine(); // receive the result from the server

                    if (toBoolean(res)) {
                        System.out.println("Add customer ID: " + arguments.elementAt(1));
                    } else {
                        System.out.println("Customer could not be added");
                    }
                } catch (IOException e) {
                    System.out.println("Customer could not be added");
                }

                break;
            }
            case DeleteFlight: {
                checkArgumentsCount(2, arguments.size());

                System.out.println("Deleting a flight");
                System.out.println("-Flight Number: " + arguments.elementAt(1));

                outToServer.println(commandStr); // send the user's input to the server

                try{ 
                    String res = inFromServer.readLine(); // receive the result from the server

                    if (toBoolean(res)) {
                        System.out.println("Flight Deleted");
                    } else {
                        System.out.println("Flight could not be deleted");
                    }
                } catch (IOException e) {
                    System.out.println("Flight could not be deleted");
                }

                break;
            }
            case DeleteCars: {
                checkArgumentsCount(2, arguments.size());

                System.out.println("Deleting all cars at a particular location");
                System.out.println("-Car Location: " + arguments.elementAt(1));

                outToServer.println(commandStr); // send the user's input to the server

                try{ 
                    String res = inFromServer.readLine(); // receive the result from the server

                    if (toBoolean(res)) {
                        System.out.println("Cars Deleted");
                    } else {
                        System.out.println("Cars could not be deleted");
                    }
                } catch (IOException e) {
                    System.out.println("Cars could not be deleted");
                }

                break;
            }
            case DeleteRooms: {
                checkArgumentsCount(2, arguments.size());

                System.out.println("Deleting all rooms at a particular location");
                System.out.println("-Car Location: " + arguments.elementAt(1));

                outToServer.println(commandStr); // send the user's input to the server

                try{
                    String res = inFromServer.readLine(); // receive the result from the server

                    if (toBoolean(res)) {
                        System.out.println("Rooms Deleted");
                    } else {
                        System.out.println("Rooms could not be deleted");
                    }
                } catch (IOException e) {
                    System.out.println("Rooms could not be deleted");
                }

                break;
            }
            case DeleteCustomer: {
                checkArgumentsCount(2, arguments.size());

                System.out.println("Deleting a customer from the database");
                System.out.println("-Customer ID: " + arguments.elementAt(1));

                outToServer.println(commandStr); // send the user's input to the server

                try{
                    String res = inFromServer.readLine(); // receive the result from the server

                    if (toBoolean(res)) {
                        System.out.println("Customer Deleted");
                    } else {
                        System.out.println("Customer could not be deleted");
                    }
                } catch (IOException e) {
                    System.out.println("Customer could not be deleted");
                }

                break;
            }
            case QueryFlight: {
                checkArgumentsCount(2, arguments.size());

                System.out.println("Querying a flight");
                System.out.println("-Flight Number: " + arguments.elementAt(1));

                outToServer.println(commandStr); // send the user's input to the server

                String res = "";
                try{
                    res = inFromServer.readLine(); // receive the result from the server
                } catch (IOException e) {
                    System.out.println("Flight could not be queried");
                    break;
                }

                int seats = toInt(res);
                System.out.println("Number of seats available: " + seats);
                break;
            }
            case QueryCars: {
                checkArgumentsCount(2, arguments.size());

                System.out.println("Querying cars location");
                System.out.println("-Car Location: " + arguments.elementAt(1));

                outToServer.println(commandStr); // send the user's input to the server

                String res = "";
                try {
                    res = inFromServer.readLine(); // receive the result from the server
                } catch (IOException e) {
                    System.out.println("Cars could not be queried");
                    break;
                }

                int numCars = toInt(res);
                System.out.println("Number of cars at this location: " + numCars);
                break;
            }
            case QueryRooms: {
                checkArgumentsCount(2, arguments.size());

                System.out.println("Querying rooms location");
                System.out.println("-Room Location: " + arguments.elementAt(1));

                outToServer.println(commandStr); // send the user's input to the server

                String res = "";
                try {
                    res = inFromServer.readLine(); // receive the result from the server
                } catch (IOException e) {
                    System.out.println("Rooms could not be queried");
                    break;
                }

                int numRoom = toInt(res);
                System.out.println("Number of rooms at this location: " + numRoom);
                break;
            }
            case QueryCustomer: {
                checkArgumentsCount(2, arguments.size());

                System.out.println("Querying customer information");
                System.out.println("-Customer ID: " + arguments.elementAt(1));

                outToServer.println(commandStr); // send the user's input to the server

                String res = "";
                try {
                    res = inFromServer.readLine(); // receive the result from the server
                } catch (IOException e) {
                    System.out.println("Customers could not be queried");
                    break;
                }

                String bill = res;
                System.out.print(bill);
                break;
            }
            case QueryFlightPrice: {
                checkArgumentsCount(2, arguments.size());

                System.out.println("Querying a flight price");
                System.out.println("-Flight Number: " + arguments.elementAt(1));

                outToServer.println(commandStr); // send the user's input to the server

                String res = "";
                try {
                    res = inFromServer.readLine(); // receive the result from the server
                } catch (IOException e) {
                    System.out.println("Flight Price could not be queried");
                    break;
                }

                int price = toInt(res);
                System.out.println("Price of a seat: " + price);
                break;
            }
            case QueryCarsPrice: {
                checkArgumentsCount(2, arguments.size());

                System.out.println("Querying cars price");
                System.out.println("-Car Location: " + arguments.elementAt(1));

                outToServer.println(commandStr); // send the user's input to the server

                String res = "";
                try {
                    res = inFromServer.readLine(); // receive the result from the server
                } catch (IOException e) {
                    System.out.println("Car Price could not be queried");
                    break;
                }

                int price = toInt(res);
                System.out.println("Price of cars at this location: " + price);
                break;
            }
            case QueryRoomsPrice: {
                checkArgumentsCount(2, arguments.size());

                System.out.println("Querying rooms price");
                System.out.println("-Room Location: " + arguments.elementAt(1));

                outToServer.println(commandStr); // send the user's input to the server

                String res = "";
                try {
                    res = inFromServer.readLine(); // receive the result from the server
                } catch (IOException e) {
                    System.out.println("Room Price could not be queried");
                    break;
                }

                int price = toInt(res);
                System.out.println("Price of rooms at this location: " + price);
                break;
            }
            case ReserveFlight: {
                checkArgumentsCount(3, arguments.size());

                System.out.println("Reserving seat in a flight");
                System.out.println("-Customer ID: " + arguments.elementAt(1));
                System.out.println("-Flight Number: " + arguments.elementAt(2));

                outToServer.println(commandStr); // send the user's input to the server

                try{
                    String res = inFromServer.readLine(); // receive the result from the server

                    if (toBoolean(res)) {
                        System.out.println("Flight Reserved");
                    } else {
                        System.out.println("Flight could not be reserved");
                    }
                } catch (IOException e) {
                    System.out.println("Flight could not be reserved");
                }

                break;
            }
            case ReserveCar: {
                checkArgumentsCount(3, arguments.size());

                System.out.println("Reserving a car at a location");
                System.out.println("-Customer ID: " + arguments.elementAt(1));
                System.out.println("-Car Location: " + arguments.elementAt(2));

                outToServer.println(commandStr); // send the user's input to the server
                try{
                    String res = inFromServer.readLine(); // receive the result from the server

                    if (toBoolean(res)) {
                        System.out.println("Car Reserved");
                    } else {
                        System.out.println("Car could not be reserved");
                    }
                } catch (IOException e) {
                    System.out.println("Car could not be reserved");
                }

                break;
            }
            case ReserveRoom: {
                checkArgumentsCount(3, arguments.size());

                System.out.println("Reserving a room at a location");
                System.out.println("-Customer ID: " + arguments.elementAt(1));
                System.out.println("-Room Location: " + arguments.elementAt(2));

                outToServer.println(commandStr); // send the user's input to the server

                try{
                    String res = inFromServer.readLine(); // receive the result from the server

                    if (toBoolean(res)) {
                        System.out.println("Room Reserved");
                    } else {
                        System.out.println("Room could not be reserved");
                    }
                } catch (IOException e) {
                    System.out.println("Room could not be reserved");
                }
                break;
            }
            case Bundle: {
                if (arguments.size() < 6) {
                    System.err.println((char) 27 + "[31;1mCommand exception: " + (char) 27
                            + "[0mBundle command expects at least 6 arguments. Location \"help\" or \"help,<CommandName>\"");
                    break;
                }

                System.out.println("Reserving an bundle");
                System.out.println("-Customer ID: " + arguments.elementAt(1));
                for (int i = 0; i < arguments.size() - 5; ++i) {
                    System.out.println("-Flight Number: " + arguments.elementAt(2 + i));
                }
                System.out.println("-Location for Car/Room: " + arguments.elementAt(arguments.size() - 3));
                System.out.println("-Book Car: " + arguments.elementAt(arguments.size() - 2));
                System.out.println("-Book Room: " + arguments.elementAt(arguments.size() - 1));

                outToServer.println(commandStr); // send the user's input to the server

                try{
                    String res = inFromServer.readLine(); // receive the result from the server

                    if (toBoolean(res)) {
                        System.out.println("Bundle Reserved");
                    } else {
                        System.out.println("Bundle could not be reserved");
                    }
                } catch (IOException e) {
                    System.out.println("Bundle could not be reserved");
                }

                break;
            }
            case Quit:
                checkArgumentsCount(1, arguments.size());

                System.out.println("Quitting client");
                System.exit(0);
        }
    }

    public static Vector<String> parse(String command) {
        Vector<String> arguments = new Vector<String>();
        StringTokenizer tokenizer = new StringTokenizer(command, ",");
        String argument = "";
        while (tokenizer.hasMoreTokens()) {
            argument = tokenizer.nextToken();
            argument = argument.trim();
            arguments.add(argument);
        }
        return arguments;
    }

    public static void checkArgumentsCount(Integer expected, Integer actual) throws IllegalArgumentException {
        if (expected != actual) {
            throw new IllegalArgumentException("Invalid number of arguments. Expected " + (expected - 1) + ", received "
                    + (actual - 1) + ". Location \"help,<CommandName>\" to check usage of this command");
        }
    }

    public static int toInt(String string) throws NumberFormatException {
        return (Integer.valueOf(string)).intValue();
    }

    public static boolean toBoolean(String string)// throws Exception
    {
        boolean result = false;
        if (string.equals("1")) {
            result = true;
        } else if (string.equals("0")) {
            result = false;
        } else {
            result = (Boolean.valueOf(string)).booleanValue();
        }

        return result;
    }
    
}

