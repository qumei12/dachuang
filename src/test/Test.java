package test;

import java.sql.Connection;
import dbhelper.DBHelper;

public class Test {
	public static void main(String[] args) {
		Connection conn = DBHelper.getConnection();
		if (conn != null) {
			System.out.println("? ���ݿ����ӳɹ���");
		} else {
			System.out.println("? ���ݿ�����ʧ�ܣ�");
		}
	}
}

