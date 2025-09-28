
# echo "Edit file run_middleware.sh to include instructions for launching the middleware"
echo '  $1 - server name of Flights'
echo '  $2 - server name of Cars'
echo '  $3 - server name of Room'

java -cp .:../Server/RMIInterface.jar:../Server MiddleWare.TCP.TCPMiddleware $1 $2 $3