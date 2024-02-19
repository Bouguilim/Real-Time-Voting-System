package com.journaldev.jsf.util;

import javax.servlet.http.HttpSession;

public class Client {
	private String userId = SessionUtils.getUserId();
	
	public String getuserId() {
		return userId;
	}
	
	public boolean getisConnected() {
		HttpSession session = SessionUtils.getSession();
		if (session != null) {
			return true;
		}
		return false;
	}
}