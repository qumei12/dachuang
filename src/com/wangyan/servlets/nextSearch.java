package com.wangyan.servlets;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Collections;
import java.util.Comparator;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.wangyan.index.APIMap;

import dbhelper.DBSearch;
import javabean.API;
import model.LDAModel;

/**
 * Servlet implementation class nextSearch
 */
public class nextSearch extends HttpServlet {
	private static final long serialVersionUID = 1L;
	
	private static LDAModel ldaModel = null; // 添加LDA模型实例

	double[][] apiRelation = null;

	int top_k = 3;

	Map<Integer, Integer> apiIndex_ID = null;
	Map<Integer, String> apiIndex_Name = null;

	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public nextSearch() {
		super();
		// 初始化LDA模型（只初始化一次）
		if (ldaModel == null) {
			ldaModel = new LDAModel();
			ldaModel.initializeLDAModel();
			ldaModel.inferenceModel();
		}
		
		// 注释掉对已删除的GetRelation类的使用
		/*
		if (apiRelation == null) {
			apiRelation = new GetRelation().getSimilarityByMD_REA();
		}
		*/

		apiIndex_ID = new HashMap<>();
		apiIndex_Name = new HashMap<>();
		new APIMap().setMap(apiIndex_ID, apiIndex_Name);
	}
	
	/**
	 * 基于原始Mashup的topK兴趣主题推荐API
	 * @param currentApiIndex 当前API索引
	 * @param top 推荐数量
	 * @param mashupIndex 原始Mashup索引
	 * @param topK 原始Mashup的兴趣主题数量
	 * @return 推荐的API索引数组
	 */
	private int[] getTopMAFromMashupInterests(int currentApiIndex, int top, int mashupIndex, int topK) {
		int[] recom = new int[top];
		
		// 获取原始Mashup的topK个兴趣主题
		Double[] interestProbs = new Double[ldaModel.getInterestAmount()];
		for (int k = 0; k < ldaModel.getInterestAmount(); k++) {
			interestProbs[k] = ldaModel.getTheta()[mashupIndex][k];
		}

		List<Integer> interestIndices = new ArrayList<>();
		for (int i = 0; i < ldaModel.getInterestAmount(); i++) {
			interestIndices.add(i);
		}

		// 按照概率排序
		interestIndices.sort((o1, o2) -> {
			if (interestProbs[o1] > interestProbs[o2]) return -1;
			else if (interestProbs[o1] < interestProbs[o2]) return 1;
			else return 0;
		});
		
		System.out.println("原始Mashup的Top " + topK + " 个兴趣主题:");
		for (int i = 0; i < Math.min(topK, ldaModel.getInterestAmount()); i++) {
			int interestId = interestIndices.get(i);
			System.out.println("  兴趣主题 " + interestId + " - 概率: " + String.format("%.6f", interestProbs[interestId]));
		}

		// 查找当前API属于哪个兴趣主题（在原始Mashup的兴趣主题中）
		int currentInterest = -1;
		for (int i = 0; i < Math.min(topK, ldaModel.getInterestAmount()); i++) {
			int interestId = interestIndices.get(i);
			if (ldaModel.getPhi()[interestId][currentApiIndex] > 0) {
				currentInterest = interestId;
				break;
			}
		}
		
		// 如果当前API不属于任何原始兴趣主题，则使用概率最高的主题
		if (currentInterest == -1 && !interestIndices.isEmpty()) {
			currentInterest = interestIndices.get(0);
		}
		
		System.out.println("当前API (索引: " + currentApiIndex + ") 属于兴趣主题: " + currentInterest);

		// 只在当前API所属的兴趣主题下查找其他API
		if (currentInterest >= 0) {
			double[] apiProbs = new double[ldaModel.getServiceAmount()];
			int[] apiIndices = new int[ldaModel.getServiceAmount()];
			
			for(int j = 0; j < ldaModel.getServiceAmount(); j++){
				apiIndices[j] = j;
				apiProbs[j] = ldaModel.getPhi()[currentInterest][j];
			}
			
			// 按概率降序排序
			for(int x = 0; x < apiProbs.length; x++){
				for(int y = 0; y < apiProbs.length - x - 1; y++){
					if(apiProbs[y] < apiProbs[y + 1]){
						double temp1 = apiProbs[y];
						apiProbs[y] = apiProbs[y + 1];
						apiProbs[y + 1] = temp1;

						int temp2 = apiIndices[y];
						apiIndices[y] = apiIndices[y + 1];
						apiIndices[y + 1] = temp2;
					}
				}
			}
			
			// 选择该兴趣主题下概率最高的API（排除当前API）
			int recomCount = 0;
			for(int j = 0; j < apiIndices.length && recomCount < top; j++){
				if(apiIndices[j] != currentApiIndex){
					recom[recomCount] = apiIndices[j];
					System.out.println("从兴趣主题 " + currentInterest + " 推荐API (索引: " + recom[recomCount] + ")");
					recomCount++;
				}
			}
			
			// 如果推荐数量不足，使用默认方法补充
			if (recomCount < top) {
				System.out.println("补充推荐数量: " + (top - recomCount));
				int[] defaultRecom = getTopMAByInterest(currentApiIndex, top - recomCount);
				for (int i = 0; i < defaultRecom.length && recomCount < top; i++) {
					recom[recomCount] = defaultRecom[i];
					recomCount++;
				}
			}
		} else {
			// 如果无法确定兴趣主题，使用默认方法
			System.out.println("无法确定当前API的兴趣主题，使用默认方法进行推荐");
			return getTopMAByInterest(currentApiIndex, top);
		}
		
		return recom;
	}
	
