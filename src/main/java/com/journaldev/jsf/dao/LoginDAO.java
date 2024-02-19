package com.journaldev.jsf.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.journaldev.jsf.util.DataConnect;

public class LoginDAO {

	public static String validate(String user, String password) {
		Connection con = null;
		PreparedStatement ps = null;

		try {
			con = DataConnect.getConnection("myvoteapp");
			ps = con.prepareStatement("SELECT uname, admin FROM `myvoteapp`.`users` WHERE uname = ? AND password = ?");
			ps.setString(1, user);
			ps.setString(2, password);

			ResultSet rs = ps.executeQuery();

			if (rs.next()) {
				int adminValue = rs.getInt("admin");
	            if (adminValue == 1) {
	                return "admin";
	            } else {
	                return "user";
	            }
			}
		} catch (SQLException ex) {
			System.out.println("Login error -->" + ex.getMessage());
		} finally {
			DataConnect.close(con);
		}
		return null;
	}
	
	public static int getUserId(String username, String password) {
        Connection con = null;
        PreparedStatement ps = null;
        int uid = 0;

        try {
            con = DataConnect.getConnection("myvoteapp");
            ps = con.prepareStatement("SELECT uid FROM `myvoteapp`.`users` WHERE uname = ? AND password = ?");
            ps.setString(1, username);
            ps.setString(2, password);

            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                uid = rs.getInt("uid");
            }
        } catch (SQLException ex) {
            System.out.println("Get user's uid error -->" + ex.getMessage());
        } finally {
            DataConnect.close(con);
        }

        return uid;
    }
}