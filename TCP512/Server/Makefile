all: compile-server-tcp

RMIInterface.jar: Server/Interface/IResourceManager.java
	@echo "Compiling RMI server interface"
	javac Server/Interface/IResourceManager.java Server/Common/TCPMessage.java Server/Common/Trace.jave
	jar cvf RMIInterface.jar Server/Interface/IResourceManager.class Server/Common/TCPMessage.class Server/Common/Trace.class

compile-server-tcp: Server/TCP/*.java Server/Interface/IResourceManager.java Server/Common/*.java Server/DistributedCommon/*.java
	javac Server/TCP/*.java Server/DistributedCommon/*.java Server/Interface/IResourceManager.java Server/Common/*.java

clean:
	rm -f Server/Interface/*.class Server/Common/*.class Server/RMI/*.class
	rm -f RMIInterface.jar
