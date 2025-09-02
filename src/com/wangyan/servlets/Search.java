package com.wangyan.servlets;

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

import javabean.API;
import javabean.Mashup;
import model.LDAModel;
import dbhelper.DBSearch;

/**
 * Servlet implementation class Search
 */
public class Search extends HttpServlet {
	private static final long serialVersionUID = 1L;

	// LDA算法模型
	LDAModel ldaModel = null;
	IndexUtil iu = null;

	Map<Integer, Integer> apiSequ = null;
	Map<Integer, List<Integer>> mashupWordsBag = null;
	Map<Integer, List<Integer>> apiWordsBag = null;

	Map<Integer, Integer> mashupIndex_ID = null;
	Map<Integer, String> mashupIndex_Name = null;

	Map<Integer, Integer> apiIndex_ID = null;
	Map<Integer, String> apiIndex_Name = null;

	// 添加预设topK配置（用于API推荐）
	private static final Map<String, Integer> MASHUP_TOPK_CONFIG = new HashMap<String, Integer>();

	// 静态初始化块
	static {
		MASHUP_TOPK_CONFIG.put("Bing Maps Mashup Tilt Shift", 5);
		MASHUP_TOPK_CONFIG.put("Twitter", 3);
		MASHUP_TOPK_CONFIG.put("Google Maps", 7);
		MASHUP_TOPK_CONFIG.put("YouTube", 4);
		MASHUP_TOPK_CONFIG.put("Flickr", 6);
		// 可以继续添加更多预设
	}

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

		iu = new IndexUtil();
		iu.createIndex(mashupIndex_Name);
	}

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String mashupName = request.getParameter("search");
		System.out.println("精确搜索mashup名称: " + mashupName);

		try {
			// 检查参数
			if (mashupName == null || mashupName.trim().isEmpty()) {
				request.setAttribute("error", "请输入mashup名称");
				request.getRequestDispatcher("error.jsp").forward(request, response);
				return;
			}

			// 首先验证mashup是否存在（精确匹配）
			DBSearch dbSearch = new DBSearch();
			Mashup mashup = dbSearch.getMashupByName(mashupName.trim());

			if (mashup == null || mashup.getN_ID() <= 0) {
				request.setAttribute("error", "未找到名称为 '" + mashupName + "' 的mashup");
				request.getRequestDispatcher("notFind.jsp").forward(request, response);
				return;
			}

			// 确定使用的topK值
			int dynamicTopK = determineTopKForSearch(mashupName);
			System.out.println("使用TopK值: " + dynamicTopK);

			// 使用LDA模型进行推荐
			List<API> apiList = new ArrayList<>();

			// 基于找到的mashup进行LDA推荐
			iu.query(mashupName, dynamicTopK, mashupIndex_Name, mashupIndex_ID,
					apiIndex_Name, apiIndex_ID, mashupWordsBag, apiWordsBag, apiList);

			// 设置请求属性
			request.setAttribute("mashup", mashup);  // 传递找到的mashup对象
			request.setAttribute("apiList", apiList);

			if(apiList.size() == 0){
				request.getRequestDispatcher("notFind.jsp").forward(request, response);
			} else {
				request.getRequestDispatcher("searchResult.jsp").forward(request, response);
			}

		} catch (Exception e) {
			e.printStackTrace();
			request.setAttribute("error", "系统错误: " + e.getMessage());
			request.getRequestDispatcher("error.jsp").forward(request, response);
		}
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doGet(request, response);
	}

	// 根据mashup名称确定topK值
	private int determineTopKForSearch(String mashupName) {
		if (mashupName == null || mashupName.trim().isEmpty()) {
			return DEFAULT_TOPK;
		}

		// 根据mashup名称匹配预设的topK值（精确匹配）
		for (Map.Entry<String, Integer> entry : MASHUP_TOPK_CONFIG.entrySet()) {
			if (mashupName.equalsIgnoreCase(entry.getKey())) {
				System.out.println("匹配到预设Mashup: " + entry.getKey() + " -> TopK: " + entry.getValue());
				return entry.getValue();
			}
		}

		// 如果没有匹配到预设值，返回默认值
		return DEFAULT_TOPK;
	}
}
