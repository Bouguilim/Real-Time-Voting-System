package com.journaldev.jsf.beans;

import java.io.Serializable;


import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;


@ManagedBean
@SessionScoped
public class UserBean implements Serializable {

	private static final long serialVersionUID = 1L;
	
	public String submitVotes() {
		// Process the submitted votes and update the database
		// ...

		// Remove submitted votes from the remainingVotes list
		// ...

		// Update the remainingVotes in SessionUtils

		// Redirect to a success page or perform other actions
		return "user";
	}

	

	// Other methods as needed
}
