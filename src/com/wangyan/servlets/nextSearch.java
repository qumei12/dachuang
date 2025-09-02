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
		// 设置请求和响应的字符编码
		request.setCharacterEncoding("UTF-8");
		response.setCharacterEncoding("UTF-8");
		response.setContentType("text/html;charset=UTF-8");
		String queryString = request.getQueryString();
		if (queryString != null && queryString.contains("%")) {
			// URL 中包含编码参数，确保正确解码
			request.getParameterMap(); // 触发参数解析
		}
		try {
			// 获取参数并进行验证
			String params = request.getParameter("id");
			System.out.println("接收到的参数: " + params);

			// 检查参数是否为空
			if (params == null || params.trim().isEmpty() || "null".equals(params)) {
				System.err.println("参数为空或不存在");
				request.setAttribute("error", "参数不能为空");
				request.getRequestDispatcher("error.jsp").forward(request, response);
				return;
			}

			// 将单个ID转换为数组格式以兼容现有代码
			String[] apis_str = new String[]{params};

			int[] apis_int = new int[apis_str.length];
			for (int i = 0; i < apis_str.length; i++){
				// 添加空值检查
				if (apis_str[i] == null || apis_str[i].trim().isEmpty() || "null".equals(apis_str[i])) {
					throw new NumberFormatException("参数格式错误: 包含空值或null");
				}
				try {
					apis_int[i] = Integer.parseInt(apis_str[i]);
				} catch (NumberFormatException e) {
					throw new NumberFormatException("参数格式错误: 无法解析为数字 - " + apis_str[i]);
				}
			}

			int startApiId = apis_int[apis_int.length - 1];

			// 检查映射是否初始化
			if (apiIndex_ID == null) {
				throw new ServletException("API索引映射未初始化");
			}

			Set<Integer> keySet = apiIndex_ID.keySet();
			Iterator<Integer> it = keySet.iterator();

			int startApiIndex = 0;
			boolean found = false;

			while (it.hasNext()) {
				Integer key = (Integer) it.next();
				// 添加空值检查
				if (key != null && apiIndex_ID.get(key) != null && apiIndex_ID.get(key) == startApiId) {
					startApiIndex = key;
					found = true;
					break;
				}
			}

			// 如果未找到对应的API索引
			if (!found) {
				System.err.println("未找到API索引，startApiId: " + startApiId);
				request.setAttribute("error", "未找到对应的API，ID: " + startApiId);
				request.getRequestDispatcher("error.jsp").forward(request, response);
				return;
			}

			int[] recommandApiIndex = null;
			recommandApiIndex = getTopMA(startApiIndex, top_k);

			List<Integer> recommandApiId = new ArrayList<>();

			for(int i = 0; i < recommandApiIndex.length; i++){
				// 添加空值检查
				if (apiIndex_ID != null && apiIndex_ID.get(recommandApiIndex[i]) != null) {
					int id = apiIndex_ID.get(recommandApiIndex[i]);
					recommandApiId.add(id);
				}
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

		} catch (NumberFormatException e) {
			System.err.println("数字格式异常: " + e.getMessage());
			e.printStackTrace();
			request.setAttribute("error", "参数格式错误，请提供有效的数字ID: " + e.getMessage());
			request.getRequestDispatcher("error.jsp").forward(request, response);
		} catch (Exception e) {
			System.err.println("系统异常: " + e.getMessage());
			e.printStackTrace();
			request.setAttribute("error", "系统错误: " + e.getMessage());
			request.getRequestDispatcher("error.jsp").forward(request, response);
		}
	}


	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
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
			// 添加边界检查
			if (j >= arr_index.length) {
				break;
			}
			if(arr_index[j] != index){
				recom[i] = arr_index[j];
				i++;
			}
		}

		return recom;
	}
}
