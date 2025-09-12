package com.wangyan.business;

import java.util.ArrayList;

import dbhelper.DBSearch;
import javabean.API;
import javabean.Mashup;
import javabean.Mashup_json;

public class Search {
	public static int searchPageAmount(){
		DBSearch dbs = new DBSearch();
		return dbs.getPageAmount();
	}
	
	public static ArrayList<Mashup_json> searchMashupTable(int page){
		
		int count = 20;//每次返回10个数据
		int startId = (page - 1) * 20;
		//System.out.println(startId + "***");
		DBSearch dbs = new DBSearch();
		return dbs.getMashupTable(startId, count);
		
	}
	
	public static ArrayList<API> searchMashupApiRelation(int mashupId){
		DBSearch dbs = new DBSearch();
		ArrayList<API> list = dbs.getDiseaseSupplyRelation(mashupId);
		return list;
	}
}