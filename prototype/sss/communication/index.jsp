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
       DriverManager.getConnection("jdbc:mysql://10.1.1.64:3306/ssPrototype?" +
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
	String ssFile = "/adClips/" + request.getParameter("tagID") + ".png";
	//String ssFile = "/var/lib/tomcat6/webapps/ROOT/sshfs/adClips/" + request.getParameter("tagID") + ".png";
	String scriptURL = "http://10.1.1.59/scripts/" + request.getParameter("tagID") + ".html"; 
	
	PrintWriter pw = new PrintWriter(new FileOutputStream(scriptFile));
    pw.println("<style>body { margin: 75px 0 0 425px; padding:0px;}</style>" + tagScript);
    pw.close();
	
	String s = null;
	
	Process p = Runtime.getRuntime().exec("java -jar /var/lib/tomcat6/webapps/ROOT/adClipper/adClipper.jar " + scriptURL + " " + request.getParameter("tagID"));
		
	/*out.println("<br>Command: java -jar /var/lib/tomcat6/webapps/ROOT/adClipper/adClipTester.jar " + scriptURL + " " + ssFile);
	out.println(p.getOutputStream());
		
		
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
	}*/
	
	out.println("ok");

%>