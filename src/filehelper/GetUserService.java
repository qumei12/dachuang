package filehelper;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import dbhelper.DBHelper;

public class GetUserService {
	
	public static Integer[][] getUsersServices(){
		
		double trainset = 1;
		
		GetMA gm = new GetMA();
		int[][] MA = gm.getArrayMA();
		
		int row = (int)(MA.length * trainset);
		
		int[][] trainMA = gm.getTrainSet(MA, row);
		//int[][] testMA = gm.getTestSet(MA, row);
		Integer[][] UsersServices = new Integer[trainMA.length][trainMA[0].length];
		
		for(int i = 0;i < UsersServices.length;i++){
			for(int j = 0;j < UsersServices[i].length;j++){
				UsersServices[i][j] = trainMA[i][j];
			}
		}
		
		return UsersServices;
		
	}
	
	/**
	 * 根据病种ID获取该病种的所有病案索引列表
	 * @param diseaseId 病种ID
	 * @return 病案索引列表
	 */
	public static List<Integer> getCaseIndexesByDiseaseId(int diseaseId) {
		List<Integer> caseIndexes = new ArrayList<>();
		
		Connection connection = DBHelper.getConnection();
		Statement statement = null;
		
		try {
			statement = connection.createStatement();
			
			// 查询指定病种的所有病案ID
			String sql = "SELECT N_ID FROM tb_case WHERE N_MASHUP_ID = " + diseaseId + " ORDER BY N_ID";
			ResultSet rs = statement.executeQuery(sql);
			
			// 获取所有病案的ID列表
			List<Integer> allCaseIds = new ArrayList<>();
			GetMA gm = new GetMA();
			int[][] MA = gm.getArrayMA();
			
			Statement statement2 = connection.createStatement();
			ResultSet rs2 = statement2.executeQuery("SELECT N_ID FROM tb_case ORDER BY N_ID");
			while (rs2.next()) {
				allCaseIds.add(rs2.getInt(1));
			}
			
			// 查找这些病案在矩阵中的索引位置
			while (rs.next()) {
				int caseId = rs.getInt(1);
				int index = allCaseIds.indexOf(caseId);
				if (index != -1) {
					caseIndexes.add(index);
				}
			}
			
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			try {
				if (statement != null) {
					statement.close();
				}
				if (connection != null) {
					connection.close();
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		
		return caseIndexes;
	}
}