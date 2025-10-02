# Usage: ./run_client.sh [<server_hostname>

java -cp ../Server/RMIInterface.jar:.:../Server/ Client.TCPClient $1 $2
