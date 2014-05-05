
<%@ page import="javax.servlet.*" %>
<%@ page import="javax.servlet.http.*" %>
<%@ page import="java.sql.*" %>
<%@ page import="java.util.*" %>
<%@ page import="java.io.*" %>

<%

Connection conn = null;
Statement stmt = null;
ResultSet rs = null;

    Class.forName("com.mysql.jdbc.Driver");
    conn =
       DriverManager.getConnection("jdbc:mysql://10.1.1.17:3306/ssPrototype?" +
                                   "user=root&password=qwas12");
    
    stmt = conn.createStatement();
    if (stmt.execute("SELECT * FROM tagInfo WHERE TGI_id = " + request.getParameter("tagID"))) {
        rs = stmt.getResultSet();
    }
    // Do something with the Connection

	rs.next();
	String tagScript = rs.getString("TGI_usedTag");
	//out.println(tagScript);
	
	String scriptFile = "/var/lib/tomcat6/webapps/ROOT/scripts/" + request.getParameter("tagID") + ".html";
	String ssFile = "/var/lib/tomcat6/webapps/ROOT/scripts/sshfs/adClips/" + request.getParameter("tagID") + ".png";
	String scriptURL = "http://10.1.1.50/scripts/" + request.getParameter("tagID") + ".html"; 
	
	PrintWriter pw = new PrintWriter(new FileOutputStream(scriptFile));
    pw.println("<style>body { margin:0px; padding:0px; }</style>" + tagScript);
    pw.close();
	
	String s = null;
	
	Process p = Runtime.getRuntime().exec("java -jar /var/lib/tomcat6/webapps/ROOT/adClipper/adClipTester.jar " + scriptURL + " " + ssFile);
	
	p.waitFor();
	
	BufferedReader stdInput = new BufferedReader(new 
		InputStreamReader(p.getInputStream()));

	BufferedReader stdError = new BufferedReader(new 
		InputStreamReader(p.getErrorStream()));

	// read the output from the command
	out.println("Here is the standard output of the command:\n");
	while ((s = stdInput.readLine()) != null) {
		out.println(s);
	}
	
	// read any errors from the attempted command
	out.println("Here is the standard error of the command (if any):\n");
	while ((s = stdError.readLine()) != null) {
		out.println(s);
	}
	
	out.println("ok");

%>
<br>
Passed header: 
<%= request.getParameter("tagID") %><br><br>

Command: 
<%= "java -jar /var/lib/tomcat6/webapps/ROOT/adClipper/adClipTester.jar " + scriptURL + " " + ssFile %><br><br>

Command URL: 
<%= scriptURL %><br><br>

Command file name: 
<%= ssFile %><br><br>
