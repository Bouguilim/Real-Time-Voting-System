package com.journaldev.jsf.util;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.faces.context.FacesContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import com.mysql.jdbc.Connection;
import com.mysql.jdbc.PreparedStatement;

public class SessionUtils {

    public static HttpSession getSession() {
        return (HttpSession) FacesContext.getCurrentInstance().getExternalContext().getSession(false);
    }

    public static HttpServletRequest getRequest() {
        return (HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest();
    }

    public static String getUserName() {
        HttpSession session = (HttpSession) FacesContext.getCurrentInstance().getExternalContext().getSession(false);
        return session.getAttribute("username").toString();
    }

    public static String getUserId() {
        HttpSession session = getSession();
        if (session != null) {
            return (String) session.getAttribute("userid");
        }
        return null;
    }

    public static List<String> getAllUsers() {
        List<String> allUsers = new ArrayList<>();
        Connection con = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            con = (Connection) DataConnect.getConnection("myvoteapp"); 
            ps = (PreparedStatement) con.prepareStatement("SELECT uid FROM `myvoteapp`.`users`");
            rs = ps.executeQuery();

            while (rs.next()) {
                String userId = rs.getString("uid");
                allUsers.add(userId);
            }
        } catch (SQLException e) {
            e.printStackTrace(); 
        } finally {
            DataConnect.close(con);
        }

        return allUsers;
    }
    
 
}
