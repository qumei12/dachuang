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
	
	public int[][] getCaseSupplyMatrix(){
		Connection connection = DBHelper.getConnection();
		List<String> caseList = new ArrayList<String>();     // 病例列表
		List<String> supplyList = new ArrayList<String>();   // 耗材列表
		
		int caseAmount = 0;    // 病例数量
		int supplyAmount = 0;  // 耗材数量
		
		Statement statement;
		
		try {
			statement = connection.createStatement();
			
			// 获取病例数量
			ResultSet rs = statement.executeQuery("select count(*) from `tb_case`;");
			
			while (rs.next()) {
				caseAmount = rs.getInt(1);
			}
			
			// 获取耗材数量
			rs = statement.executeQuery("select count(distinct C_NAME) from `tb_supply`;");
			
			while(rs.next()) {
				supplyAmount = rs.getInt(1);
			}
			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		try {
			statement = connection.createStatement();
			
			// 获取所有病例ID
			ResultSet rs = statement.executeQuery("select N_ID from tb_case order by N_ID");
			while(rs.next()){
				caseList.add(rs.getString(1));
			}
			
			// 获取所有耗材名称 - 修改为与SupplyMap.java一致的排序方式
			rs = statement.executeQuery("select C_NAME from (select C_NAME, MIN(N_ID) as MIN_ID from tb_supply group by C_NAME) as t order by MIN_ID");
			while(rs.next()){
				supplyList.add(rs.getString(1));
			}
			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		// 创建病例-耗材矩阵
		int[][] caseSupplyMatrix = new int[caseAmount][supplyAmount];
		
		try {
			statement = connection.createStatement();
			
			// 填充矩阵
			for(int i = 0; i < caseList.size(); i++){
				ResultSet rs = statement.executeQuery(
					"select s.C_NAME from tb_supply s where s.N_CASE_ID = " + caseList.get(i)
				);
				
				while(rs.next()){
					String supplyName = rs.getString(1);
					int index = supplyList.indexOf(supplyName);
					if(index != -1){
						caseSupplyMatrix[i][index] = 1;
					}
				}
			}
			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return caseSupplyMatrix;
	}
	
	public int[][] getTrainSet(int[][] matrix, int row) {
		// TODO Auto-generated method stub
		int[][] trainMatrix = new int[row][matrix[0].length];
		for(int i = 0; i < row; i++) {
			for(int j = 0; j < matrix[i].length; j++) {
				trainMatrix[i][j] = matrix[i][j];
			}
		}
		return trainMatrix;
	}
	
	public int[][] getTestSet(int[][] matrix, int row) {
		// TODO Auto-generated method stub
		int[][] testMatrix = new int[matrix.length - row][matrix[0].length];
		for(int i = row; i < matrix.length; i++) {
			for(int j = 0; j < matrix[i].length; j++) {
				testMatrix[i - row][j] = matrix[i][j];
			}
		}
		return testMatrix;
	}
}