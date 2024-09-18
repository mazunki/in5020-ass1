#!/bin/sh

proxy() {
	java -jar bin/proxy.jar &
	PROXYPID=$!
	sleep 2
}

server() {
	java -jar bin/server.jar $@ &
	SERVERPIDS="${SERVERPIDS} $!"
	sleep 0.5
}

kill_simulation() {
	kill $SERVERPIDS
	sleep 2
	kill $PROXYPID
}

trap kill_simulation INT

proxy

server 1 norge
server 2 china
server 3 canada
server 4 egypt
server 5 australia

sleep 3 && echo "ok... waiting"

wait

