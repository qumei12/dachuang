package com.wangyan.servlets;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Collections;
import java.util.Comparator;
import java.util.Arrays;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.wangyan.index.SupplyMap;
import com.wangyan.index.IndexUtil;
import com.wangyan.index.DiseaseMap;

import javabean.Supply;
import javabean.Disease;
import javabean.Case;
import model.LDAModel;
import model.ModelTrainer;
import dbhelper.DBSearch;
import filehelper.CaseSupplyMatrixService;

// 添加IO相关的导入
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Set;
import java.io.File;
import java.io.FileNotFoundException;

/**
 * Servlet implementation class Search
 */
public class Search extends HttpServlet {
	private static final long serialVersionUID = 1L;
	

	
	// LDA模型实例
	private static LDAModel ldaModel = null;
	
	// 其他成员变量
	private Map<Integer, List<Integer>> supplyWordsBag;
	private Map<Integer, Integer> diseaseIndex_ID;
	private Map<Integer, String> diseaseIndex_Name;
	private Map<Integer, Integer> supplyIndex_ID;
	private Map<Integer, String> supplyIndex_Name;
	private IndexUtil iu;
	

       
    /**
     * @see HttpServlet#HttpServlet()
     */
	public Search() {
		super();
		// 初始化LDA模型（只初始化一次）
		if (ldaModel == null) {
			// 尝试加载预训练模型
			ldaModel = ModelTrainer.loadPretrainedModel();
			
			if (ldaModel == null) {
				// 如果没有预训练模型，则进行实时训练
				System.out.println("未找到预训练模型，进行实时训练...");
				ldaModel = new LDAModel();
				ldaModel.initializeLDAModel();
				ldaModel.inferenceModel();
				System.out.println("实时训练完成");
			}
			
			// 获取病案（原Mashup）和耗材（原API）的词汇袋
			// 由于LDAModel中没有getMashupWordsBag方法，我们使用getAPIWordsBag获取耗材词汇袋
			// 病案的词汇袋可以通过其他方式获取，这里暂时注释掉
			// diseaseWordsBag = ldaModel.getMashupWordsBag();
			supplyWordsBag = ldaModel.getAPIWordsBag();
			//System.out.println("词袋子生成完毕");
		}

		diseaseIndex_ID = new HashMap<>();
		diseaseIndex_Name = new HashMap<>();
		new DiseaseMap().setMap(diseaseIndex_ID, diseaseIndex_Name);

		supplyIndex_ID = new HashMap<>();
		supplyIndex_Name = new HashMap<>();
		new SupplyMap().setMap(supplyIndex_ID, supplyIndex_Name);

		iu = new IndexUtil();
		iu.createIndex(diseaseIndex_Name);
	}
	

	
	/**
	 * 解析CSV行，支持带引号的字段
	 * @param line CSV行内容
	 * @return 字段数组
	 */
	private static String[] parseCsvLine(String line) {
		if (line == null || line.trim().isEmpty()) {
			return new String[0];
		}
		
		java.util.ArrayList<String> values = new java.util.ArrayList<>();
		StringBuilder current = new StringBuilder();
		boolean inQuotes = false;
		
		for (int i = 0; i < line.length(); i++) {
			char c = line.charAt(i);
			
			if (c == '"') {
				// 检查是否是转义引号
				if (i + 1 < line.length() && line.charAt(i + 1) == '"') {
					// 双引号表示一个引号字符
					current.append('"');
					i++; // 跳过下一个引号
				} else {
					// 切换引号状态
					inQuotes = !inQuotes;
				}
			} else if (c == ',' && !inQuotes) {
				values.add(current.toString());
				current = new StringBuilder();
			} else {
				current.append(c);
			}
		}
		
		// 添加最后一个字段
		values.add(current.toString());
		
		// 清理每个字段值，去除首尾空格但保留引号内的空格
		String[] result = new String[values.size()];
		for (int i = 0; i < values.size(); i++) {
			result[i] = values.get(i).trim();
		}
		
		return result;
	}
	
