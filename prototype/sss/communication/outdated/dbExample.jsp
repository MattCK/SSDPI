<%-- 

	If you aren't a Java programmer, don't let all of this code scare you off.
	If you stick to using the features that I show you how to create on this site,
	you won't have to do coding like this on your documentation.
	
	If you are a Java programmer, then you will probably be able to tell that I am
	only a novice Java programmer. Please feel free to point out any weird habits I
	may have picked up!
	
	This page connects to the database to extract the departments that are available, 
	and populates the drop-down list box on the page with the values retrieved.

--%>

<%-- Import the necessary Java classes --%>
<%@ page import="javax.servlet.*" %>
<%@ page import="javax.servlet.http.*" %>
<%@ page import="java.sql.*" %>
<%@ page import="java.util.*" %>

<%		
		//create a Connection, Statement, and ResultSet object to connect to the database
		Connection con = null;
    Statement stmt = null;
    ResultSet rs = null;
		
		//create a query to extract data from the database
		String queryText = "select distinct Department from dbtutorial";
    try {
			//initialize the JDBC-ODBC bridge driver
      Class.forName("sun.jdbc.odbc.JdbcOdbcDriver");
			
			//open a connection to the database
			String username = "";
			String password = "";
      con = DriverManager.getConnection("jdbc:odbc:dbtutorial",username,password);
			
			//execute the query
      stmt = con.createStatement();
      rs = stmt.executeQuery(queryText);
    } catch (Exception e) {}
		
		//create the text for the two drop-down list boxes on the page
		//doing it this way is faster than parsing the database twice to create them
		//they use Vectors, which are basically lists of objects; in this case, lists of Strings
		//anytime you use a Vector object, you must import the java.util.* classes
		Vector s_listbox = new Vector();
		Vector i_listbox = new Vector();
		
		//add first line to each of the Vector objects
		s_listbox.add("<select name=\"s_department\">"); //what this adds to the Vector is: <select name="s_department">
		i_listbox.add("<select name=\"i_department\">"); //and this adds: <select name="i_department">
		
		//add empty entry to the Search list box Vector
		s_listbox.add("<option value=\"\"> </option>");
		
		//loop through all of the records in the database
		while (rs.next()) {
			//store value from the current record in the database in a String object
			String value = rs.getString("Department");
			
			//add the option entry to each of the Vectors
			s_listbox.add("<option value=\"" + value + "\">" + value + "</option>");
			i_listbox.add("<option value=\"" + value + "\">" + value + "</option>");
		}
		
		//add last line to the drop-down list box syntax
		s_listbox.add("</select>");
		i_listbox.add("</select>");
		
%>

<html>
<head>
<title>JSP Database Connectivity Tutorial</title>
<meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1">
<link rel="stylesheet" href="JSP.css" type="text/css">
</head>

<body bgcolor="#FFFFFF" text="#000000">
<table width="100%" cellpadding="5">
  <tr valign="top"> 
    <td> 
      <table width="100%" cellpadding="5" bordercolor="#000066" bgcolor="#FFFFFF" border="1" cellspacing="0">
        <tr> 
          <td> 
            <h1>JSP Database Connectivity Tutorial </h1>
            <p><b>Congratulations!</b> If you are viewing this page in your web 
              browser by pointing your browser to <span class="code">http://localhost:8080/examples/jsp/dbtutorial/index.jsp</span>, 
              then you have successfully set up your Tomcat web server.</p>
            <p>Using the features in this included set of JSP files, you can determine 
              if you are also able to successfully connect to, extract data from, 
              and insert data into the provided Access database (or the ODBC-compliant, 
              non-Access database you created).</p>
            <p>This set of JSP files enables you to query the database to extract 
              contact information and add new contacts. Try both listing contact 
              data from the database and adding your own contacts to verify that 
              you are both able to extract data from and insert data into the 
              database.</p>
            <hr>
            <h2>Search the contact database</h2>
            <p>The more information you fill in in the following form, the fewer 
              responses you will receive. You can enter partial information, such 
              as a few letters of a first or last name. </p>
            <p>If you want to list <b>all</b> of the contacts in the database, 
              leave all of the fields blank, and click <b>Submit</b>.</p>
            <form name="form1" method="post" action="search.jsp">
              <table border="0" cellspacing="0" cellpadding="5">
                <tr> 
                  <td> 
                    <div align="right">First name</div>
                  </td>
                  <td> 
                    <input type="text" name="s_firstName">
                  </td>
                </tr>
                <tr> 
                  <td> 
                    <div align="right">Last name</div>
                  </td>
                  <td> 
                    <input type="text" name="s_lastName">
                  </td>
                </tr>
                <tr> 
                  <td> 
                    <div align="right">Email address</div>
                  </td>
                  <td> 
                    <input type="text" name="s_email">
                  </td>
                </tr>
                <tr> 
                  <td> 
                    <div align="right">Department</div>
                  </td>
                  <td> 
                    <%-- Add the lines from the Vector object created earlier --%>
                    <% for (int i=0; i<s_listbox.size(); i++) { %>
                    <%= (String)s_listbox.elementAt(i) %> 
                    <% } %>
                  </td>
                </tr>
              </table>
              <p> 
                <input type="submit" name="Submit" value="Submit">
                <input type="reset" name="Reset" value="Reset">
              </p>
            </form>
            <hr>
            <h2>Add a contact to the database</h2>
            <form name="form2" method="post" action="add.jsp">
              <table border="0" cellspacing="0" cellpadding="5">
                <tr> 
                  <td> 
                    <div align="right">First name</div>
                  </td>
                  <td> 
                    <input type="text" name="i_firstName">
                  </td>
                </tr>
                <tr> 
                  <td> 
                    <div align="right">Last name</div>
                  </td>
                  <td> 
                    <input type="text" name="i_lastName">
                  </td>
                </tr>
                <tr> 
                  <td> 
                    <div align="right">Email address</div>
                  </td>
                  <td> 
                    <input type="text" name="i_email">
                  </td>
                </tr>
                <tr> 
                  <td> 
                    <div align="right">Department</div>
                  </td>
                  <td> 
                    <%-- Add the lines from the Vector object created earlier --%>
                    <% for (int i=0; i<i_listbox.size(); i++) { %>
                    <%= (String)i_listbox.elementAt(i) %> 
                    <% } %>
                  </td>
                </tr>
              </table>
              <p> 
                <input type="submit" name="Submit3" value="Submit">
                <input type="reset" name="Reset2" value="Reset">
              </p>
            </form>
          </td>
        </tr>
      </table>
    </td>
  </tr>
</table>
</body>
</html>
