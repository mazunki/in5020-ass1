#!/bin/sh
proxy() {
	java -jar bin/proxy.jar &
	sleep 2
}

server() {
	java -jar bin/server.jar $@ &
	sleep 0.5
}

proxy

server europe norge
server europe sweden
server europe denmark

server asia china
server asia japan

server america canada
server america brazil

server africa egypt


