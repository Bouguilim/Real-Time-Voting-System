package com.Server;

import com.journaldev.jsf.dao.*;
import com.journaldev.jsf.util.*;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.context.FacesContext;
import javax.servlet.http.HttpSession;

import java.io.IOException;
import java.io.Serializable;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;


@ManagedBean
@SessionScoped
public class AuthenticationServiceImpl extends UnicastRemoteObject implements AuthenticationService, Serializable {
    private static final long serialVersionUID = 1L;
    
    private String pwd;
	private String msg;
	private String user;
    public static Object receivedObject;
    
	public String getPwd() {
		return pwd;
	}

	public void setPwd(String pwd) {
		this.pwd = pwd;
	}

	public String getMsg() {
		return msg;
	}

	public void setMsg(String msg) {
		this.msg = msg;
	}

	public String getUser() {
		return user;
	}

	public void setUser(String user) {
		this.user = user;
	}


    public AuthenticationServiceImpl() throws RemoteException {
        super();
    }
   

    @Override
    public String validateUsernamePassword() throws RemoteException, IOException {
    	String role = LoginDAO.validate(user, pwd);
		if (role != null) {
			HttpSession session = SessionUtils.getSession();
			session.setAttribute("username", user);
			int userId = LoginDAO.getUserId(user, pwd); 
	        session.setAttribute("userid", String.valueOf(userId));
	        System.out.println(userId);
			if (role.equals("admin")) {
	            return "admin";
	        } else if (role.equals("user")) {
	            return "user";
	        }
		} else {
			FacesContext.getCurrentInstance().addMessage(
					null,
					new FacesMessage(FacesMessage.SEVERITY_WARN,
							"Incorrect Username and Passowrd",
							"Please enter correct username and Password"));
			return "login";
		}
		return "login";
	}
    
    public String logout() {
		HttpSession session = SessionUtils.getSession();
		session.invalidate();
		return "login";
	}
	
	public String goRegister() {
		return "register";
	}
}