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
import model.LDAModel;
import model.ModelTrainer;
import dbhelper.DBSearch;
import filehelper.CaseSupplyMatrixService;

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

	// 默认topK值
	private static final int DEFAULT_TOPK = 3;

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
			Disease disease = dbs.getMashupByName(diseaseName);
			
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
			// 智能计算最优推荐主题数量
			int dynamicTopK = calculateOptimalRecommendationCount(diseaseIndex, disease, ldaModel);
			
			// 使用IndexUtil的推荐方法进行推荐（使用LDA模型）
			List<Supply> recommandSupplyList = iu.recommendOneSupplyPerInterestByLDAModel(
				diseaseIndex, 
				dynamicTopK,
				ldaModel,
				supplyIndex_ID
			);
			
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
			
			System.out.println("病种 \"" + diseaseName + "\" 的Top " + dynamicTopK + " 个兴趣主题:");
			// 正确地为每个推荐的耗材确定其所属的主题
			int[] interestIds = new int[limitedSupplyList.size()];
			for (int i = 0; i < Math.min(dynamicTopK, limitedSupplyList.size()); i++) {
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
			
			// 显示该病种在所有主题上的概率分布
			System.out.println("病种 \"" + diseaseName + "\" 在所有 " + ldaModel.getTopicAmount() + " 个主题上的概率分布:");
			for (int i = 0; i < interestIndices.size(); i++) {
				int topicIndex = interestIndices.get(i);
				System.out.println("  主题 " + topicIndex + " - 概率: " + String.format("%.6f", interestProbs[topicIndex]) + 
					(i < dynamicTopK ? " [推荐]" : ""));
			}
			
			// 计算每个耗材的平均使用数量
			// 创建耗材ID到平均使用数量的映射
			Map<Integer, Integer> supplyToAverageQuantityMap = new HashMap<>();
			for (Supply supply : limitedSupplyList) {
				// 计算该耗材在该病种中的平均使用数量
				int averageQuantity = dbs.getAverageSupplyQuantityForDisease(disease.getID(), supply.getID());
				supplyToAverageQuantityMap.put(supply.getID(), averageQuantity);
				System.out.println("    平均使用数量: " + averageQuantity);
			}
			
			// 传递数据到JSP页面
			request.setAttribute("disease", disease);
			request.setAttribute("supplyList", limitedSupplyList);
			request.setAttribute("supplyToAverageQuantityMap", supplyToAverageQuantityMap);
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
			
			// 保存每行对应的主题ID列表
			List<Integer> rowToInterestList = new ArrayList<>();
			for (int i = 0; i < Math.min(limitedSupplyList.size(), interestIds.length); i++) {
				rowToInterestList.add(interestIds[i]);
			}
			session.setAttribute("rowToInterestList", rowToInterestList);
			
			// 保存病种的完整兴趣主题排序列表
			session.setAttribute("diseaseInterestOrder", new ArrayList<>(interestIndices));
			
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
			Disease disease = dbs.getMashupByName(diseaseName);
			
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
			
			// 传递行对应的主题ID列表
			@SuppressWarnings("unchecked")
			List<Integer> rowToInterestList = (List<Integer>) session.getAttribute("rowToInterestList");
			request.setAttribute("rowToInterestList", rowToInterestList != null ? rowToInterestList : new ArrayList<>());
			
			request.setAttribute("diseaseIndex_ID", diseaseIndex_ID);
			request.setAttribute("diseaseIndex_Name", diseaseIndex_Name);
			request.setAttribute("supplyIndex_ID", supplyIndex_ID);
			request.setAttribute("supplyIndex_Name", supplyIndex_Name);
			
			request.getRequestDispatcher("searchResult.jsp").forward(request, response);
			
		} catch (Exception e) {
			e.printStackTrace();
			request.setAttribute("error", "系统错误: " + e.getMessage());
			request.getRequestDispatcher("error.jsp").forward(request, response);
		}
	}
	
	/**
	 * 根据病种在各主题上的概率分布智能确定推荐主题数量
	 * 
	 * @param diseaseIndex 病种索引
	 * @param disease 病种对象
	 * @param ldaModel LDA模型
	 * @return 推荐的主题数量
	 */
	private int calculateOptimalRecommendationCount(int diseaseIndex, Disease disease, LDAModel ldaModel) {
		// 最小推荐主题数
		final int MIN_RECOMMENDATIONS = 1;
		
		// 最大推荐主题数
		final int MAX_RECOMMENDATIONS = 20;
		
		// 最小主题概率阈值
		final double MIN_PROBABILITY_THRESHOLD = 0.01; // 1%
		
		// 累积概率阈值
		final double CUMULATIVE_PROBABILITY_THRESHOLD = 0.8; // 80%

		// 获取病种在各主题上的概率分布
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
		} else {
			// 如果没有找到相关病案，回退到使用单个病案的分布
			for (int k = 0; k < ldaModel.getTopicAmount(); k++) {
				interestProbs[k] = ldaModel.getTheta()[diseaseIndex][k];
			}
		}
		
		// 按概率降序排序
		List<Double> sortedProbs = new ArrayList<>(Arrays.asList(interestProbs));
		Collections.sort(sortedProbs, Collections.reverseOrder());
		
		// 方法1: 基于累积概率阈值
		int countByCumulative = getRecommendationCountByCumulativeProbability(sortedProbs, CUMULATIVE_PROBABILITY_THRESHOLD);
		
		// 方法2: 基于最小概率阈值
		int countByMinThreshold = getRecommendationCountByMinThreshold(sortedProbs, MIN_PROBABILITY_THRESHOLD);
		
		// 方法3: 基于肘部法则(Elbow Method)
		int countByElbow = getRecommendationCountByElbowMethod(sortedProbs);
		
		// 综合决策：取多种方法的平均值
		int finalCount = (int) Math.round((countByCumulative + countByMinThreshold + countByElbow) / 3.0);
		
		// 确保在合理范围内
		finalCount = Math.max(MIN_RECOMMENDATIONS, finalCount);
		finalCount = Math.min(MAX_RECOMMENDATIONS, finalCount);
		
		System.out.println("智能推荐数量计算:");
		System.out.println("  基于累积概率: " + countByCumulative);
		System.out.println("  基于最小阈值: " + countByMinThreshold);
		System.out.println("  基于肘部法则: " + countByElbow);
		System.out.println("  基于平均值: " + finalCount);
		
		return finalCount;
	}
	
	/**
	 * 基于累积概率阈值确定推荐数量
	 * 
	 * @param sortedProbs 按概率降序排列的主题概率列表
	 * @param threshold 累积概率阈值
	 * @return 推荐数量
	 */
	private int getRecommendationCountByCumulativeProbability(List<Double> sortedProbs, double threshold) {
		double cumulativeProb = 0.0;
		int count = 0;
		
		for (Double prob : sortedProbs) {
			cumulativeProb += prob;
			count++;
			
			// 当累积概率达到阈值时停止
			if (cumulativeProb >= threshold) {
				break;
			}
		}
		
		return count;
	}
	
	/**
	 * 基于最小概率阈值确定推荐数量
	 * 
	 * @param sortedProbs 按概率降序排列的主题概率列表
	 * @param threshold 最小概率阈值
	 * @return 推荐数量
	 */
	private int getRecommendationCountByMinThreshold(List<Double> sortedProbs, double threshold) {
		int count = 0;
		
		for (Double prob : sortedProbs) {
			// 当概率低于阈值时停止
			if (prob < threshold) {
				break;
			}
			count++;
		}
		
		return count;
	}
	
	/**
	 * 基于肘部法则确定推荐数量
	 * 
	 * @param sortedProbs 按概率降序排列的主题概率列表
	 * @return 推荐数量
	 */
	private int getRecommendationCountByElbowMethod(List<Double> sortedProbs) {
		if (sortedProbs.size() <= 2) {
			return sortedProbs.size();
		}
		
		// 计算相邻概率的差值
		List<Double> differences = new ArrayList<>();
		for (int i = 0; i < sortedProbs.size() - 1; i++) {
			differences.add(sortedProbs.get(i) - sortedProbs.get(i + 1));
		}
		
		// 找到差值最大的点，即"肘部"
		double maxDifference = -1;
		int elbowIndex = 0;
		
		for (int i = 0; i < differences.size(); i++) {
			if (differences.get(i) > maxDifference) {
				maxDifference = differences.get(i);
				elbowIndex = i;
			}
		}
		
		// 肘部点之后的主题可以认为贡献较小
		return elbowIndex + 1;
	}
	
	// 根据病种名称确定topK值
	private int determineTopKForSearch(String diseaseName) {
		if (diseaseName == null || diseaseName.trim().isEmpty()) {
			return DEFAULT_TOPK;
		}

		// 直接返回默认值，因为我们现在使用智能计算
		return DEFAULT_TOPK;
	}


}