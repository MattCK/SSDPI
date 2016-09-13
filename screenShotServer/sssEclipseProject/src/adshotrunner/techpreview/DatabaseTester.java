package adshotrunner.techpreview;

import java.sql.ResultSet;
import java.sql.SQLException;

import adshotrunner.utilities.MySQLDatabase;

public class DatabaseTester {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		try {
			ResultSet domainSet = MySQLDatabase.executeQuery("SELECT * FROM menuDomains");
			while (domainSet.next()) {
				System.out.println(domainSet.getString(2));
			}
			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
