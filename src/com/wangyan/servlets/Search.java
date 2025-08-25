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
       
	
	// LDA�㷨ģ��
	LDAModel ldaModel = null;
	// ������ɢ�㷨�������
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

	// ���Ԥ��topK���ã�����API�Ƽ���
	private static final Map<String, Integer> MASHUP_TOPK_CONFIG = new HashMap<String, Integer>() {{
		put("Facebook", 5);
		put("Twitter", 3);
		put("Google Maps", 7);
		put("YouTube", 4);
		put("Flickr", 6);
		// ���Լ�����Ӹ���Ԥ��
	}};

	// Ĭ��topKֵ
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
			//System.out.println("�ʴ����������");
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
		System.out.println("�����ؼ���: " + keyword);
		List<Mashup> mashupList = new ArrayList<>();
		List<API> apiList = new ArrayList<>();
		// ���������ؼ���ȷ��ʹ�õ�topKֵ
		int dynamicTopK = determineTopKForSearch(keyword);
		System.out.println("ʹ��TopKֵ: " + dynamicTopK);

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
	// ���������ؼ���ȷ��topKֵ
	private int determineTopKForSearch(String keyword) {
		if (keyword == null || keyword.trim().isEmpty()) {
			return DEFAULT_TOPK;
		}

		// ���ݹؼ���ƥ��Ԥ���topKֵ
		for (Map.Entry<String, Integer> entry : MASHUP_TOPK_CONFIG.entrySet()) {
			if (keyword.toLowerCase().contains(entry.getKey().toLowerCase())) {
				System.out.println("ƥ�䵽Ԥ��Mashup: " + entry.getKey() + " -> TopK: " + entry.getValue());
				return entry.getValue();
			}
		}

		// ���û��ƥ�䵽Ԥ��ֵ������Ĭ��ֵ
		return DEFAULT_TOPK;
	}
}
