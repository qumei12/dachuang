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

//import com.sun.org.apache.regexp.internal.recompile;

import fileout.FileOut;
import javabean.API;
import javabean.Mashup;
import javabean.Mashup_json;

public class DBSearch {
	
	public int getPageAmount(){
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
		
		if(MashupAmount != -1){
			pageAmount = MashupAmount / 20  + 1;
		}
		
		return pageAmount;
	}
	
	public ArrayList<Mashup_json> getMashupTable(int startId, int count){
		Connection connection = DBHelper.getConnection();
		
		Statement statement;
		
		ArrayList<Mashup_json> mashupList = new ArrayList<Mashup_json>();
		
		try {
			statement = connection.createStatement();
			String sql = "select * from `tb_mashup` order by N_ID limit " + startId + "," + count + ";";
			//System.out.println(sql);
			ResultSet rs = statement.executeQuery(sql);
		
			while(rs.next()){
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
	
	public ArrayList<API> getMashupApiRelation(int mashupId){
		Connection connection = DBHelper.getConnection();
		
		Statement statement;
		
		ArrayList<API> list = new ArrayList<API>();
		
		try {
			statement = connection.createStatement();
			
			String sql = "select * from `tb_api` where n_mashup_id=" + mashupId + " order by N_ID ";
		
			ResultSet rs = statement.executeQuery(sql);
			
			while(rs.next()){
				API api = new API();
				api.setN_ID(rs.getInt(1));
				api.setC_NAME(rs.getString(4));
				api.setC_DESCRIPTION(rs.getString(5));
				api.setC_URL(rs.getString(6));
				api.setN_MASHUP_ID(rs.getInt(2));
				api.setN_MASHUP_API_ID(rs.getInt(3));
				
				list.add(api);
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
		
		return list;
	}
	
	public Map<Integer, Integer> getApiSequ(){
		Connection connection = DBHelper.getConnection();
		
		Statement statement = null;
		
		Map<Integer, Integer> map = new HashMap<Integer, Integer>();
		
		
		
		try {
			statement = connection.createStatement();
			
			ResultSet rs = statement.executeQuery("select distinct(c_name), n_id from `tb_api` group by c_name order by n_id;");
			
			int order = 0;
			
			while(rs.next()){
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
	
	public API getApiById(int id){
		Connection connection = DBHelper.getConnection();
		
		Statement statement = null;
		
		String sql = "select * from `tb_api` where n_id=" + id + ";";
		
		API api = new API();
		
		try {
			statement = connection.createStatement();
			
			ResultSet rs = statement.executeQuery(sql);
			
			while(rs.next()){
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
	
	public Mashup getMashupById(int id){
		Connection connection = DBHelper.getConnection();
		
		Statement statement = null;
		
		String sql = "select * from `tb_mashup` where n_id=" + id + ";";
		
		Mashup mashup = new Mashup();
		
		try {
			statement = connection.createStatement();
			
			ResultSet rs = statement.executeQuery(sql);
			
			while(rs.next()){
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
}
