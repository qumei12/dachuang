package dbhelper;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBHelper {

	public DBHelper() {
		super();
		// TODO Auto-generated constructor stub
	}
	
	protected static Connection connection;
	
	protected static String dbClassName = "com.mysql.cj.jdbc.Driver";
	protected static String dbUrl = "jdbc:mysql://localhost/db_mashup";
	protected static String dbUser = "root";
	protected static String dbPassword = "root";
	
	public static Connection getConnection() {
			try {
				Class.forName(dbClassName).newInstance();
				//System.out.println("数据库驱动加载成功");
			} catch (InstantiationException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			} catch (IllegalAccessException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			} catch (ClassNotFoundException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}

		
		try {
			connection = DriverManager.getConnection(dbUrl, dbUser, dbPassword);
			//System.out.println("数据库连接成功");
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return connection;
	}
		
}
