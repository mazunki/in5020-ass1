#!/bin/sh

PROXYPID=

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

server europe norge
server europe sweden
server europe denmark

server asia china
server asia japan

server america canada
server america brazil

server africa egypt

sleep 3 && echo "ok... waiting"

wait

