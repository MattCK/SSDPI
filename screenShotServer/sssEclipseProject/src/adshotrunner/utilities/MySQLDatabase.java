package adshotrunner.utilities;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class MySQLDatabase {
	
	//---------------------------------------------------------------------------------------
	//---------------------------------- Constants ------------------------------------------
	//---------------------------------------------------------------------------------------	
	final public static String DATABASEURL = "jdbc:mysql://adshotrunner.c4gwips6xiw8.us-east-1.rds.amazonaws.com/adshotrunner";
	final public static String DATABASEUSERNAME = "adshotrunner";
	final public static String DATABASEPASSWORD = "xbSAb2G92E";
	
	//--------------------------------------------------------------------------------------
	//---------------------------------- Static Methods ------------------------------------
	//--------------------------------------------------------------------------------------
	//***************************** Public Static Methods **********************************
	public static PreparedStatement getPreparedStatement(String query) throws SQLException {
		return getConnection().prepareStatement(query);
	}
	
	public static ResultSet executeQuery(String query) throws SQLException {
		return getConnection().createStatement().executeQuery(query);
	}

	//**************************** Private Static Methods **********************************
	private static Connection getConnection() throws SQLException {
		return DriverManager.getConnection(DATABASEURL, DATABASEUSERNAME, DATABASEPASSWORD);
	}

}
