package com.Server;

import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;


public class RMIServer {
    private static final String RMI_REGISTRY_NAME = "rmi://localhost:1099/VoteAuthentication";

    public static void main(String[] args) {
        try {
            startRMIRegistry();
            AuthenticationServiceImpl authenticationService = new AuthenticationServiceImpl();
            Naming.rebind(RMI_REGISTRY_NAME, authenticationService);
            System.out.println("RMI server is running.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void startRMIRegistry() throws RemoteException {
        LocateRegistry.createRegistry(1099);
    }
}

