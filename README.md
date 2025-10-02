# COMP512 Programming Assignment 1
A Travel Reservation system, a component-based distributed information system, where customers can reserve flights, cars and rooms for their vacation.

Script files are provided to help running the application. 

To run the resource managers as well as the middleware, a convenience script file run_servers.sh is provided:
```
cd Server/
./make_all_chmod_all.sh  # Only required on first run, to make sure all latest updates are applied.
./run_servers.sh  
```

To run the client:
```
cd Client
make    # Only required on first run
./run_client.sh [<middleware_host> [<middleware_name>]]
```

## Testing
To run the tests, in a running client session, send the command:
```
!, <file_path>
```
Where file_path are the path for the test files, which contains batched commands to be executed. Some test files are already provided under the Client/Client/Test folder.



