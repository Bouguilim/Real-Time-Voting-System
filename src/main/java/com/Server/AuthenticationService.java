package com.Server;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.io.IOException;

public interface AuthenticationService extends Remote {
    String validateUsernamePassword() throws RemoteException, IOException;
}
