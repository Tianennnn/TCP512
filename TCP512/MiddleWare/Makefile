all: compile-MiddleWare-tcp

../Server/RMIInterface.jar: ../Server/Server/Interface/IResourceManager.java
	make -C ../Server/ RMIInterface.jar

compile-MiddleWare-tcp: MiddleWare/TCP/*.java MiddleWare/Common/*.java MiddleWare/Customers/*.java ../Server/RMIInterface.jar
	javac -cp ../Server/RMIInterface.jar:.:../Server/ MiddleWare/TCP/*.java MiddleWare/Common/*.java MiddleWare/Customers/*.java