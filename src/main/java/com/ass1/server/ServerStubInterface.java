package com.ass1.server;

import java.rmi.Remote;
import java.rmi.RemoteException;

// import com.ass1.Identifier;

public interface ServerStubInterface extends Remote {
	public Object call(String method, Object[] args) throws RemoteException;
}
