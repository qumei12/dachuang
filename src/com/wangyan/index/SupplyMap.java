package com.wangyan.index;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Map;

import dbhelper.DBHelper;

public class SupplyMap {
	
	public void setMap(Map<Integer, Integer> supplyIndex_ID, Map<Integer, String> supplyIndex_Name){
		//supplyIndex_ID = new HashMap<>();
		//supplyIndex_Name = new HashMap<>();
		
		Connection connection = DBHelper.getConnection();
		
		Statement statement = null;
		
		try {
			statement = connection.createStatement();
			
			String sql = "select MIN(N_ID), C_NAME from `tb_supply` group by C_NAME order by MIN(N_ID);";
			
			ResultSet rs = statement.executeQuery(sql);
			
			int i = 0;
			while(rs.next()){
				supplyIndex_ID.put(i, rs.getInt(1));
				supplyIndex_Name.put(i, rs.getString(2));
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