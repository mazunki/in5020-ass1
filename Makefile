

server: target/classes/com/ass1/server/Server.class
	mvn exec:java -Dexec.mainClass="com.ass1.server.Server" -Djava.rmi.server.hostname="127.0.0.1"

client: target/classes/com/ass1/client/Client.class
	mvn exec:java -Dexec.mainClass="com.ass1.client.Client" -Djava.rmi.server.hostname="127.0.0.1"
