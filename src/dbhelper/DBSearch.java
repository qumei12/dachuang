package dbhelper;

import java.sql.Connection;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.sql.PreparedStatement;

//import com.sun.org.apache.regexp.internal.recompile;

import fileout.FileOut;
import javabean.API;
import javabean.Mashup;
import javabean.Mashup_json;

public class DBSearch {

	public int getPageAmount() {
		Connection connection = DBHelper.getConnection();

		int MashupAmount = -1;

		Statement statement;

		try {
			statement = connection.createStatement();

			ResultSet rs = statement.executeQuery("select count(*) from `tb_mashup`;");

			while (rs.next()) {
				MashupAmount = rs.getInt(1);
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

		if (MashupAmount != -1) {
			pageAmount = MashupAmount / 20 + 1;
		}

		return pageAmount;
	}

	public ArrayList<Mashup_json> getMashupTable(int startId, int count) {
		Connection connection = DBHelper.getConnection();

		Statement statement;

		ArrayList<Mashup_json> mashupList = new ArrayList<Mashup_json>();

		try {
			statement = connection.createStatement();
			String sql = "select * from `tb_mashup` order by N_ID limit " + startId + "," + count + ";";
			//System.out.println(sql);
			ResultSet rs = statement.executeQuery(sql);

			while (rs.next()) {
				Mashup_json mashup = new Mashup_json();
				mashup.setN_ID(rs.getInt(1));
				mashup.setC_NAME(rs.getString(2));
				mashup.setC_DESCRIPTION(rs.getString(3));
				mashup.setC_URL(rs.getString(4));
				Date date = rs.getDate(5);
				String date_s = (date.getYear() + 1900) + "-" + (date.getMonth() + 1) + "-" + date.getDate();
				mashup.setC_DATE(date_s);

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

	public ArrayList<API> getDiseaseSupplyRelation(int diseaseId) {
		Connection connection = DBHelper.getConnection();

		Statement statement;

		ArrayList<API> list = new ArrayList<API>();

		try {
			statement = connection.createStatement();

			String sql = "select * from `tb_api` where n_mashup_id=" + diseaseId + " order by N_ID ";
			System.out.println("执行查询关联耗材: " + sql); // 添加调试信息

			ResultSet rs = statement.executeQuery(sql);

			while (rs.next()) {
				API api = new API();
				api.setN_ID(rs.getInt(1));
				api.setC_NAME(rs.getString(4));
				api.setC_DESCRIPTION(rs.getString(5));
				api.setC_URL(rs.getString(6));
				api.setN_MASHUP_ID(rs.getInt(2));
				api.setN_MASHUP_API_ID(rs.getInt(3));

				list.add(api);
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

			ResultSet rs = statement.executeQuery("select distinct(c_name), n_id from `tb_api` group by c_name order by n_id;");

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

	public API getSupplyById(int id) {
		Connection connection = DBHelper.getConnection();

		Statement statement = null;

		String sql = "select * from `tb_api` where n_id=" + id + ";";

		API api = new API();

		try {
			statement = connection.createStatement();

			ResultSet rs = statement.executeQuery(sql);

			while (rs.next()) {
				api.setN_ID(rs.getInt(1));
				api.setC_NAME(rs.getString(4));
				api.setC_DESCRIPTION(rs.getString(5));
				api.setC_URL(rs.getString(6));
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

		return api;
	}

	public Mashup getMashupById(int id) {
		Connection connection = DBHelper.getConnection();

		Statement statement = null;

		String sql = "select * from `tb_mashup` where n_id=" + id + ";";

		Mashup mashup = new Mashup();

		try {
			statement = connection.createStatement();

			ResultSet rs = statement.executeQuery(sql);

			while (rs.next()) {
				mashup.setN_ID(rs.getInt(1));
				mashup.setC_NAME(rs.getString(2));
				mashup.setC_DESCRIPTION(rs.getString(3));
				mashup.setC_URL(rs.getString(4));
				mashup.setD_DATE(rs.getDate(5));
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

		return mashup;
	}

	public Mashup getMashupByName(String name) {
		Connection connection = DBHelper.getConnection();
		PreparedStatement statement = null;
		Mashup mashup = new Mashup();

		try {
			// 使用PreparedStatement防止SQL注入
			String sql = "SELECT * FROM tb_mashup WHERE C_NAME = ?";
			statement = connection.prepareStatement(sql);
			statement.setString(1, name);

			ResultSet rs = statement.executeQuery();

			if (rs.next()) {
				mashup.setN_ID(rs.getInt(1));
				mashup.setC_NAME(rs.getString(2));
				mashup.setC_DESCRIPTION(rs.getString(3));
				mashup.setC_URL(rs.getString(4));
				mashup.setD_DATE(rs.getDate(5));
			} else {
				mashup.setN_ID(-1);
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

		return mashup.getN_ID() != -1 ? mashup : null;
	}
	
	/**
	 * 根据病种名称模糊搜索病种
	 * @param name 病种名称关键词
	 * @return 病种列表
	 */
	public ArrayList<Mashup> getDiseaseByNameFuzzy(String name) {
		Connection connection = DBHelper.getConnection();
		PreparedStatement statement = null;
		ArrayList<Mashup> diseases = new ArrayList<Mashup>();

		try {
			// 使用PreparedStatement防止SQL注入
			String sql = "SELECT * FROM tb_mashup WHERE C_NAME LIKE ?";
			statement = connection.prepareStatement(sql);
			statement.setString(1, "%" + name + "%");

			ResultSet rs = statement.executeQuery();

			while (rs.next()) {
				Mashup disease = new Mashup();
				disease.setN_ID(rs.getInt(1));
				disease.setC_NAME(rs.getString(2));
				disease.setC_DESCRIPTION(rs.getString(3));
				disease.setC_URL(rs.getString(4));
				disease.setD_DATE(rs.getDate(5));
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
			String sql = "select n_id from `tb_api` order by n_id limit " + index + ",1";
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
			String sql = "select n_id from `tb_api` order by n_id limit " + index + ",1";
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


}