package com.ass1.loadbalancer;

import java.rmi.Remote;
import java.rmi.RemoteException;

import com.ass1.*;
import com.ass1.server.*;

public interface ProxyServerInterface extends Remote {
	public final static String PROXY_IDENTIFIER = "proxyserver";
	static final long serialVersionUID = 1L;

	public void registerZone(Identifier zone) throws RemoteException;

	public void startupTask(ServerInterface server, Identifier zoneId) throws RemoteException;

	public void completeTask(ServerInterface server, Identifier zoneId) throws RemoteException;

	public void register(Zone zone) throws RemoteException;

	public void register(ServerInterface server, Identifier zoneId, Identifier serverId) throws RemoteException;

	public void unregister(ServerInterface server, Identifier zoneId, Identifier serverId) throws RemoteException;

	public void stop() throws RemoteException;

	public ServerInterface getServer(Identifier zoneId) throws RemoteException;
}
