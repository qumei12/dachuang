package dbhelper;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.sql.PreparedStatement;
import java.util.List;

//import com.sun.org.apache.regexp.internal.recompile;

import javabean.Supply;
import javabean.Disease;
import javabean.DiseaseJson;

public class DBSearch {

	public int getPageAmount() {
		Connection connection = DBHelper.getConnection();

		int DiseaseAmount = -1;

		Statement statement;

		try {
			statement = connection.createStatement();

			ResultSet rs = statement.executeQuery("select count(*) from `tb_disease`;");

			while (rs.next()) {
				DiseaseAmount = rs.getInt(1);
			}

		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			try {
				connection.close();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		int pageAmount = 0;

		if (DiseaseAmount != -1) {
			pageAmount = DiseaseAmount / 20 + 1;
		}

		return pageAmount;
	}

	public ArrayList<DiseaseJson> getMashupTable(int startId, int count) {
		Connection connection = DBHelper.getConnection();

		Statement statement;

		ArrayList<DiseaseJson> mashupList = new ArrayList<DiseaseJson>();

		try {
			statement = connection.createStatement();
			String sql = "select * from `tb_disease` order by N_ID limit " + startId + "," + count + ";";
			//System.out.println(sql);
			ResultSet rs = statement.executeQuery(sql);

			while (rs.next()) {
				DiseaseJson mashup = new DiseaseJson();
				mashup.setN_ID(rs.getInt(1));
				mashup.setC_NAME(rs.getString(2));
				mashup.setC_DESCRIPTION(rs.getString(3));
				// 根据数据库实际结构，表中可能没有C_URL和C_DATE字段
				// mashup.setC_URL(rs.getString(4));
				// Date date = rs.getDate(5);
				// String date_s = (date.getYear() + 1900) + "-" + (date.getMonth() + 1) + "-" + date.getDate();
				// mashup.setC_DATE(date_s);

				mashupList.add(mashup);
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			try {
				connection.close();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		return mashupList;
	}

	public ArrayList<Supply> getDiseaseSupplyRelation(int diseaseId) {
		Connection connection = DBHelper.getConnection();

		Statement statement;

		ArrayList<Supply> list = new ArrayList<Supply>();

		try {
			statement = connection.createStatement();

			String sql = "select * from `tb_supply` where n_mashup_id=" + diseaseId + " order by N_ID ";
			System.out.println("执行查询关联耗材: " + sql); // 添加调试信息

			ResultSet rs = statement.executeQuery(sql);

			while (rs.next()) {
				Supply supply = new Supply();
				supply.setID(rs.getInt(1));
				supply.setNAME(rs.getString(4));
				supply.setDESCRIPTION(rs.getString(5));
				supply.setURL(rs.getString(6));
				supply.setDISEASE_ID(rs.getInt(2));
				supply.setSUPPLY_ID(rs.getInt(3));

				list.add(supply);
			}
			
			System.out.println("查询结果数量: " + list.size() + " (病种ID: " + diseaseId + ")"); // 添加调试信息

		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			try {
				connection.close();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return list;
	}

	public Map<Integer, Integer> getApiSequ() {
		Connection connection = DBHelper.getConnection();

		Statement statement = null;

		Map<Integer, Integer> map = new HashMap<Integer, Integer>();


		try {
			statement = connection.createStatement();

			ResultSet rs = statement.executeQuery("select distinct(c_name), n_id from `tb_supply` group by c_name order by n_id;");

			int order = 0;

			while (rs.next()) {
				map.put(order++, rs.getInt(2));
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			try {
				connection.close();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return map;
	}

	public Supply getSupplyById(int id) {
		Connection connection = DBHelper.getConnection();

		Statement statement = null;

		String sql = "select * from `tb_supply` where n_id=" + id + ";";

		Supply supply = new Supply();

		try {
			statement = connection.createStatement();

			ResultSet rs = statement.executeQuery(sql);

			while (rs.next()) {
				supply.setID(rs.getInt(1));                           // N_ID - 耗材ID
				supply.setNAME(rs.getString(3));                      // C_NAME - 耗材名称
				supply.setDESCRIPTION(rs.getString(6));               // C_PRICE - 价格
				supply.setURL(rs.getString(5));                       // C_SPECIFICATION - 规格
				supply.setPRODUCT_NAME(rs.getString(4));              // C_PRODUCT_NAME - 产品名称
				supply.setPRICE(rs.getString(6));                     // C_PRICE - 价格
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			try {
				connection.close();
			} catch (SQLException e) {
				// TODO Auto-generated block
				e.printStackTrace();
			}
		}

		return supply;
	}

	public Disease getMashupById(int id) {
		Connection connection = DBHelper.getConnection();

		Statement statement = null;

		String sql = "select * from `tb_disease` where n_id=" + id + ";";

		Disease disease = new Disease();

		try {
			statement = connection.createStatement();

			ResultSet rs = statement.executeQuery(sql);

			while (rs.next()) {
				disease.setID(rs.getInt(1));
				disease.setNAME(rs.getString(2));
				disease.setDESCRIPTION(rs.getString(3));
				// 根据数据库实际结构，表中可能没有URL和DATE字段
				// disease.setURL(rs.getString(4));
				// disease.setDATE(rs.getDate(5));
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			try {
				connection.close();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		return disease;
	}

	public Disease getMashupByName(String name) {
		Connection connection = DBHelper.getConnection();
		PreparedStatement statement = null;
		Disease disease = new Disease();

		try {
			// 使用PreparedStatement防止SQL注入
			String sql = "SELECT * FROM tb_disease WHERE C_NAME = ?";
			statement = connection.prepareStatement(sql);
			statement.setString(1, name);

			ResultSet rs = statement.executeQuery();

			if (rs.next()) {
				disease.setID(rs.getInt(1));
				disease.setNAME(rs.getString(2));
				disease.setDESCRIPTION(rs.getString(3));
				// 根据数据库实际结构，表中可能没有URL和DATE字段
				// disease.setURL(rs.getString(4));
				// disease.setDATE(rs.getDate(5));
			} else {
				disease.setID(-1);
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

		return disease.getID() != -1 ? disease : null;
	}
	
	/**
	 * 根据病种名称模糊搜索病种
	 * @param name 病种名称关键词
	 * @return 病种列表
	 */
	public ArrayList<Disease> getDiseaseByNameFuzzy(String name) {
		Connection connection = DBHelper.getConnection();
		PreparedStatement statement = null;
		ArrayList<Disease> diseases = new ArrayList<Disease>();

		try {
			// 使用PreparedStatement防止SQL注入
			String sql = "SELECT * FROM tb_disease WHERE C_NAME LIKE ?";
			statement = connection.prepareStatement(sql);
			statement.setString(1, "%" + name + "%");

			ResultSet rs = statement.executeQuery();

			while (rs.next()) {
				Disease disease = new Disease();
				disease.setID(rs.getInt(1));
				disease.setNAME(rs.getString(2));
				disease.setDESCRIPTION(rs.getString(3));
				// 根据数据库实际结构，表中可能没有URL和DATE字段
				// disease.setURL(rs.getString(4));
				// disease.setDATE(rs.getDate(5));
				diseases.add(disease);
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

		return diseases;
	}

	/**
	 * 根据API索引获取API ID
	 * @param index API索引
	 * @return API ID
	 */
	public int getApiIdByIndex(int index) {
		Connection connection = DBHelper.getConnection();
		Statement statement = null;
		int apiId = -1;

		try {
			statement = connection.createStatement();
			String sql = "select n_id from `tb_supply` order by n_id limit " + index + ",1";
			ResultSet rs = statement.executeQuery(sql);

			if (rs.next()) {
				apiId = rs.getInt(1);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			try {
				if (statement != null) statement.close();
				if (connection != null) connection.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}

		return apiId;
	}

	/**
	 * 根据耗材索引获取耗材 ID
	 * @param index 耗材索引
	 * @return 耗材 ID
	 */
	public int getSupplyIdByIndex(int index) {
		Connection connection = DBHelper.getConnection();
		Statement statement = null;
		int supplyId = -1;

		try {
			statement = connection.createStatement();
			String sql = "select n_id from `tb_supply` order by n_id limit " + index + ",1";
			ResultSet rs = statement.executeQuery(sql);

			if (rs.next()) {
				supplyId = rs.getInt(1);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			try {
				if (statement != null) statement.close();
				if (connection != null) connection.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}

		return supplyId;
	}

	/**
	 * 计算指定耗材在指定病种所有病案中的平均使用数量
	 * @param diseaseId 病种ID
	 * @param supplyName 耗材名称
	 * @return 平均使用数量
	 */
	public double getAverageSupplyUsage(int diseaseId, String supplyName) {
		Connection connection = DBHelper.getConnection();
		PreparedStatement statement = null;
		double averageUsage = 0.0;

		try {
			// 先获取该病种下的所有病案ID
			String caseIdsSql = "SELECT N_ID FROM tb_case WHERE N_MASHUP_ID = ?";
			PreparedStatement caseIdsStatement = connection.prepareStatement(caseIdsSql);
			caseIdsStatement.setInt(1, diseaseId);
			ResultSet caseIdsRs = caseIdsStatement.executeQuery();
			
			List<Integer> caseIds = new ArrayList<>();
			while (caseIdsRs.next()) {
				caseIds.add(caseIdsRs.getInt(1));
			}
			
			// 添加调试信息
			System.out.println("病种ID " + diseaseId + " 下的病案数量: " + caseIds.size());
			System.out.println("查询耗材: " + supplyName);
			
			if (!caseIds.isEmpty()) {
				// 构建病案ID列表
				StringBuilder caseIdList = new StringBuilder();
				boolean first = true;
				for (int caseId : caseIds) {
					if (!first) {
						caseIdList.append(",");
					}
					caseIdList.append(caseId);
					first = false;
				}
				
				// 查询这些病案中指定耗材的使用情况
				// 使用正确的字段名C_QUANTITY来统计耗材数量
				String usageSql = "SELECT SUM(C_QUANTITY) as total_usage FROM tb_supply WHERE N_CASE_ID IN (" + caseIdList.toString() + ") AND C_NAME = ?";
				statement = connection.prepareStatement(usageSql);
				statement.setString(1, supplyName);
				
				// 添加调试信息
				System.out.println("执行SQL: " + usageSql);
				System.out.println("参数: " + supplyName);
				
				ResultSet rs = statement.executeQuery();
				
				int totalUsage = 0;
				if (rs.next()) {
					totalUsage = rs.getInt("total_usage");
					// 处理NULL值情况
					if (rs.wasNull()) {
						totalUsage = 0;
						System.out.println("查询结果为NULL");
					} else {
						System.out.println("总使用量: " + totalUsage);
					}
				} else {
					System.out.println("没有查询到结果");
				}
				
				// 计算平均使用数量
				int caseCount = caseIds.size();
				if (caseCount > 0) {
					averageUsage = (double) totalUsage / caseCount;
					System.out.println("平均使用量: " + averageUsage);
				}
			}
			
			caseIdsStatement.close();
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			try {
				if (statement != null) statement.close();
				if (connection != null) connection.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		
		System.out.println("返回平均使用量: " + averageUsage);
		return averageUsage;
	}

	/**
	 * 计算特定病种中使用指定耗材的病案的平均数量
	 * @param diseaseId 病种ID
	 * @param supplyId 耗材ID
	 * @return 平均使用数量（取整）
	 */
	public int getAverageSupplyQuantityForDisease(int diseaseId, int supplyId) {
	    Connection connection = DBHelper.getConnection();
	    Statement statement = null;
	    int averageQuantity = 0;
	    
	    try {
	        statement = connection.createStatement();
	        
	        // 查询指定病种中使用指定耗材的所有记录，并计算平均数量
	        String sql = "SELECT AVG(C_QUANTITY) as avg_quantity FROM tb_supply WHERE N_CASE_ID IN " +
	                    "(SELECT N_ID FROM tb_case WHERE N_MASHUP_ID = " + diseaseId + ") " +
	                    "AND C_NAME = (SELECT C_NAME FROM tb_supply WHERE N_ID = " + supplyId + " LIMIT 1)";
	        
	        ResultSet rs = statement.executeQuery(sql);
	        
	        if (rs.next()) {
	            double avg = rs.getDouble("avg_quantity");
	            // 取整
	            averageQuantity = (int) Math.round(avg);
	        }
	        
	    } catch (SQLException e) {
	        e.printStackTrace();
	    } finally {
	        try {
	            if (statement != null) statement.close();
	            if (connection != null) connection.close();
	        } catch (SQLException e) {
	            e.printStackTrace();
	        }
	    }
	    
	    return averageQuantity;
	}

}