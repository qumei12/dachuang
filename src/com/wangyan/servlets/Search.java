package com.wangyan.servlets;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Collections;
import java.util.Comparator;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

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

	Map<Integer, Integer> supplySequ = null;
	Map<Integer, List<Integer>> diseaseWordsBag = null;
	Map<Integer, List<Integer>> supplyWordsBag = null;

	Map<Integer, Integer> diseaseIndex_ID = null;
	Map<Integer, String> diseaseIndex_Name = null;

	Map<Integer, Integer> supplyIndex_ID = null;
	Map<Integer, String> supplyIndex_Name = null;

	// 添加预设topK配置（用于耗材推荐）
	private static final Map<String, Integer> DISEASE_TOPK_CONFIG = new HashMap<String, Integer>();

	// 静态初始化块
	static {
		DISEASE_TOPK_CONFIG.put("Bing Maps Mashup Tilt Shift", 5);
		DISEASE_TOPK_CONFIG.put("Twitter", 3);
		DISEASE_TOPK_CONFIG.put("Google Maps", 7);
		DISEASE_TOPK_CONFIG.put("YouTube", 4);
		DISEASE_TOPK_CONFIG.put("Flickr", 6);
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
			diseaseWordsBag = ldaModel.getMashupWordsBag();
			supplyWordsBag = ldaModel.getAPIWordsBag();
			//System.out.println("词袋子生成完毕");
		}

		diseaseIndex_ID = new HashMap<>();
		diseaseIndex_Name = new HashMap<>();
		new MashupMap().setMap(diseaseIndex_ID, diseaseIndex_Name);

		supplyIndex_ID = new HashMap<>();
		supplyIndex_Name = new HashMap<>();
		new APIMap().setMap(supplyIndex_ID, supplyIndex_Name);

		iu = new IndexUtil();
		iu.createIndex(diseaseIndex_Name);
	}

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String diseaseName = request.getParameter("search");
		String replaceSupplyIdStr = request.getParameter("replaceSupplyId");
		String positionStr = request.getParameter("position");
		
		// 检查是否是替换耗材的请求
		if (replaceSupplyIdStr != null && !replaceSupplyIdStr.isEmpty()) {
			handleReplaceSupply(request, response, replaceSupplyIdStr, positionStr);
			return;
		}
		
		System.out.println("精确搜索病种名称: " + diseaseName);
		
		try {
			// 检查参数
			if (diseaseName == null || diseaseName.trim().isEmpty()) {
				request.setAttribute("error", "请输入病种名称");
				request.getRequestDispatcher("error.jsp").forward(request, response);
				return;
			}

			// 首先验证病种是否存在（精确匹配）
			DBSearch dbs = new DBSearch();
			Mashup disease = dbs.getMashupByName(diseaseName);
			
			if (disease == null) {
				// 如果精确匹配失败，尝试模糊搜索
				System.out.println("未找到精确匹配的病种，尝试模糊搜索...");
				request.getRequestDispatcher("notFind.jsp").forward(request, response);
				return;
			}
			
			// 获取病种索引
			Integer diseaseIndex = null;
			for (Map.Entry<Integer, String> entry : diseaseIndex_Name.entrySet()) {
				if (entry.getValue().equals(diseaseName)) {
					diseaseIndex = entry.getKey();
					break;
				}
			}
			
			if (diseaseIndex == null) {
				request.setAttribute("error", "未找到病种索引");
				request.getRequestDispatcher("error.jsp").forward(request, response);
				return;
			}
			
			// 使用LDA模型为病种推荐耗材，而不是直接从数据库获取关联耗材
			// 获取该病种的预设topK值
			int dynamicTopK = DISEASE_TOPK_CONFIG.getOrDefault(diseaseName, DEFAULT_TOPK);
			
			// 使用IndexUtil的推荐方法进行推荐（使用LDA模型）
			List<API> recommandSupplyList = iu.recommendOneAPIPerInterestByLDAModel(
				diseaseIndex, 
				dynamicTopK,
				ldaModel,
				supplyIndex_ID
			);
			
			// 如果没有推荐到耗材
			if (recommandSupplyList == null || recommandSupplyList.isEmpty()) {
				request.setAttribute("disease", disease);
				request.setAttribute("supplyList", new ArrayList<API>());
				request.setAttribute("diseaseIndex", diseaseIndex);
				request.getRequestDispatcher("searchResult.jsp").forward(request, response);
				return;
			}
			
			// 将推荐结果转换为ArrayList
			ArrayList<API> limitedSupplyList = new ArrayList<>(recommandSupplyList);
			
			// 创建耗材索引到兴趣主题的映射
			Map<Integer, Integer> supplyToInterestMap = new HashMap<>();
			
			// 获取当前病种的topK兴趣主题
			Double[] interestProbs = new Double[ldaModel.getInterestAmount()];
			for (int k = 0; k < ldaModel.getInterestAmount(); k++) {
				interestProbs[k] = ldaModel.getTheta()[diseaseIndex][k];
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
			
			System.out.println("病种 \"" + diseaseName + "\" 的Top " + dynamicTopK + " 个兴趣主题:");
			for (int i = 0; i < Math.min(dynamicTopK, ldaModel.getInterestAmount()); i++) {
				int interestId = interestIndices.get(i);
				System.out.println("  兴趣主题 " + interestId + " - 概率: " + String.format("%.6f", interestProbs[interestId]));
			}
			
			// 为每个推荐的耗材确定其主要兴趣主题
			for (API supply : limitedSupplyList) {
				// 通过supplyIndex_ID查找耗材索引
				Integer supplyIndex = null;
				for (Map.Entry<Integer, Integer> entry : supplyIndex_ID.entrySet()) {
					if (entry.getValue().equals(supply.getN_ID())) {
						supplyIndex = entry.getKey();
						break;
					}
				}
				
				if (supplyIndex != null) {
					// 在病种的topK兴趣主题中查找该耗材所属的主要兴趣主题
					int mainInterestId = -1;
					double maxProb = -1;
					
					for (int i = 0; i < Math.min(dynamicTopK, ldaModel.getInterestAmount()); i++) {
						int interestId = interestIndices.get(i);
						if (supplyIndex < ldaModel.getPhi()[interestId].length && 
							ldaModel.getPhi()[interestId][supplyIndex] > maxProb) {
							maxProb = ldaModel.getPhi()[interestId][supplyIndex];
							mainInterestId = interestId;
						}
					}
					
					if (mainInterestId != -1) {
						supplyToInterestMap.put(supplyIndex, mainInterestId);
						System.out.println("  耗材 \"" + supply.getC_NAME() + "\" 属于兴趣主题 " + mainInterestId);
					}
				}
			}
			
			// 传递数据到JSP页面
			request.setAttribute("disease", disease);
			request.setAttribute("supplyList", limitedSupplyList);
			request.setAttribute("diseaseIndex", diseaseIndex);
			request.setAttribute("supplyToInterestMap", supplyToInterestMap);
			request.setAttribute("diseaseIndex_ID", diseaseIndex_ID);
			request.setAttribute("diseaseIndex_Name", diseaseIndex_Name);
			request.setAttribute("supplyIndex_ID", supplyIndex_ID);
			request.setAttribute("supplyIndex_Name", supplyIndex_Name);
			
			// 保存数据到会话，供后续替换使用
			HttpSession session = request.getSession();
			
			// 保存疾病信息
			session.setAttribute("disease", disease);
			
			// 保存原始推荐耗材列表（不可变副本）
			session.setAttribute("originalSupplyList", new ArrayList<>(limitedSupplyList));
			
			// 保存疾病索引
			session.setAttribute("diseaseIndex", diseaseIndex);
			
			// 保存耗材到兴趣主题的映射（不可变副本）
			session.setAttribute("supplyToInterestMap", new HashMap<>(supplyToInterestMap));
			
			// 记录调试信息
			System.out.println("初始搜索时保存原始供应列表到会话");
			System.out.println("原始供应列表大小: " + limitedSupplyList.size());
			
			request.getRequestDispatcher("searchResult.jsp").forward(request, response);
			
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
		request.setCharacterEncoding("UTF-8");
		String diseaseName = request.getParameter("search");
		
		System.out.println("执行病种搜索: " + diseaseName);
		try {
			if(diseaseName == null || diseaseName.trim().isEmpty()) {
				request.setAttribute("error", "请输入搜索关键词");
				request.getRequestDispatcher("error.jsp").forward(request, response);
				return;
			}
			
			// 首先尝试精确匹配
			DBSearch dbs = new DBSearch();
			Mashup disease = dbs.getMashupByName(diseaseName);
			
			if (disease != null && disease.getN_ID() != -1) {
				// 精确匹配成功，重定向到 doGet 方法处理
				String redirectURL = "./Search?search=" + java.net.URLEncoder.encode(diseaseName, "UTF-8");
				response.sendRedirect(redirectURL);
				return;
			}
			
			// 如果精确匹配失败，执行模糊搜索
			System.out.println("精确匹配失败，执行病种名称模糊搜索: " + diseaseName);
			ArrayList<Mashup> diseases = dbs.getDiseaseByNameFuzzy(diseaseName);
			
			if(diseases == null || diseases.isEmpty()) {
				System.out.println("未检索到相关病种");
				request.getRequestDispatcher("notFind.jsp").forward(request, response);
				return;
			}
			
			// 传递搜索结果到JSP页面
			request.setAttribute("diseases", diseases);
			request.setAttribute("keyword", diseaseName);
			request.getRequestDispatcher("searchResult.jsp").forward(request, response);
			
		} catch(Exception e) {
			e.printStackTrace();
			request.setAttribute("error", "系统错误: " + e.getMessage());
			request.getRequestDispatcher("error.jsp").forward(request, response);
		}
	}
	
	/**
	 * 处理替换耗材的请求
	 * @param request
	 * @param response
	 * @param replaceSupplyIdStr 要替换的耗材ID
	 * @param positionStr 要替换的位置
	 * @throws ServletException
	 * @throws IOException
	 */
	private void handleReplaceSupply(HttpServletRequest request, HttpServletResponse response, 
			String replaceSupplyIdStr, String positionStr) 
			throws ServletException, IOException {
		try {
			// 获取要替换的耗材
			int replaceSupplyId = Integer.parseInt(replaceSupplyIdStr);
			int position = 0; // 默认替换第一个位置
			if (positionStr != null && !positionStr.isEmpty()) {
				position = Integer.parseInt(positionStr);
			}
			
			DBSearch dbs = new DBSearch();
			API newSupply = dbs.getSupplyById(replaceSupplyId);
			
			// 创建包含所有原始耗材的supplyList，并替换指定位置的耗材
			ArrayList<API> supplyList = new ArrayList<>();
			
			// 从会话中获取原始的supplyList
			HttpSession session = request.getSession();
			@SuppressWarnings("unchecked")
			ArrayList<API> originalSupplyList = (ArrayList<API>) session.getAttribute("originalSupplyList");
			
			if (originalSupplyList != null && !originalSupplyList.isEmpty()) {
				// 复制原始列表
				for (int i = 0; i < originalSupplyList.size(); i++) {
					if (i == position) {
						// 替换指定位置的耗材
						supplyList.add(newSupply);
					} else {
						// 保留其他位置的耗材
						supplyList.add(originalSupplyList.get(i));
					}
				}
				
				// 如果位置超出原始列表范围，则添加到末尾
				if (position >= originalSupplyList.size()) {
					supplyList.add(newSupply);
				}
			} else {
				// 如果没有原始列表，则只显示新耗材
				supplyList.add(newSupply);
			}
			
			// 设置必要的属性并转发到searchResult.jsp
			request.setAttribute("supplyList", supplyList);
			request.setAttribute("supplyIndex_ID", supplyIndex_ID);
			request.setAttribute("supplyIndex_Name", supplyIndex_Name);
			
			// 设置其他必要属性（从会话中获取或使用默认值）
			request.setAttribute("disease", session.getAttribute("disease"));
			Integer diseaseIndex = (Integer) session.getAttribute("diseaseIndex");
			request.setAttribute("diseaseIndex", diseaseIndex != null ? diseaseIndex : -1);
			@SuppressWarnings("unchecked")
			Map<Integer, Integer> supplyToInterestMap = (Map<Integer, Integer>) session.getAttribute("supplyToInterestMap");
			request.setAttribute("supplyToInterestMap", supplyToInterestMap != null ? supplyToInterestMap : new HashMap<>());
			request.setAttribute("diseaseIndex_ID", diseaseIndex_ID);
			request.setAttribute("diseaseIndex_Name", diseaseIndex_Name);
			
			request.getRequestDispatcher("searchResult.jsp").forward(request, response);
		} catch (Exception e) {
			e.printStackTrace();
			request.setAttribute("error", "系统错误: " + e.getMessage());
			request.getRequestDispatcher("error.jsp").forward(request, response);
		}
	}
	
	/**
	 * 处理替换耗材的请求
	 * @param request
	 * @param response
	 * @param replaceSupplyIdStr 要替换的耗材ID
	 * @throws ServletException
	 * @throws IOException
	 */
	private void handleReplaceSupply(HttpServletRequest request, HttpServletResponse response, 
			String replaceSupplyIdStr) 
			throws ServletException, IOException {
		try {
			// 获取要替换的耗材
			int replaceSupplyId = Integer.parseInt(replaceSupplyIdStr);
			
			DBSearch dbs = new DBSearch();
			API newSupply = dbs.getSupplyById(replaceSupplyId);
			
			// 从会话中获取原始的supplyList
			HttpSession session = request.getSession();
			@SuppressWarnings("unchecked")
			ArrayList<API> originalSupplyList = (ArrayList<API>) session.getAttribute("originalSupplyList");
			
			// 创建包含所有原始耗材的supplyList，并替换第一个耗材
			ArrayList<API> supplyList = new ArrayList<>();
			
			if (originalSupplyList != null && !originalSupplyList.isEmpty()) {
				// 替换第一个耗材，保留其他耗材
				supplyList.add(newSupply);
				for (int i = 1; i < originalSupplyList.size(); i++) {
					supplyList.add(originalSupplyList.get(i));
				}
			} else {
				// 如果没有原始列表，则只显示新耗材
				supplyList.add(newSupply);
			}
			
			// 设置必要的属性并转发到searchResult.jsp
			request.setAttribute("supplyList", supplyList);
			request.setAttribute("supplyIndex_ID", supplyIndex_ID);
			request.setAttribute("supplyIndex_Name", supplyIndex_Name);
			
			// 设置其他必要属性（从会话中获取或使用默认值）
			request.setAttribute("disease", session.getAttribute("disease"));
			Integer diseaseIndex = (Integer) session.getAttribute("diseaseIndex");
			request.setAttribute("diseaseIndex", diseaseIndex != null ? diseaseIndex : -1);
			@SuppressWarnings("unchecked")
			Map<Integer, Integer> supplyToInterestMap = (Map<Integer, Integer>) session.getAttribute("supplyToInterestMap");
			request.setAttribute("supplyToInterestMap", supplyToInterestMap != null ? supplyToInterestMap : new HashMap<>());
			request.setAttribute("diseaseIndex_ID", diseaseIndex_ID);
			request.setAttribute("diseaseIndex_Name", diseaseIndex_Name);
			
			request.getRequestDispatcher("searchResult.jsp").forward(request, response);
		} catch (Exception e) {
			e.printStackTrace();
			request.setAttribute("error", "系统错误: " + e.getMessage());
			request.getRequestDispatcher("error.jsp").forward(request, response);
		}
	}
	
	// 根据病种名称确定topK值
	private int determineTopKForSearch(String diseaseName) {
		if (diseaseName == null || diseaseName.trim().isEmpty()) {
			return DEFAULT_TOPK;
		}

		// 根据病种名称匹配预设的topK值（精确匹配）
		Integer presetTopK = DISEASE_TOPK_CONFIG.get(diseaseName);
		if (presetTopK != null) {
			System.out.println("匹配到预设病种: " + diseaseName + " -> TopK: " + presetTopK);
			return presetTopK;
		}

		// 如果没有匹配到预设值，返回默认值
		System.out.println("未匹配到预设病种: " + diseaseName + " -> 使用默认TopK: " + DEFAULT_TOPK);
		return DEFAULT_TOPK;
	}


}