package com.wangyan.servlets;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.wangyan.index.APIMap;
import com.wangyan.index.IndexUtil;
import com.wangyan.index.MashupMap;

import filehelper.DeleteDirectory;
import javabean.API;
import javabean.Mashup;
import model.LDAModel;

/**
 * Servlet implementation class Search
 */
public class Search extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
	
	// LDA算法模型
	LDAModel ldaModel = null;
	// 物质扩散算法结果矩阵
	//double[][] apiRelation = null;
	IndexUtil iu = null;

	int top_k = 3;
	int[][] recommand = null;

	Map<Integer, Integer> apiSequ = null;
	Map<Integer, List<Integer>> mashupWordsBag = null;
	Map<Integer, List<Integer>> apiWordsBag = null;
	
	
	Map<Integer, Integer> mashupIndex_ID = null;
	Map<Integer, String> mashupIndex_Name = null;
	
	Map<Integer, Integer> apiIndex_ID = null;
	Map<Integer, String> apiIndex_Name = null;

	// 添加预设topK配置（用于API推荐）
	private static final Map<String, Integer> MASHUP_TOPK_CONFIG = new HashMap<String, Integer>() {{
		put("Facebook", 5);
		put("Twitter", 3);
		put("Google Maps", 7);
		put("YouTube", 4);
		put("Flickr", 6);
		// 可以继续添加更多预设
	}};

	// 默认topK值
	private static final int DEFAULT_TOPK = 3;

	/**
     * @see HttpServlet#HttpServlet()
     */
    public Search() {
        super();
        if (ldaModel == null) {
			ldaModel = new LDAModel();
			//System.out.println("1 Initialize the model ...");
			ldaModel.initializeLDAModel();
			//System.out.println("2 Learning and Saving the model ...");
			ldaModel.inferenceModel();
			//System.out.println("LDAModel creation finished!");
			mashupWordsBag = ldaModel.getMashupWordsBag();
			apiWordsBag = ldaModel.getAPIWordsBag();
			//System.out.println("词袋子生成完毕");
		}
        
        mashupIndex_ID = new HashMap<>();
		mashupIndex_Name = new HashMap<>();
		new MashupMap().setMap(mashupIndex_ID, mashupIndex_Name);
		
		apiIndex_ID = new HashMap<>();
		apiIndex_Name = new HashMap<>();
		new APIMap().setMap(apiIndex_ID, apiIndex_Name);
		
		//DeleteDirectory.deleteDir(new File("./index"));
		iu = new IndexUtil();
		iu.createIndex(mashupIndex_Name);
		
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		//response.getWriter().append("Served at: ").append(request.getContextPath());

		String keyword = request.getParameter("search");
		System.out.println("搜索关键词: " + keyword);
		List<Mashup> mashupList = new ArrayList<>();
		List<API> apiList = new ArrayList<>();
		// 根据搜索关键词确定使用的topK值
		int dynamicTopK = determineTopKForSearch(keyword);
		System.out.println("使用TopK值: " + dynamicTopK);

		iu.query(keyword, dynamicTopK, mashupIndex_Name, mashupIndex_ID, apiIndex_Name, apiIndex_ID, mashupWordsBag, apiWordsBag, mashupList, apiList);
	
		request.setAttribute("mashupList", mashupList);
		request.setAttribute("apiList", apiList);
		if(apiList.size() == 0 || mashupList.size() == 0){
			request.getRequestDispatcher("notFind.jsp").forward(request, response);
		} else {
			request.getRequestDispatcher("searchResult.jsp").forward(request, response);
		}
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		doGet(request, response);
	}
	// 根据搜索关键词确定topK值
	private int determineTopKForSearch(String keyword) {
		if (keyword == null || keyword.trim().isEmpty()) {
			return DEFAULT_TOPK;
		}

		// 根据关键词匹配预设的topK值
		for (Map.Entry<String, Integer> entry : MASHUP_TOPK_CONFIG.entrySet()) {
			if (keyword.toLowerCase().contains(entry.getKey().toLowerCase())) {
				System.out.println("匹配到预设Mashup: " + entry.getKey() + " -> TopK: " + entry.getValue());
				return entry.getValue();
			}
		}

		// 如果没有匹配到预设值，返回默认值
		return DEFAULT_TOPK;
	}
}
