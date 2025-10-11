package dbhelper;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javabean.Disease;
import javabean.Supply;

public class DBSearch {
	
	// 添加平均使用数量缓存，提高性能
	private static Map<String, Integer> averageQuantityCache = new HashMap<>();
	private static final int CACHE_MAX_SIZE = 1000; // 缓存最大条目数

	public ArrayList<Disease> getMashupTable(int startId, int count) {
		Connection connection = DBHelper.getConnection();

		Statement statement = null;

		ArrayList<Disease> mashupList = new ArrayList<Disease>();

		try {
			statement = connection.createStatement();

			ResultSet rs = statement
					.executeQuery("select * from `tb_disease` order by N_ID limit " + startId + "," + count + ";");

			while (rs.next()) {
				Disease disease = new Disease();
				disease.setID(rs.getInt(1));
				disease.setNAME(rs.getString(2));
				disease.setDESCRIPTION(rs.getString(3));
				// disease.setURL(rs.getString(4));
				// disease.setDATE(rs.getDate(5));

				mashupList.add(disease);
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

			String sql = "SELECT s.* FROM tb_supply s " +
					"JOIN tb_case c ON s.N_CASE_ID = c.N_ID " +
					"JOIN tb_disease d ON c.N_MASHUP_ID = d.N_ID " +
					"WHERE d.N_ID = " + diseaseId + " ORDER BY s.N_ID";
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
	
	/**
	 * 根据病种名称精确查找病种
	 * @param name 病种名称
	 * @return 病种对象，如果未找到则返回ID为-1的病种对象
	 */
	public Disease getDiseaseByName(String name) {
		Connection connection = DBHelper.getConnection();
		PreparedStatement statement = null;
		Disease disease = null;

		try {
			// 使用PreparedStatement防止SQL注入
			String sql = "SELECT * FROM tb_disease WHERE C_NAME = ?";
			statement = connection.prepareStatement(sql);
			statement.setString(1, name);

			ResultSet rs = statement.executeQuery();

			if (rs.next()) {
				disease = new Disease();
				disease.setID(rs.getInt(1));
				disease.setNAME(rs.getString(2));
				disease.setDESCRIPTION(rs.getString(3));
				// 根据数据库实际结构，表中可能没有URL和DATE字段
				// disease.setURL(rs.getString(4));
				// disease.setDATE(rs.getDate(5));
			} else {
				disease = new Disease();
				disease.setID(-1); // 设置为-1表示未找到
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

		return disease;
	}

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
	 * 计算特定病种中使用指定耗材的病案的平均数量
	 * @param diseaseId 病种ID
	 * @param supplyId 耗材ID
	 * @return 平均使用数量（取整）
	 */
	public int getAverageSupplyQuantityForDisease(int diseaseId, int supplyId) {
	    // 创建缓存键
	    String cacheKey = diseaseId + ":" + supplyId;
	    
	    // 检查缓存
	    if (averageQuantityCache.containsKey(cacheKey)) {
	        return averageQuantityCache.get(cacheKey);
	    }
	    
	    Connection connection = DBHelper.getConnection();
	    Statement statement = null;
	    int averageQuantity = 0;
	    
	    try {
	        statement = connection.createStatement();
	        
	        // 查询指定病种中使用指定耗材的病案数量和总使用量
	        // 根据规范，只统计使用了该耗材的病案（C_QUANTITY > 0）
	        String sql = "SELECT COUNT(*) as case_count, SUM(s.C_QUANTITY) as total_quantity FROM tb_supply s " +
	                    "INNER JOIN tb_case c ON s.N_CASE_ID = c.N_ID " +
	                    "WHERE c.N_MASHUP_ID = " + diseaseId + " " +
	                    "AND s.C_NAME = (SELECT C_NAME FROM tb_supply WHERE N_ID = " + supplyId + " LIMIT 1) " +
	                    "AND s.C_QUANTITY > 0";
	        
	        ResultSet rs = statement.executeQuery(sql);
	        
	        if (rs.next()) {
	            int caseCount = rs.getInt("case_count");
	            int totalQuantity = rs.getInt("total_quantity");
	            
	            // 处理NULL值情况
	            if (!rs.wasNull() && caseCount > 0) {
	                // 计算平均使用数量并取整
	                double avg = (double) totalQuantity / caseCount;
	                averageQuantity = (int) Math.round(avg);
	            }
	        }
	        
	        // 添加到缓存
	        if (averageQuantityCache.size() >= CACHE_MAX_SIZE) {
	            // 简单的缓存清理策略：清除第一个元素
	            averageQuantityCache.clear();
	        }
	        averageQuantityCache.put(cacheKey, averageQuantity);
	        
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

	/**
	 * 获取病种表中的总记录数，用于分页计算
	 * @return 总记录数
	 */
	public int getPageAmount() {
		Connection connection = DBHelper.getConnection();
		Statement statement = null;
		int count = 0;

		try {
			statement = connection.createStatement();
			ResultSet rs = statement.executeQuery("SELECT COUNT(*) FROM tb_disease");

			if (rs.next()) {
				count = rs.getInt(1);
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

		return count;
	}

}