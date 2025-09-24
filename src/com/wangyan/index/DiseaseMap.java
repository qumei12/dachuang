package com.wangyan.index;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Map;

import dbhelper.DBHelper;

public class DiseaseMap {
	
	public void setMap(Map<Integer, Integer> diseaseIndex_ID, Map<Integer, String> diseaseIndex_Name){
		//diseaseIndex_ID = new HashMap<>();
		//diseaseIndex_Name = new HashMap<>();
		
		Connection connection = DBHelper.getConnection();
		
		Statement statement = null;
		
		try {
			statement = connection.createStatement();
			
			String sql = "select N_ID, C_NAME from `tb_disease`;";
			
			ResultSet rs = statement.executeQuery(sql);
			
			int i = 0;
			while(rs.next()){
				diseaseIndex_ID.put(i, rs.getInt(1));
				diseaseIndex_Name.put(i, rs.getString(2));
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