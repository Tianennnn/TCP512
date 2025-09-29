# Usage: ./run_client.sh [<server_hostname> [<server_rmiobject>]]

java -cp ../Server/RMIInterface.jar:.:../Server/ Client.TCPClient $1 $2
