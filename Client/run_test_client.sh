# Usage: ./run_client.sh [<server_hostname>

java -cp ../Server/RMIInterface.jar:.:../Server/ Client.TCPTestClient $1 $2
