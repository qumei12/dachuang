package test;

import java.sql.Connection;
import dbhelper.DBHelper;

public class Test {
	public static void main(String[] args) {
		Connection conn = DBHelper.getConnection();
		if (conn != null) {
			System.out.println("? 数据库连接成功！");
		} else {
			System.out.println("? 数据库连接失败！");
		}
	}
}

