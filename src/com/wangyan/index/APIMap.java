package com.wangyan.index;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Map;

import dbhelper.DBHelper;

public class APIMap {
	
	public void setMap(Map<Integer, Integer> apiIndex_ID, Map<Integer, String> apiIndex_Name){
		//mashupIndex_ID = new HashMap<>();
		//mashupIndex_Name = new HashMap<>();
		
		Connection connection = DBHelper.getConnection();
		
		Statement statement = null;
		
		try {
			statement = connection.createStatement();
			
			String sql = "select MIN(N_ID), C_NAME from `tb_api` group by C_NAME order by MIN(N_ID);";
			
			ResultSet rs = statement.executeQuery(sql);
			
			int i = 0;
			while(rs.next()){
				apiIndex_ID.put(i, rs.getInt(1));
				apiIndex_Name.put(i, rs.getString(2));
				i++;
			}
			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			if(connection != null){
				try {
					connection.close();
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		
	}
}
