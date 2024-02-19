package com.journaldev.jsf.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import com.journaldev.jsf.util.DataConnect;

public class RegisterDAO {

    public static boolean registerUser(String username, String email, String password) {
        Connection con = null;
        PreparedStatement ps = null;

        try {
            con = DataConnect.getConnection("myvoteapp");
            ps = con.prepareStatement("INSERT INTO `myvoteapp`.`users` (uname, email, password, admin) VALUES (?, ?, ?, 0)");
            ps.setString(1, username);
            ps.setString(2, email);
            ps.setString(3, password);

            int rowsAffected = ps.executeUpdate();

            return rowsAffected > 0;
        } catch (SQLException ex) {
            System.out.println("Registration error: " + ex.getMessage());
            return false;
        } finally {
            DataConnect.close(con);
        }
    }
}
