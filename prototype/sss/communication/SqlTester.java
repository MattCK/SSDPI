/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package sqltester;

@ page import java.sql.Connection;
@ page import java.sql.DriverManager;
@ page import java.sql.SQLException;
@ page import java.sql.Statement;
@ page import java.sql.ResultSet;

/**
 *
 * @author matt
 */
public class SqlTester {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws ClassNotFoundException {
        
Connection conn = null;
Statement stmt = null;
ResultSet rs = null;

try {
    Class.forName("com.mysql.jdbc.Driver");
    conn =
       DriverManager.getConnection("jdbc:mysql://10.1.1.17:3306/javatest?" +
                                   "user=javatest&password=qwas12");
    
    System.out.println("connectionMade");
    stmt = conn.createStatement();
    System.out.println("StatenebtMade");
    if (stmt.execute("SELECT * FROM `tester` WHERE 1")) {
        rs = stmt.getResultSet();
        
    }
    System.out.println("Statement Executed");
    // Do something with the Connection


} catch (SQLException ex) {
    // handle any errors
    System.out.println("SQLException: " + ex.getMessage());
    System.out.println("SQLState: " + ex.getSQLState());
    System.out.println("VendorError: " + ex.getErrorCode());
        
        // TODO code application logic here
    }
    
}
}
