package filehelper;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.text.SimpleDateFormat;
import java.text.ParseException;
import java.util.Date;

import dbhelper.DBHelper;


public class GetMA {
	
	public int[][] getArrayMA(){
		Connection connection = DBHelper.getConnection();
		List<Mashup> mashupList = new ArrayList<Mashup>();
		List<String> apiList = new ArrayList<String>();
		
		int MashupAmount = 0;
		int ApiAmount = 0;
		
		Statement statement;
		
		try {
			statement = connection.createStatement();
			
			ResultSet rs = statement.executeQuery("select count(*) from `tb_mashup`;");
			
			while (rs.next()) {
				MashupAmount = rs.getInt(1);
			}
			
			rs = statement.executeQuery("select count(distinct C_NAME) from `tb_api`;");
			
			while(rs.next()) {
				ApiAmount = rs.getInt(1);
			}
			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		try {
			statement = connection.createStatement();
			
			ResultSet rs = statement.executeQuery("select * from `tb_mashup`;");
			
			while(rs.next()){
				Mashup mashup = new Mashup();
				mashup.setN_ID(rs.getInt(1));
				mashup.setC_NAME(rs.getString(2));
				mashup.setC_DESCRIPTION(rs.getString(3));
				mashup.setC_URL(rs.getString(4));
				String dateStr = rs.getString(5);  // 改为 getString 读取日期字段
				if (dateStr != null && !dateStr.isEmpty()) {
					try {
						// 使用 SimpleDateFormat 解析日期格式
						SimpleDateFormat sdf = new SimpleDateFormat("yyyy-M-dd");
						Date utilDate = sdf.parse(dateStr);
						mashup.setD_DATE(new java.sql.Date(utilDate.getTime()));
					} catch (ParseException e) {
						System.err.println("日期解析失败：" + dateStr);
						mashup.setD_DATE(null);  // 或者设置默认值
					}
				} else {
					mashup.setD_DATE(null);
				}

				mashupList.add(mashup);
			}
			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		try {
			statement = connection.createStatement();
			
			ResultSet rs = statement.executeQuery("select distinct C_NAME from `tb_api`;");
			
			while(rs.next()){
//				statement = connection.createStatement();
//				ResultSet rs1 = statement.executeQuery("select * from `tb_api` where C_NAME = \"" + rs.getString(1) + "\" limit 1;");
//				
//				while (rs1.next()) {
//					API api = new API();
//					api.setN_ID(rs1.getInt(1));
//					api.setN_MASHUP_ID(rs1.getInt(2));
//					api.setN_MASHUP_API_ID(rs1.getInt(3));
//					api.setC_NAME(rs1.getString(4));
//					api.setC_DESCRIPTION(rs1.getString(5));
//					api.setC_URL(rs1.getString(6));
//					
//					apiList.add(api);
				
				apiList.add(rs.getString(1));
//				}
				
			}
			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		int[][] MA = new int[MashupAmount][ApiAmount];
		
		
		for (int i = 0; i < mashupList.size(); i++) {
			Mashup mu = mashupList.get(i);
			
			try {
				statement = connection.createStatement();
				
				ResultSet rs = statement.executeQuery("select C_NAME from `tb_api` where N_MASHUP_ID=" + mu.getN_ID() + ";");
				
				while (rs.next()) {
					MA[i][apiList.indexOf(rs.getString(1))] = 1;
				}
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			
		}
		
		//FileOut.printMatrix(MA, "MA数组"); 
		
		return MA;
	}
	
	public int[][] getTrainSet(int[][] MA,int row){
		
		int[][] train = new int[row][MA[0].length];
		
		HashSet<Integer> set = new HashSet<>();
		
		//System.out.println(row);
		
		while (set.size() < row) {
			set.add((int)((Math.random() * MA.length)));
		}
		
		Iterator<Integer> it = set.iterator();
		//System.out.println(set.size());
		
		int m = 0;
		while (it.hasNext()) {
			Integer integer = (Integer) it.next();
			
			for(int j = 0;j < MA[integer].length;j++){
				train[m][j] = MA[integer][j];
			}
			m++;
		}
		
//		for(int i = 0;i < row;i++){
//			for(int j = 0;j < MA[i].length;j++){
//				train[i][j] = MA[i][j];
//			}
//		}
		//FileOut.printMatrix(train, "MAtrain数组");
		
		return train;
	}
	
	public int[][] getTestSet(int[][] MA,int row){
		
		int[][] test = new int[MA.length - row + 1][MA[0].length];
		//System.out.println(MA.length);
		for(int i = row;i < MA.length;i++){
			for(int j = 0;j < MA[i].length;j++){
				test[i - row][j] = MA[i][j];
			}
		}
		//FileOut.printMatrix(test, "MAtest数组");
		
		return test;
	}
}
