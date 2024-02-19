package com.journaldev.jsf.beans;

import java.rmi.Naming;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;

import java.io.IOException;
import java.io.Serializable;
import java.net.Socket;

import com.Server.*;

@ManagedBean
@SessionScoped
public class AuthenticationClient implements Serializable {
    private static final long serialVersionUID = 2L;
    static String role;
    public static String serverAddress = "localhost";
    public static int serverPort = 8000;

	public static String AuthentificateAndConnect() {
        try {
        	
            AuthenticationService authenticationService = (AuthenticationService) Naming.lookup("rmi://localhost:1099/VoteAuthentication");
            role = authenticationService.validateUsernamePassword();
            try {
				Socket socket = new Socket(serverAddress, serverPort);
			} catch (IOException e) {
				e.printStackTrace();
				return "";
			}
            return role;
            
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }
    
	public String goRegister() {
		return "register";
	}
}