	/**
	 * 查找API在原始Mashup主要兴趣主题中的主题
	 * @param apiIndex API索引
	 * @param mashupIndex Mashup索引
	 * @return 主要兴趣主题索引
	 */
	private int findMainInterestForMashup(int apiIndex, int mashupIndex) {
		// 获取原始Mashup的topK个兴趣主题
		Double[] interestProbs = new Double[ldaModel.getInterestAmount()];
		for (int k = 0; k < ldaModel.getInterestAmount(); k++) {
			interestProbs[k] = ldaModel.getTheta()[mashupIndex][k];
		}

		List<Integer> interestIndices = new ArrayList<>();
		for (int i = 0; i < ldaModel.getInterestAmount(); i++) {
			interestIndices.add(i);
		}

		// 按照概率排序
		interestIndices.sort((o1, o2) -> {
			if (interestProbs[o1] > interestProbs[o2]) return -1;
			else if (interestProbs[o1] < interestProbs[o2]) return 1;
			else return 0;
		});
		
		// 查找当前API属于哪个兴趣主题（在原始Mashup的主要兴趣主题中）
		for (int i = 0; i < Math.min(5, ldaModel.getInterestAmount()); i++) { // 默认检查前5个兴趣主题
			int interestId = interestIndices.get(i);
			if (ldaModel.getPhi()[interestId][apiIndex] > 0) {
				return interestId;
			}
		}
		
		// 如果没找到，返回第一个主要兴趣主题
		if (!interestIndices.isEmpty()) {
			return interestIndices.get(0);
		}
		
		// 如果还是没有找到，返回默认方法的结果
		return findMainInterest(apiIndex);
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
			
			// 获取原始Mashup的topK兴趣主题（可选参数）
			String mashupIndexStr = request.getParameter("mashupIndex");
			String interestIdStr = request.getParameter("interestId");
			String topKStr = request.getParameter("topK");
			
			int mashupIndex = -1;
			int interestId = -1;
			int topK = top_k; // 默认值
			
			if (mashupIndexStr != null && !mashupIndexStr.isEmpty()) {
				try {
					mashupIndex = Integer.parseInt(mashupIndexStr);
				} catch (NumberFormatException e) {
					System.err.println("Mashup索引格式错误: " + mashupIndexStr);
				}
			}
			
			if (interestIdStr != null && !interestIdStr.isEmpty()) {
				try {
					interestId = Integer.parseInt(interestIdStr);
				} catch (NumberFormatException e) {
					System.err.println("兴趣主题ID格式错误: " + interestIdStr);
				}
			}
			
			if (topKStr != null && !topKStr.isEmpty()) {
				try {
					topK = Integer.parseInt(topKStr);
				} catch (NumberFormatException e) {
					System.err.println("TopK值格式错误: " + topKStr);
				}
			}

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

			// 使用基于同一兴趣的推荐方法（关键修改）
			int[] recommandApiIndex;
			int mainInterest = -1;
			
			if (mashupIndex >= 0 && interestId >= 0) {
				// 使用传递的兴趣主题ID进行推荐
				recommandApiIndex = getTopMABySpecificInterest(startApiIndex, top_k, interestId);
				mainInterest = interestId;
				System.out.println("使用传递的兴趣主题 (ID: " + interestId + ") 进行推荐");
			} else if (mashupIndex >= 0) {
				// 使用原始Mashup的topK兴趣主题进行推荐
				recommandApiIndex = getTopMAFromMashupInterests(startApiIndex, top_k, mashupIndex, topK);
				// 获取当前API在原始Mashup主要兴趣主题中的主题
				mainInterest = findMainInterestForMashup(startApiIndex, mashupIndex);
				System.out.println("使用原始Mashup (索引: " + mashupIndex + ") 的兴趣主题进行推荐");
			} else {
				// 如果没有原始Mashup信息，则使用默认方法
				recommandApiIndex = getTopMAByInterest(startApiIndex, top_k);
				mainInterest = findMainInterest(startApiIndex);
				System.out.println("使用默认方法进行推荐");
			}

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

			System.out.println("当前API (ID: " + startApiId + ") 属于兴趣主题: " + mainInterest);
			
			for(int i = 0; i < recommandApiId.size(); i++){
				API api = dbSearch.getApiById(recommandApiId.get(i));
				recommandResult.add(api);
				System.out.println("推荐的API (ID: " + recommandApiId.get(i) + ") 属于兴趣主题: " + mainInterest);
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

	/**
	 * 基于指定兴趣主题的API推荐方法
	 * @param index 当前API索引
	 * @param top 推荐数量
	 * @param interestId 指定的兴趣主题ID
	 * @return 推荐的API索引数组
	 */
	private int[] getTopMABySpecificInterest(int index, int top, int interestId) {
		int[] recom = new int[top];
		
		System.out.println("当前API索引 " + index + " 属于指定兴趣主题: " + interestId);
		
		// 在指定兴趣主题下找到其他高概率API
		double[] arr = new double[ldaModel.getServiceAmount()];
		int[] arr_index = new int[ldaModel.getServiceAmount()];
		
		for(int i = 0; i < ldaModel.getServiceAmount(); i++){
			arr_index[i] = i;
			arr[i] = ldaModel.getPhi()[interestId][i];
		}
		
		// 按概率降序排序
		for(int i = 0; i < arr.length; i++){
			for(int j = 0; j < arr.length - i - 1; j++){
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
		
		// 选择指定兴趣主题下概率最高的API（排除当前API）
		for(int i = 0, j = 0; i < top; j++){
			if (j >= arr_index.length) {
				break;
			}
			if(arr_index[j] != index){
				recom[i] = arr_index[j];
				System.out.println("推荐的API (索引: " + recom[i] + ") 属于兴趣主题: " + interestId);
				i++;
			}
		}
		
		return recom;
	}

	/**
	 * 基于相同兴趣主题的API推荐方法
	 * @param index 当前API索引
	 * @param top 推荐数量
	 * @return 推荐的API索引数组
	 */
	private int[] getTopMAByInterest(int index, int top) {
		int[] recom = new int[top];
		
		// 找到当前API最可能的兴趣主题
		int mainInterest = findMainInterest(index);
		System.out.println("当前API索引 " + index + " 属于兴趣主题: " + mainInterest);
		
		// 在同一兴趣主题下找到其他高概率API
		double[] arr = new double[ldaModel.getServiceAmount()];
		int[] arr_index = new int[ldaModel.getServiceAmount()];
		
		for(int i = 0; i < ldaModel.getServiceAmount(); i++){
			arr_index[i] = i;
			arr[i] = ldaModel.getPhi()[mainInterest][i];
		}
		
		// 按概率降序排序
		for(int i = 0; i < arr.length; i++){
			for(int j = 0; j < arr.length - i - 1; j++){
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
		
		// 选择同一兴趣主题下概率最高的API（排除当前API）
		for(int i = 0, j = 0; i < top; j++){
			if (j >= arr_index.length) {
				break;
			}
			if(arr_index[j] != index){
				recom[i] = arr_index[j];
				System.out.println("推荐的API (索引: " + recom[i] + ") 属于兴趣主题: " + mainInterest);
				i++;
			}
		}
		
		return recom;
	}

	/**
	 * 查找指定API索引的主要兴趣主题
	 * @param index API索引
	 * @return 主要兴趣主题索引
	 */
	private int findMainInterest(int index) {
		int mainInterest = -1;
		double maxProb = -1;
		
		// 遍历所有兴趣主题，找到当前API概率最高的主题
		for (int k = 0; k < ldaModel.getInterestAmount(); k++) {
			if (ldaModel.getPhi()[k][index] > maxProb) {
				maxProb = ldaModel.getPhi()[k][index];
				mainInterest = k;
			}
		}
		
		return mainInterest;
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