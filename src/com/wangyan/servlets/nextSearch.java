package com.wangyan.servlets;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.wangyan.index.APIMap;

import allthings.GetRelation;
import dbhelper.DBSearch;
import javabean.API;
import jdk.internal.org.objectweb.asm.tree.IntInsnNode;

/**
 * Servlet implementation class nextSearch
 */
public class nextSearch extends HttpServlet {
	private static final long serialVersionUID = 1L;

	double[][] apiRelation = null;
	
	int top_k = 3;
	
	Map<Integer, Integer> apiIndex_ID = null;
	Map<Integer, String> apiIndex_Name = null;

	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public nextSearch() {
		super();
		if (apiRelation == null) {
			apiRelation = new GetRelation().getSimilarityByMD_REA();
		}
		
		apiIndex_ID = new HashMap<>();
		apiIndex_Name = new HashMap<>();
		new APIMap().setMap(apiIndex_ID, apiIndex_Name);
	}

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		// TODO Auto-generated method stub
		// response.getWriter().append("Served at:
		// ").append(request.getContextPath());
		String params = request.getParameter("idList");
		System.out.println(params);
		String[] apis_str = params.split(",");
		
		int[] apis_int = new int[apis_str.length];
		for (int i = 0; i < apis_str.length; i++){
			apis_int[i] = Integer.parseInt(apis_str[i]);
		}
		
		int startApiId = apis_int[apis_int.length - 1];
		
		Set<Integer> keySet = apiIndex_ID.keySet();
		Iterator<Integer> it = keySet.iterator();
		
		int startApiIndex = 0;
		
		while (it.hasNext()) {
			Integer key = (Integer) it.next();
			if (apiIndex_ID.get(key) == startApiId) {
				startApiIndex = key;
				break;
			}
		}
		
		int[] recommandApiIndex = null;
		
		recommandApiIndex = getTopMA(startApiIndex, top_k);
		
		List<Integer> recommandApiId = new ArrayList<>();
		
		for(int i = 0; i < recommandApiIndex.length; i++){
			int id = apiIndex_ID.get(recommandApiIndex[i]);
			recommandApiId.add(id);
			//System.out.println( apiIndex_Name.get(recommandApiIndex[i]));
		}
		
		List<API> recommandResult = new ArrayList<>();
		DBSearch dbSearch = new DBSearch();
		
		for(int i = 0; i < recommandApiId.size(); i++){
			API api = dbSearch.getApiById(recommandApiId.get(i));
			recommandResult.add(api);
		}
		
		List<API> recommandChain = new ArrayList<>();
		
		for(int i = 0; i < apis_int.length; i++){
			API api = dbSearch.getApiById(apis_int[i]);
			recommandChain.add(api);
		}
		
		request.setAttribute("result", recommandResult);
		request.setAttribute("chain", recommandChain);
		
		request.getRequestDispatcher("nextSearchResult2.jsp").forward(request, response);
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		// TODO Auto-generated method stub
		doGet(request, response);
	}

	
	private int[] getTopMA(int index, int top){
		int[] recom = new int[top];
		
		double[] arr = new double[apiRelation[index].length];
		int[] arr_index = new int[apiRelation[index].length];
		
		for(int i = 0; i < apiRelation[index].length; i++){
			arr_index[i] = i;
			arr[i] = apiRelation[index][i];
		}
		
		for(int i = 0;i < arr.length;i++){
			for(int j = 0; j < arr.length - i - 1;j++){
				if(arr[j] < arr[j + 1]){
					double temp1 = arr[j];
					arr[j] = arr[j + 1];
					arr[j + 1] = temp1;
					
					int temp2 = arr_index[j];
					arr_index[j] = arr_index[j + 1];
					arr_index[j + 1] = temp2;
				}
			}
		}
		
		for(int i = 0, j = 0; i < top ;j++){
			if(arr_index[j] != index){
				recom[i] = arr_index[j];
				i++;
			}
		}
		
		return recom;
		
	}
}