	/**
	 * 根据DRG编码获取支付标准
	 * @param drgCode DRG编码
	 * @return 支付标准金额，如果未找到则返回0.0
	 */
	private static double getPaymentStandardByDrgCode(String drgCode) {
		// 从数据库中获取DRG支付标准，而不是从CSV文件
		DBSearch dbSearch = new DBSearch();
		
		// 这里需要根据DRG编码查找对应的病种，然后获取其支付标准
		// 由于数据库结构中没有直接存储DRG编码，我们假设drgCode就是病种名称
		Disease disease = dbSearch.getDiseaseByName(drgCode);
		
		if (disease != null && disease.getDrgPaymentStandard() != null && !disease.getDrgPaymentStandard().isEmpty()) {
			try {
				return Double.parseDouble(disease.getDrgPaymentStandard());
			} catch (NumberFormatException e) {
				System.err.println("无法解析DRG支付标准: " + disease.getDrgPaymentStandard());
			}
		}
		
		return 0.0;
	}
	
	/**
	 * 计算指定DRG编码下，给定金额在DRG明细总金额中的排名百分比
	 * @param drgCode DRG编码
	 * @param targetAmount 目标金额
	 * @return 排名百分比（0-100），0%表示最便宜，100%表示最贵
	 */
	private static double calculateAmountRankPercentile(String drgCode, double targetAmount) {
		// 从数据库中获取DRG明细总金额数据，而不是从CSV文件
		DBSearch dbSearch = new DBSearch();
		
		// 根据病种名称获取病种ID
		Disease disease = dbSearch.getDiseaseByName(drgCode);
		if (disease == null || disease.getID() == -1) {
			return -1; // 未找到病种
		}
		
		// 获取该病种下的所有病例
		List<Case> cases = dbSearch.getCasesByMashupId(disease.getID());
		
		// 如果没有找到相关数据
		if (cases.isEmpty()) {
			return -1;
		}
		
		// 提取DRG明细总金额并排序
		List<Double> drgDetailAmounts = new ArrayList<>();
		for (Case caseObj : cases) {
			if (caseObj.getDrgDetailTotalAmount() != null) {
				drgDetailAmounts.add(caseObj.getDrgDetailTotalAmount().doubleValue());
			}
		}
		
		Collections.sort(drgDetailAmounts);
		
		// 计算排名
		int rank = 0;
		for (int i = 0; i < drgDetailAmounts.size(); i++) {
			if (drgDetailAmounts.get(i) <= targetAmount) {
				rank = i + 1;
			} else {
				break;
			}
		}
		
		// 计算百分比
		double percentile = (double) rank / drgDetailAmounts.size() * 100;
		
		return percentile;
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
			Disease disease = dbs.getDiseaseByName(diseaseName);
			
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
			
			// 尝试从预计算结果中加载推荐耗材
			List<Supply> recommandSupplyList = null;
			List<Integer> precomputedSupplyIndices = loadPrecomputedSupplies(diseaseName);
			
			if (precomputedSupplyIndices != null) {
				// 使用预计算的结果
				recommandSupplyList = getSuppliesFromIndices(precomputedSupplyIndices);
				System.out.println("使用预计算的推荐结果，共 " + recommandSupplyList.size() + " 个耗材");
			} else {
				// 使用LDA模型为病种推荐耗材，而不是直接从数据库获取关联耗材
				// 智能计算最优推荐主题数量
				int dynamicTopK = determineTopKForSearch(diseaseName);
				
				// 使用IndexUtil的推荐方法进行推荐（使用LDA模型）
				// 传入病种ID以使用该病种下所有病案的平均主题分布
				recommandSupplyList = iu.recommendOneSupplyPerInterestByLDAModel(
					diseaseIndex, 
					dynamicTopK,
					ldaModel,
					supplyIndex_ID,
					disease.getID()  // 传入病种ID
				);
				System.out.println("实时计算推荐结果，共 " + recommandSupplyList.size() + " 个耗材");
			}
			
			// 如果没有推荐到耗材
			if (recommandSupplyList == null || recommandSupplyList.isEmpty()) {
				request.setAttribute("disease", disease);
				request.setAttribute("supplyList", new ArrayList<Supply>());
				request.setAttribute("diseaseIndex", diseaseIndex);
				request.getRequestDispatcher("searchResult.jsp").forward(request, response);
				return;
			}
			
			// 将推荐结果转换为ArrayList
			ArrayList<Supply> limitedSupplyList = new ArrayList<>(recommandSupplyList);
			
			// 过滤掉在当前病种中没有使用记录的耗材
			ArrayList<Supply> filteredSupplyList = new ArrayList<>();
			for (Supply supply : limitedSupplyList) {
				if (supply != null) {
					// 检查该耗材在当前病种中是否有使用记录
					int usageCount = dbs.getAverageSupplyQuantityForDisease(disease.getID(), supply.getID());
					if (usageCount > 0) {
						// 只有在当前病种中有使用记录的耗材才保留
						filteredSupplyList.add(supply);
					}
				}
			}
			limitedSupplyList = filteredSupplyList;
			
			// 创建耗材索引到兴趣主题的映射
			Map<Integer, Integer> supplyToInterestMap = new HashMap<>();
			
			// 获取当前病种的topK兴趣主题
			// 使用病种下所有病案的平均主题分布
			Double[] interestProbs = new Double[ldaModel.getTopicAmount()];
			
			// 获取该病种下所有病案的索引
			int diseaseId = disease.getID(); // 获取病种在数据库中的ID
			List<Integer> caseIndexes = CaseSupplyMatrixService.getCaseIndexesByDiseaseId(diseaseId);
			
			if (caseIndexes != null && !caseIndexes.isEmpty()) {
				// 计算所有病案的平均主题分布
				for (int k = 0; k < ldaModel.getTopicAmount(); k++) {
					interestProbs[k] = 0.0;
				}
				
				for (int caseIndex : caseIndexes) {
					// 确保病案索引在有效范围内
					if (caseIndex >= 0 && caseIndex < ldaModel.getCaseAmount()) {
						for (int k = 0; k < ldaModel.getTopicAmount(); k++) {
							interestProbs[k] += ldaModel.getTheta()[caseIndex][k];
						}
					}
				}
				
				// 计算平均值
				for (int k = 0; k < ldaModel.getTopicAmount(); k++) {
					interestProbs[k] /= caseIndexes.size();
				}
				
				System.out.println("使用病种 \"" + diseaseName + "\" 下 " + caseIndexes.size() + " 个病案的平均主题分布");
			} else {
				// 如果没有找到相关病案，回退到使用单个病案的分布
				for (int k = 0; k < ldaModel.getTopicAmount(); k++) {
					interestProbs[k] = ldaModel.getTheta()[diseaseIndex][k];
				}
				System.out.println("回退到使用单个病案的主题分布");
			}

			List<Integer> interestIndices = new ArrayList<>();
			for (int i = 0; i < ldaModel.getTopicAmount(); i++) {
				interestIndices.add(i);
			}

			// 按照概率排序
			interestIndices.sort((o1, o2) -> {
				if (interestProbs[o1] > interestProbs[o2]) return -1;
				else if (interestProbs[o1] < interestProbs[o2]) return 1;
				else return 0;
			});
			
			System.out.println("病种 \"" + diseaseName + "\" 的前 " + Math.min(10, interestIndices.size()) + " 个兴趣主题:");
			// 正确地为每个推荐的耗材确定其所属的主题
			int[] interestIds = new int[limitedSupplyList.size()];
			for (int i = 0; i < Math.min(10, limitedSupplyList.size()); i++) {
				int interestId = interestIndices.get(i);
				Supply supply = limitedSupplyList.get(i);
				System.out.println("  兴趣主题 " + interestId + " - 概率: " + String.format("%.6f", interestProbs[interestId]));
				System.out.println("    推荐耗材: \"" + supply.getNAME() + "\"");
				
				// 通过supplyIndex_ID查找耗材索引
				Integer supplyIndex = null;
				for (Map.Entry<Integer, Integer> entry : supplyIndex_ID.entrySet()) {
					if (entry.getValue().equals(supply.getID())) {
						supplyIndex = entry.getKey();
						break;
					}
				}
				
				if (supplyIndex != null) {
					// 直接使用推荐时的主题ID，确保一致性
					supplyToInterestMap.put(supplyIndex, interestId);
					interestIds[i] = interestId;
				}
			}
			
			// 显示该病种在所有主题上的概率分布（调试用，可删除）
			/*
			System.out.println("病种 \"" + diseaseName + "\" 在所有 " + ldaModel.getTopicAmount() + " 个主题上的概率分布:");
			for (int i = 0; i < interestIndices.size(); i++) {
				int topicIndex = interestIndices.get(i);
				System.out.println("  主题 " + topicIndex + " - 概率: " + String.format("%.6f", interestProbs[topicIndex]) + 
					(i < dynamicTopK ? " [推荐]" : ""));
			}
			*/
			
			// 计算每个耗材的平均使用数量
			// 创建耗材ID到平均使用数量的映射
			Map<Integer, Integer> supplyToAverageQuantityMap = new HashMap<>();
			for (Supply supply : limitedSupplyList) {
				// 计算该耗材在该病种中的平均使用数量
				int averageQuantity = dbs.getAverageSupplyQuantityForDisease(disease.getID(), supply.getID());
				supplyToAverageQuantityMap.put(supply.getID(), averageQuantity);
				System.out.println("    平均使用数量: " + averageQuantity);
			}
			
			// 获取DRG支付标准
			double drgPaymentStandard = getPaymentStandardByDrgCode(disease.getNAME());
			
			// 计算总价
			double totalAmount = 0.0;
			for (Supply supply : limitedSupplyList) {
				double unitPrice = 0.0;
				int averageQuantity = 0;
				
				if (supply.getPRICE() != null && !supply.getPRICE().isEmpty() && !supply.getPRICE().equals("0")) {
					try {
						unitPrice = Double.parseDouble(supply.getPRICE());
					} catch (NumberFormatException e) {
						// 忽略无法解析的价格
					}
				}
				
				if (supplyToAverageQuantityMap != null) {
					Integer quantity = supplyToAverageQuantityMap.get(supply.getID());
					if (quantity != null) {
						averageQuantity = quantity;
					}
				}
				
				totalAmount += unitPrice * averageQuantity;
			}
			
			// 计算总价在DRG明细总金额中的排名百分比
			double amountRankPercentile = calculateAmountRankPercentile(disease.getNAME(), totalAmount);
			
			// 获取DRG总金额最大的病案的耗材信息
			Map<String, Object> highestCostCaseInfo = getHighestCostCaseSupplies(disease.getNAME(), 5);
			// 获取DRG总金额最小的病案的耗材信息
			Map<String, Object> lowestCostCaseInfo = getLowestCostCaseSupplies(disease.getNAME(), 5);
			
			// 传递数据到JSP页面
			request.setAttribute("disease", disease);
			request.setAttribute("supplyList", limitedSupplyList);
			request.setAttribute("supplyToAverageQuantityMap", supplyToAverageQuantityMap);
			request.setAttribute("drgPaymentStandard", drgPaymentStandard);
			request.setAttribute("totalAmount", totalAmount);
			request.setAttribute("amountRankPercentile", amountRankPercentile);
			request.setAttribute("diseaseIndex", diseaseIndex);
			request.setAttribute("supplyToInterestMap", supplyToInterestMap);
			request.setAttribute("diseaseIndex_ID", diseaseIndex_ID);
			request.setAttribute("diseaseIndex_Name", diseaseIndex_Name);
			request.setAttribute("supplyIndex_ID", supplyIndex_ID);
			request.setAttribute("supplyIndex_Name", supplyIndex_Name);
			request.setAttribute("highestCostCaseInfo", highestCostCaseInfo);
			request.setAttribute("lowestCostCaseInfo", lowestCostCaseInfo);
			
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
			
			// 保存每行对应的主题ID列表
			List<Integer> rowToInterestList = new ArrayList<>();
			for (int i = 0; i < Math.min(limitedSupplyList.size(), interestIds.length); i++) {
				rowToInterestList.add(interestIds[i]);
			}
			session.setAttribute("rowToInterestList", rowToInterestList);
			
			// 保存病种的完整兴趣主题排序列表
			session.setAttribute("diseaseInterestOrder", new ArrayList<>(interestIndices));
			
			// 保存DRG支付标准和排名百分比
			session.setAttribute("drgPaymentStandard", drgPaymentStandard);
			session.setAttribute("amountRankPercentile", amountRankPercentile);
			
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
			Disease disease = dbs.getDiseaseByName(diseaseName);
			
			if (disease != null && disease.getID() != -1) {
				// 精确匹配成功，重定向到 doGet 方法处理
				String redirectURL = "./Search?search=" + java.net.URLEncoder.encode(diseaseName, "UTF-8");
				response.sendRedirect(redirectURL);
				return;
			}
			
			// 如果精确匹配失败，执行模糊搜索
			System.out.println("精确匹配失败，执行病种名称模糊搜索: " + diseaseName);
			ArrayList<Disease> diseases = dbs.getDiseaseByNameFuzzy(diseaseName);
			
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
			Supply newSupply = dbs.getSupplyById(replaceSupplyId);
			
			// 创建包含所有原始耗材的supplyList，并替换指定位置的耗材
			ArrayList<Supply> supplyList = new ArrayList<>();
			
			// 从会话中获取原始的supplyList
			HttpSession session = request.getSession();
			@SuppressWarnings("unchecked")
			ArrayList<Supply> originalSupplyList = (ArrayList<Supply>) session.getAttribute("originalSupplyList");
			
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
			
			// 获取疾病信息
			Disease disease = (Disease) session.getAttribute("disease");
			
			// 获取原始的平均使用数量映射
			@SuppressWarnings("unchecked")
			Map<Integer, Integer> supplyToAverageQuantityMap = (Map<Integer, Integer>) session.getAttribute("supplyToAverageQuantityMap");
			
			// 如果原始映射为空，则需要为所有耗材重新计算平均使用数量
			if (supplyToAverageQuantityMap == null) {
				supplyToAverageQuantityMap = new HashMap<>();
				// 为所有耗材计算平均使用数量
				for (Supply supply : supplyList) {
					int averageQuantity = dbs.getAverageSupplyQuantityForDisease(disease.getID(), supply.getID());
					supplyToAverageQuantityMap.put(supply.getID(), averageQuantity);
				}
			} else {
				// 更新映射表，添加新替换耗材的平均使用数量
				int newSupplyAverageQuantity = dbs.getAverageSupplyQuantityForDisease(disease.getID(), newSupply.getID());
				supplyToAverageQuantityMap.put(newSupply.getID(), newSupplyAverageQuantity);
			}
			
			// 获取DRG支付标准
			Double drgPaymentStandardObj = (Double) session.getAttribute("drgPaymentStandard");
			double drgPaymentStandard = (drgPaymentStandardObj != null) ? drgPaymentStandardObj : 0.0;
			
			// 计算总价
			double totalAmount = 0.0;
			for (Supply supply : supplyList) {
				double unitPrice = 0.0;
				int averageQuantity = 0;
				
				if (supply.getPRICE() != null && !supply.getPRICE().isEmpty() && !supply.getPRICE().equals("0")) {
					try {
						unitPrice = Double.parseDouble(supply.getPRICE());
					} catch (NumberFormatException e) {
						// 忽略无法解析的价格
					}
				}
				
				Integer quantity = supplyToAverageQuantityMap.get(supply.getID());
				if (quantity != null) {
					averageQuantity = quantity;
				}
				
				totalAmount += unitPrice * averageQuantity;
			}
			
			// 重新计算总价在DRG明细总金额中的排名百分比
			double amountRankPercentile = calculateAmountRankPercentile(disease.getNAME(), totalAmount);
			
			// 设置必要的属性并转发到searchResult.jsp
			request.setAttribute("supplyList", supplyList);
			request.setAttribute("supplyIndex_ID", supplyIndex_ID);
			request.setAttribute("supplyIndex_Name", supplyIndex_Name);
			request.setAttribute("supplyToAverageQuantityMap", supplyToAverageQuantityMap);
			request.setAttribute("totalAmount", totalAmount);
			request.setAttribute("amountRankPercentile", amountRankPercentile);
			
			// 设置其他必要属性（从会话中获取或使用默认值）
			request.setAttribute("disease", disease);
			Integer diseaseIndex = (Integer) session.getAttribute("diseaseIndex");
			request.setAttribute("diseaseIndex", diseaseIndex != null ? diseaseIndex : -1);
			@SuppressWarnings("unchecked")
			Map<Integer, Integer> supplyToInterestMap = (Map<Integer, Integer>) session.getAttribute("supplyToInterestMap");
			request.setAttribute("supplyToInterestMap", supplyToInterestMap != null ? supplyToInterestMap : new HashMap<>());
			
			// 传递行对应的主题ID列表
			@SuppressWarnings("unchecked")
			List<Integer> rowToInterestList = (List<Integer>) session.getAttribute("rowToInterestList");
			request.setAttribute("rowToInterestList", rowToInterestList != null ? rowToInterestList : new ArrayList<>());
			
			request.setAttribute("diseaseIndex_ID", diseaseIndex_ID);
			request.setAttribute("diseaseIndex_Name", diseaseIndex_Name);
			request.setAttribute("supplyIndex_ID", supplyIndex_ID);
			request.setAttribute("supplyIndex_Name", supplyIndex_Name);
			request.setAttribute("drgPaymentStandard", drgPaymentStandard);
			
			// 重新获取最高金额和最低金额病案的信息
			if (disease != null) {
				Map<String, Object> highestCostCaseInfo = getHighestCostCaseSupplies(disease.getNAME(), 5);
				Map<String, Object> lowestCostCaseInfo = getLowestCostCaseSupplies(disease.getNAME(), 5);
				request.setAttribute("highestCostCaseInfo", highestCostCaseInfo);
				request.setAttribute("lowestCostCaseInfo", lowestCostCaseInfo);
			}
			
			request.getRequestDispatcher("searchResult.jsp").forward(request, response);
			
		} catch (Exception e) {
			e.printStackTrace();
			request.setAttribute("error", "系统错误: " + e.getMessage());
			request.getRequestDispatcher("error.jsp").forward(request, response);
		}
	}
	
	/**
	 * 根据病种名称确定topK值
	 * @param diseaseName 病种名称
	 * @return topK值
	 */
	private int determineTopKForSearch(String diseaseName) {
		if (diseaseName == null || diseaseName.trim().isEmpty()) {
			return 10;
		}

		// 查找病种索引
		Integer diseaseIndex = null;
		for (Map.Entry<Integer, String> entry : diseaseIndex_Name.entrySet()) {
			if (entry.getValue().equals(diseaseName)) {
				diseaseIndex = entry.getKey();
				break;
			}
		}

		// 如果找不到病种索引，返回默认值
		if (diseaseIndex == null) {
			return 10;
		}

		// 获取该病种下所有病案的索引
		DBSearch dbs = new DBSearch();
		Disease disease = dbs.getDiseaseByName(diseaseName);
		if (disease == null) {
			return 10;
		}

		List<Integer> caseIndexes = CaseSupplyMatrixService.getCaseIndexesByDiseaseId(disease.getID());

		// 计算病种在各个主题上的平均分布
		Double[] interestProbs = new Double[ldaModel.getTopicAmount()];
		for (int k = 0; k < ldaModel.getTopicAmount(); k++) {
			interestProbs[k] = 0.0;
		}

		if (caseIndexes != null && !caseIndexes.isEmpty()) {
			// 使用病种下所有病案的平均主题分布
			for (int caseIndex : caseIndexes) {
				if (caseIndex >= 0 && caseIndex < ldaModel.getCaseAmount()) {
					for (int k = 0; k < ldaModel.getTopicAmount(); k++) {
						interestProbs[k] += ldaModel.getTheta()[caseIndex][k];
					}
				}
			}

			// 计算平均值
			for (int k = 0; k < ldaModel.getTopicAmount(); k++) {
				interestProbs[k] /= caseIndexes.size();
			}
		} else {
			// 如果没有找到相关病案，回退到使用单个病案的分布
			for (int k = 0; k < ldaModel.getTopicAmount(); k++) {
				interestProbs[k] = ldaModel.getTheta()[diseaseIndex][k];
			}
		}

		// 动态计算最优的topK值
		// 1. 计算所有主题概率的总和
		double totalProb = 0.0;
		for (int k = 0; k < ldaModel.getTopicAmount(); k++) {
			totalProb += interestProbs[k];
		}

		// 2. 计算累积概率，确定需要多少个主题才能覆盖大部分概率分布
		List<Integer> sortedIndices = new ArrayList<>();
		for (int i = 0; i < ldaModel.getTopicAmount(); i++) {
			sortedIndices.add(i);
		}

		// 按照概率排序
		sortedIndices.sort((o1, o2) -> {
			if (interestProbs[o1] > interestProbs[o2]) return -1;
			else if (interestProbs[o1] < interestProbs[o2]) return 1;
			else return 0;
		});

		// 输出前10个主题及其概率，便于调试
		System.out.println("病种 \"" + diseaseName + "\" 的前10个兴趣主题（用于计算dynamicTopK）:");
		for (int i = 0; i < Math.min(10, sortedIndices.size()); i++) {
			int topicIndex = sortedIndices.get(i);
			System.out.println("  主题 " + topicIndex + " - 概率: " + String.format("%.6f", interestProbs[topicIndex]));
		}

		// 计算累积概率，找到覆盖90%以上概率分布所需的主题数
		double cumulativeProb = 0.0;
		int dynamicTopK = 3; // 至少推荐3个主题
		for (int i = 0; i < sortedIndices.size(); i++) {
			cumulativeProb += interestProbs[sortedIndices.get(i)];
			System.out.println("  累积处理主题 " + sortedIndices.get(i) + "，当前累积概率: " + String.format("%.6f", cumulativeProb) + 
				" (" + String.format("%.2f", cumulativeProb / totalProb * 100) + "%)");
			if (cumulativeProb >= 0.95 * totalProb) {
				dynamicTopK = i + 1;
				System.out.println("  达到95%阈值，dynamicTopK设置为: " + dynamicTopK);
				break;
			}
		}

		// 限制推荐主题数量的下限（最少3个），但取消上限限制
		dynamicTopK = Math.max(3, dynamicTopK);
		System.out.println("最终确定的dynamicTopK值: " + dynamicTopK);

		return dynamicTopK;
	}
	
	/**
	 * 从预计算结果中加载推荐耗材
	 * @param diseaseName 病种名称
	 * @return 耗材索引列表，如果未找到预计算结果则返回null
	 */
	private List<Integer> loadPrecomputedSupplies(String diseaseName) {
		// 当前实现返回null，表示没有预计算结果，将使用实时计算
		return null;
	}
	
	/**
	 * 根据索引列表获取耗材对象列表
	 * @param supplyIndices 耗材索引列表
	 * @return 耗材对象列表
	 */
	private List<Supply> getSuppliesFromIndices(List<Integer> supplyIndices) {
		List<Supply> supplies = new ArrayList<>();
		if (supplyIndices != null) {
			DBSearch dbs = new DBSearch();
			for (Integer index : supplyIndices) {
				// 通过supplyIndex_ID映射获取实际的耗材ID
				Integer supplyId = supplyIndex_ID.get(index);
				if (supplyId != null) {
					Supply supply = dbs.getSupplyById(supplyId);
					if (supply != null) {
						supplies.add(supply);
					}
				}
			}
		}
		return supplies;
	}
	
	/**
	 * 获取指定DRG编码下DRG总金额最大的病案的耗材信息
	 * @param drgCode DRG编码
	 * @param maxSupplies 最大耗材数量
	 * @return 包含病案信息和耗材列表的Map
	 */
	private static Map<String, Object> getHighestCostCaseSupplies(String drgCode, int maxSupplies) {
		Map<String, Object> result = new HashMap<>();
		
		// 从数据库中获取数据，而不是从CSV文件
		DBSearch dbSearch = new DBSearch();
		
		// 根据病种名称获取病种ID
		Disease disease = dbSearch.getDiseaseByName(drgCode);
		if (disease == null || disease.getID() == -1) {
			return result; // 未找到病种
		}
		
		// 获取该病种下的所有病例，按DRG明细总金额排序
		List<Case> cases = dbSearch.getCasesByMashupId(disease.getID());
		
		if (cases.isEmpty()) {
			return result; // 没有找到病例
		}
		
		// 找到DRG总金额最大的病案
		Case highestCostCase = null;
		double maxAmount = -1;
		
		for (Case caseObj : cases) {
			if (caseObj.getDrgDetailTotalAmount() != null && caseObj.getDrgDetailTotalAmount().doubleValue() > maxAmount) {
				maxAmount = caseObj.getDrgDetailTotalAmount().doubleValue();
				highestCostCase = caseObj;
			}
		}
		
		if (highestCostCase != null) {
			result.put("caseId", highestCostCase.getCaseId());
			result.put("totalAmount", maxAmount);
			
			// TODO: 从数据库获取该病案的耗材列表
			// 由于数据库结构限制，这里暂时返回空列表
			result.put("supplies", new ArrayList<Map<String, String>>());
		}
		
		return result;
	}
	
	/**
	 * 获取指定DRG编码下DRG总金额最小的病案的耗材信息
	 * @param drgCode DRG编码
	 * @param maxSupplies 最大耗材数量
	 * @return 包含病案信息和耗材列表的Map
	 */
	private static Map<String, Object> getLowestCostCaseSupplies(String drgCode, int maxSupplies) {
		Map<String, Object> result = new HashMap<>();
		
		// 从数据库中获取数据，而不是从CSV文件
		DBSearch dbSearch = new DBSearch();
		
		// 根据病种名称获取病种ID
		Disease disease = dbSearch.getDiseaseByName(drgCode);
		if (disease == null || disease.getID() == -1) {
			return result; // 未找到病种
		}
		
		// 获取该病种下的所有病例，按DRG明细总金额排序
		List<Case> cases = dbSearch.getCasesByMashupId(disease.getID());
		
		if (cases.isEmpty()) {
			return result; // 没有找到病例
		}
		
		// 找到DRG总金额最小的病案
		Case lowestCostCase = null;
		double minAmount = Double.MAX_VALUE;
		
		for (Case caseObj : cases) {
			if (caseObj.getDrgDetailTotalAmount() != null && caseObj.getDrgDetailTotalAmount().doubleValue() < minAmount) {
				minAmount = caseObj.getDrgDetailTotalAmount().doubleValue();
				lowestCostCase = caseObj;
			}
		}
		
		if (lowestCostCase != null) {
			result.put("caseId", lowestCostCase.getCaseId());
			result.put("totalAmount", minAmount);
			
			// TODO: 从数据库获取该病案的耗材列表
			// 由于数据库结构限制，这里暂时返回空列表
			result.put("supplies", new ArrayList<Map<String, String>>());
		}
		
		return result;
	}
}