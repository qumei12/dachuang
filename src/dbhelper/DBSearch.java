package dbhelper;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.math.BigDecimal;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javabean.Case;
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
				disease.setDrgPaymentStandard(rs.getString(4));
				// disease.setURL(rs.getString(5));
				// disease.setDATE(rs.getDate(6));

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
				// 正确映射字段，根据数据库表结构:
				// N_ID INT PRIMARY KEY - 耗材ID
				// N_CASE_ID INT NOT NULL - 关联的病例ID
				// C_NAME VARCHAR(255) NOT NULL - 耗材名称
				// C_PRODUCT_NAME VARCHAR(255) - 产品名称
				// C_SPECIFICATION VARCHAR(100) - 规格
				// C_PRICE DECIMAL(10,2) - 单价
				// C_QUANTITY INT - 数量
				
				supply.setID(rs.getInt("N_ID"));                    // 耗材ID
				supply.setNAME(rs.getString("C_NAME"));             // 耗材名称
				supply.setPRODUCT_NAME(rs.getString("C_PRODUCT_NAME")); // 产品名称
				supply.setDESCRIPTION(rs.getString("C_SPECIFICATION")); // 规格作为描述
				supply.setPRICE(rs.getString("C_PRICE"));           // 价格
				supply.setQUANTITY(rs.getInt("C_QUANTITY"));        // 数量
				supply.setURL(rs.getString("C_SPECIFICATION"));            // 规格
				// 注意：DISEASE_ID和SUPPLY_ID在当前上下文中没有直接对应字段
				// N_CASE_ID是关联病例ID，不是病种ID

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
				// 正确映射字段，根据数据库表结构:
				// N_ID INT PRIMARY KEY - 耗材ID
				// N_CASE_ID INT NOT NULL - 关联的病例ID
				// C_NAME VARCHAR(255) NOT NULL - 耗材名称
				// C_PRODUCT_NAME VARCHAR(255) - 产品名称
				// C_SPECIFICATION VARCHAR(100) - 规格
				// C_PRICE DECIMAL(10,2) - 单价
				// C_QUANTITY INT - 数量
				
				supply.setID(rs.getInt("N_ID"));                           // 耗材ID
				supply.setNAME(rs.getString("C_NAME"));                    // 耗材名称
				supply.setPRODUCT_NAME(rs.getString("C_PRODUCT_NAME"));    // 产品名称
				supply.setDESCRIPTION(rs.getString("C_SPECIFICATION"));    // 规格作为描述
				supply.setPRICE(rs.getString("C_PRICE"));                  // 价格
				supply.setQUANTITY(rs.getInt("C_QUANTITY"));               // 数量
				supply.setURL(rs.getString("C_SPECIFICATION"));            // 规格
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
				disease.setDrgPaymentStandard(rs.getString(4));
				// 根据数据库实际结构，表中可能没有URL和DATE字段
				// disease.setURL(rs.getString(5));
				// disease.setDATE(rs.getDate(6));
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
				disease.setDrgPaymentStandard(rs.getString(4));
				// 根据数据库实际结构，表中可能没有URL和DATE字段
				// disease.setURL(rs.getString(5));
				// disease.setDATE(rs.getDate(6));
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
				disease.setDrgPaymentStandard(rs.getString(4));
				// 根据数据库实际结构，表中可能没有URL和DATE字段
				// disease.setURL(rs.getString(5));
				// disease.setDATE(rs.getDate(6));
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
	
	/**
	 * 根据病种ID获取病例的DRG明细总金额平均值
	 * @param mashupId 病种ID
	 * @return DRG明细总金额平均值
	 */
	public BigDecimal getAverageDrgDetailTotalAmountByMashupId(int mashupId) {
		Connection connection = DBHelper.getConnection();
		PreparedStatement statement = null;
		BigDecimal averageAmount = BigDecimal.ZERO;
		
		try {
			String sql = "SELECT AVG(C_DRG_DETAIL_TOTAL_AMOUNT) as avg_amount FROM tb_case WHERE N_MASHUP_ID = ?";
			statement = connection.prepareStatement(sql);
			statement.setInt(1, mashupId);
			
			ResultSet rs = statement.executeQuery();
			
			if (rs.next()) {
				averageAmount = rs.getBigDecimal("avg_amount");
				if (averageAmount == null) {
					averageAmount = BigDecimal.ZERO;
				}
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
		
		return averageAmount;
	}
	
	/**
	 * 根据病种ID获取所有相关病例
	 * @param mashupId 病种ID
	 * @return 病例列表
	 */
	public List<Case> getCasesByMashupId(int mashupId) {
		Connection connection = DBHelper.getConnection();
		PreparedStatement statement = null;
		List<Case> cases = new ArrayList<>();
		
		try {
			String sql = "SELECT N_ID, N_MASHUP_ID, C_CASE_ID, C_DRG_DETAIL_TOTAL_AMOUNT FROM tb_case WHERE N_MASHUP_ID = ? ORDER BY C_DRG_DETAIL_TOTAL_AMOUNT";
			statement = connection.prepareStatement(sql);
			statement.setInt(1, mashupId);
			
			ResultSet rs = statement.executeQuery();
			
			while (rs.next()) {
				Case caseObj = new Case();
				caseObj.setId(rs.getInt("N_ID"));
				caseObj.setMashupId(rs.getInt("N_MASHUP_ID"));
				caseObj.setCaseId(rs.getString("C_CASE_ID"));
				caseObj.setDrgDetailTotalAmount(rs.getBigDecimal("C_DRG_DETAIL_TOTAL_AMOUNT"));
				cases.add(caseObj);
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
		
		return cases;
	}
	
	/**
	 * 根据病案ID获取该病案使用的所有耗材
	 * @param caseId 病案ID
	 * @return 耗材列表
	 */
	public List<Map<String, String>> getSuppliesByCaseId(int caseId) {
		Connection connection = DBHelper.getConnection();
		Statement statement = null;
		List<Map<String, String>> supplies = new ArrayList<>();
		
		try {
			statement = connection.createStatement();
			String sql = "SELECT N_ID, C_NAME, C_PRODUCT_NAME, C_SPECIFICATION, C_PRICE FROM tb_supply WHERE N_CASE_ID = " + caseId;
			ResultSet rs = statement.executeQuery(sql);
			
			while (rs.next()) {
				Map<String, String> supplyInfo = new HashMap<>();
				supplyInfo.put("productId", String.valueOf(rs.getInt("N_ID")));
				supplyInfo.put("productName", rs.getString("C_PRODUCT_NAME"));
				supplyInfo.put("specification", rs.getString("C_SPECIFICATION"));
				supplyInfo.put("unitPrice", rs.getString("C_PRICE"));
				supplies.add(supplyInfo);
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
		
		return supplies;
	}
}