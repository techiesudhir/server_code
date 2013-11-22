
import java.sql.Connection;
import java.sql.DriverManager;
public class DatabaseHandler {
public Connection con = null;
String url = "jdbc:mysql://localhost:3306/";
String dbName = "womencell";
String driver = "com.mysql.jdbc.Driver";
String userName = "root"; 
String password = "delhiwomencell@181";

 public Connection getConnection(){
	 try {
		  Class.forName(driver).newInstance();
		  con = DriverManager.getConnection(url+dbName,userName,password);
		  //System.out.println("Connected to the database");
		  //con.close();
		  //System.out.println("Disconnected from database");
		  } catch (Exception e) {
		  e.printStackTrace();
		  }
		  return con;
		  //
}
 public Connection getForeignConnection(String db){
	 try {
		  Class.forName(driver).newInstance();
		  con = DriverManager.getConnection(url+db,userName,password);
		  //System.out.println("Connected to the database");
		  //con.close();
		  //System.out.println("Disconnected from database");
		  } catch (Exception e) {
		  e.printStackTrace();
		  }
		  return con;
		  //
}
 
}
