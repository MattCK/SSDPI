package adshotrunner.utilities;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import adshotrunner.system.ASRProperties;

public class ASRDatabase {
	
	//---------------------------------------------------------------------------------------
	//---------------------------------- Constants ------------------------------------------
	//---------------------------------------------------------------------------------------	
	final private static String DATABASEURL = "jdbc:mysql://" + ASRProperties.asrDatabaseHost() + "/" + 
															   ASRProperties.asrDatabase();
	final private static String DATABASEUSERNAME = ASRProperties.asrDatabaseUsername();
	final private static String DATABASEPASSWORD = ASRProperties.asrDatabasePassword();
	
	//---------------------------------------------------------------------------------------
	//-------------------------------- Static Variables -------------------------------------
	//---------------------------------------------------------------------------------------	
	//***************************** Private Static Variables ********************************
	private static Connection databaseConnection = null;

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

	public static int executeUpdate(String query) throws SQLException {
		return getConnection().createStatement().executeUpdate(query);
	}

	//**************************** Private Static Methods **********************************
	private static Connection getConnection() throws SQLException {
		
		//If a connection has not been initialized, do so
		if (databaseConnection == null) {
			databaseConnection = DriverManager.getConnection(DATABASEURL, DATABASEUSERNAME, DATABASEPASSWORD);
		}
		
		return databaseConnection;
	}

}
