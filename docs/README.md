# IN5020 - Remote Method Invocation

Repository for the first assignment of IN5020, by group 7.


# Assignment description
There are a few noteworthy components in this assignment:

- The *proxy server* which acts as the load balancer of our network
- The *servers* which are responsible for doing the computation requested by the clients
- The *clients* which will request servers for information found on the *database*

It's also worth noting that the network is split into various *zones*. Different servers are registered to a given *zone*, and clients make requests from a known zone. We don't store any information from the client ourselves, and clients are responsible for knowing which zone they belong to.

## Zones
Per the assignment, zones can support up to a maxmium of 18 simultaneous requests, and will become saturated if above this threshold. When that happens, we will try the next two zones, in order, and fall back to the original zone and ignore its saturation.

In the example layout provided by the assignment, each zone has a one-to-one match with a server, but our implementation supports sharing the load of a given zone amongst all servers registered to a zone. From the perspective of the load balancer, fetching a reference to a server on a zone is a trivial `zone.getServer()` request, and the internal load balancing of each zone will be seamless.

## Servers
Each server is responsible for listening to requests coming from the load balancer, and responding with its result. To do this computation, the servers will need access to the database.

Servers will cache up to 150 results, specific to its zone. This cache is a FIFO bucket. We also have to add 80ms latency for each remote method invocation. For non-local servers, this should be increased by 90s, totalling 170ms.

Servers will be running on two threads: one for listening to requests, and one for executing their computation. Is it sensible to treat the Zone as one thread, and the server as the other?


## Clients
Clients will need to communicate with the proxy server to make requests, and wait for a response.

## Geoname database
There are mainly three potential approaches for reading the database, all with different caveats:
- Reading and parsing the file _on server startup_, storing all the information in memory until program exit. Given that our dataset contains 140k entries, and a mock Geoname object will roughly be of 100 bytes, we can estimate this will total around ~14M of data. This is within reason for our project.
- Reading and parsing the file _per request_. This will save us some memory footprint, but because our servers are all running locally on the same machine (during simulation, at least), this is less favourable. Each request will have to wait for the server(s) to finalize their parsing. That said, this has the benefit of allowing us to modify the database without restarting our servers.
- Migrating the CSV files into a proper database like SQLite or PostgreSQL. This is the ideal solution, since it would also allow us to store the database off-shore without too much hassle, and also handle concurrent connections to the database quite well. It's also a bit overkill for this project.

That said, because we are not allowed to do any preprocessing on the dataset, we will stick with option 2.

# Compiling and running
Everything required for compilation is contained within the `Makefile`.

Running `make` will build all the classfiles. `make all` will additionally build the contained `.jar` files. If you're only interested in the final executable jar files, `make all clean` is useful.

An example execution could look like the following:
```sh
make jarfiles
make clean

java -jar bin/proxy.jar &
sleep 1

java -jar bin/server.jar &
java -jar bin/server.jar &
java -jar bin/server.jar &
sleep 1

java -jar bin/client.jar
java -jar bin/client.jar
java -jar bin/client.jar
```

We should build some java-contained test files (i.e `src/test/java/...` files), but also some better test suites in our scripts. For this, we will need to parse arguments passed to `Server.main()` and `Client.main()`. Will this be required for `ProxyServer.main()` too?

The assignment requires some specific arguments for the client, but nothing is specified for the servers nor the proxy server. I believe it would be useful to specify the IP address(es) and port number to bind to as part of the arguments. We could specify these as `-D` fields during invocation too, if we need to reserve arguments for zone details and whatnot.

