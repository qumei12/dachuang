package com.wangyan.servlets;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import dbhelper.DBSearch;
import javabean.API;
import javabean.Mashup;
import mian.Go;
import model.LDAModel;

/**
 * Servlet implementation class Recommand
 */
public class Recommand extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
	
	//private	ServletContext
	LDAModel ldaModel = null;
	int top_k = 3;
	int[][] recommand = null;
	
	Map<Integer, Integer> apiSequ = null;
	Map<Integer, List<String>> wordsBag = null;
	
    public Recommand() {
        super();
        // TODO Auto-generated constructor stub
        if(ldaModel == null){
        	ldaModel = new LDAModel(top_k);
			System.out.println("1 Initialize the model ...");
			ldaModel.initializeLDAModel();
			System.out.println("2 Learning and Saving the model ...");
			ldaModel.inferenceModel();
			System.out.println("LDAModel creation finished!");
			recommand = ldaModel.topKRecommand(top_k);
			//System.out.println("3 Output the final model ...");
			//ldaModel.saveIteratedModel(ldaModel.iterations);
		}
        
        if(apiSequ == null){
        	apiSequ = new DBSearch().getApiSequ();
        }
        
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		//response.getWriter().append("Served at: ").append(request.getContextPath());
		//this.getser
		String str = request.getParameter("idList");
		//String[] params_str = (String[]) obj_param;
		String[] params_str = str.split(",");
		
		int[] params = new int[params_str.length];
		
		for(int i = 0;i < params_str.length;i++){
			params[i] = Integer.parseInt(params_str[i]);
		}
		
		List<List<Integer>> resultList = new ArrayList<List<Integer>>();
		
		for(int i = 0;i < params.length;i++){
			int[] rec = recommand[i];
			List<List<Integer>> list = Go.mian(rec, top_k);

			System.out.println(list.size());
			System.out.println((int)(Math.random() * list.size()));
			System.out.println((int)(Math.random() * list.size()));
			System.out.println((int)(Math.random() * list.size()));
			Set<Integer> set = new HashSet<>();
			while (set.size() < 3) {
				set.add((int)(Math.random() * list.size()));
			}
			
			Iterator<Integer> iterator = set.iterator();
			
			while (iterator.hasNext()) {
				Integer integer = (Integer) iterator.next();
				int id = integer.intValue();
				resultList.add(list.get(id));
			}
		}
		
		
		List<List<API>> result = new ArrayList<>();
		
		for(int i = 0;i < resultList.size();i++){
			List<Integer> apiIdList = resultList.get(i);
			
			Set<Integer> apiIdSet = new HashSet<>();
			
			for(int j = 0;j < apiIdList.size();j++){
				apiIdSet.add(apiIdList.get(j));
			}
			
			List<API> apiList = new ArrayList<>();
			
			
			Iterator<Integer> iterator = apiIdSet.iterator();
			while (iterator.hasNext()) {
				Integer integer = (Integer) iterator.next();
				DBSearch dbs = new DBSearch();
				
				int id = apiSequ.get(integer.intValue());
				//System.out.println(id);
				API api = dbs.getApiById(id);
				apiList.add(api);
			}
			
			
			result.add(apiList);
		}
		
		
		request.setAttribute("result", result);
		request.getRequestDispatcher("/pages/recommendResult.jsp").forward(request, response);
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doGet(request, response);
	}

}
