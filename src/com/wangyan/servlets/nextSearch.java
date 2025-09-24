package com.wangyan.servlets;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
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
import javax.servlet.http.HttpSession;

import com.wangyan.index.SupplyMap;

import dbhelper.DBSearch;
import javabean.Supply;
import model.LDAModel;
import model.ModelTrainer;

/**
 * Servlet implementation class nextSearch
 */
public class nextSearch extends HttpServlet {
	private static final long serialVersionUID = 1L;
	
	private static LDAModel ldaModel = null; // 添加LDA模型实例

	double[][] supplyRelation = null;

	int top_k = 3;
	

	Map<Integer, Integer> supplyIndex_ID = null;
	Map<Integer, String> supplyIndex_Name = null;

	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public nextSearch() {
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
		}
		
		// 初始化索引映射
		supplyIndex_ID = new HashMap<>();
		supplyIndex_Name = new HashMap<>();
		new SupplyMap().setMap(supplyIndex_ID, supplyIndex_Name);
	}

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		int supplyId = Integer.parseInt(request.getParameter("id"));
		String diseaseIndexStr = request.getParameter("diseaseIndex");
		String interestIdStr = request.getParameter("interestId");
		String rowIndexStr = request.getParameter("rowIndex");
		
		int diseaseIndex = -1;
		int interestId = -1;
		int rowIndex = -1;
		
		if (diseaseIndexStr != null && !diseaseIndexStr.isEmpty() && !diseaseIndexStr.equals("-1")) {
			diseaseIndex = Integer.parseInt(diseaseIndexStr);
		}
		
		if (interestIdStr != null && !interestIdStr.isEmpty() && !interestIdStr.equals("-1")) {
			interestId = Integer.parseInt(interestIdStr);
		}
		
		if (rowIndexStr != null && !rowIndexStr.isEmpty() && !rowIndexStr.equals("-1")) {
			rowIndex = Integer.parseInt(rowIndexStr);
		}
		
		System.out.println("接收到的参数 - 耗材ID: " + supplyId + ", 病种索引: " + diseaseIndex + ", 兴趣主题ID: " + interestId + ", 行号: " + rowIndex);
		
		// 添加调试信息
		if (diseaseIndex >= 0) {
			int[] topInterests = getTopInterest(diseaseIndex, top_k);
			System.out.print("病种索引 " + diseaseIndex + " 的Top " + top_k + " 兴趣主题: ");
			for (int i = 0; i < topInterests.length; i++) {
				System.out.print(topInterests[i] + " ");
			}
			System.out.println();
		}
		
		try {
			// 获取当前耗材信息
			DBSearch dbs = new DBSearch();
			Supply currentSupply = dbs.getSupplyById(supplyId);
			
			// 为当前耗材推荐相关的其他耗材
			ArrayList<Supply> recommandSupplyList = new ArrayList<>();
			
			// 根据是否提供了兴趣主题ID来决定推荐策略
			// 优先使用明确指定的兴趣主题ID
			if(interestId >= 0) {
				// 使用指定的兴趣主题进行推荐
				System.out.println("使用指定的兴趣主题进行推荐: " + interestId);
				recommandSupplyList = recommandByInterest(interestId, supplyId);
			} else if (rowIndex >= 0 && diseaseIndex >= 0) {
				// 使用行号和病种索引确定兴趣主题进行推荐
				System.out.println("使用行号和病种索引确定兴趣主题进行推荐，行号: " + rowIndex);
				// 获取病种的topK兴趣主题（使用与Search.java中相同的排序方法）
				// 从会话中获取在Search.java中计算好的兴趣主题排序
				HttpSession session = request.getSession();
				@SuppressWarnings("unchecked")
				List<Integer> diseaseInterestOrder = (List<Integer>) session.getAttribute("diseaseInterestOrder");
				
				if (diseaseInterestOrder != null && rowIndex < diseaseInterestOrder.size()) {
					interestId = diseaseInterestOrder.get(rowIndex);
					System.out.println("确定兴趣主题: " + interestId);
					recommandSupplyList = recommandByInterest(interestId, supplyId);
				} else {
					// 如果行号超出范围，使用默认推荐方法
					System.out.println("行号超出兴趣主题范围，使用默认推荐方法");
					recommandSupplyList = recommandDefault(supplyId);
				}
			} else if (diseaseIndex >= 0) {
				// 使用病种的TopK兴趣主题进行推荐
				System.out.println("使用病种的TopK兴趣主题进行推荐");
				recommandSupplyList = recommand(diseaseIndex, supplyId);
			} else {
				// 默认推荐方法
				System.out.println("使用默认推荐方法");
				recommandSupplyList = recommandDefault(supplyId);
			}
			
			// 传递数据到JSP页面
			request.setAttribute("currentSupply", currentSupply);
			request.setAttribute("recommandSupplyList", recommandSupplyList);
			request.setAttribute("supplyIndex_ID", supplyIndex_ID);
			request.setAttribute("supplyIndex_Name", supplyIndex_Name);
			// 传递兴趣主题ID，用于继续推荐
			request.setAttribute("interestId", interestId >= 0 ? interestId : -1);
			request.setAttribute("diseaseIndex", diseaseIndex >= 0 ? diseaseIndex : -1);
			
			// 传递被点击的供应索引
			String clickedIndexStr = request.getParameter("clickedIndex");
			if (clickedIndexStr != null && !clickedIndexStr.isEmpty()) {
				request.setAttribute("clickedIndex", clickedIndexStr);
			}
			
			request.getRequestDispatcher("nextSearchResult.jsp").forward(request, response);
			
		} catch(Exception e) {
			e.printStackTrace();
			request.setAttribute("error", "系统错误: " + e.getMessage());
			request.getRequestDispatcher("error.jsp").forward(request, response);
		}
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		doGet(request, response);
	}
	
	/**
	 * 根据病种的TopK兴趣主题推荐耗材
	 * @param diseaseIndex 病种索引
	 * @param excludeSupplyId 需要排除的耗材ID
	 * @return 推荐的耗材列表
	 */
	private ArrayList<Supply> recommand(int diseaseIndex, int excludeSupplyId) {
		ArrayList<Supply> recommandSupplyList = new ArrayList<>();
		
		try {
			// 获取病种的TopK兴趣主题
			int[] topInterests = getTopInterest(diseaseIndex, top_k);
			
			// 为每个兴趣主题推荐一个耗材
			Set<Integer> addedSupplyIds = new HashSet<>(); // 避免重复添加
			for(int interestIndex : topInterests) {
				int[] recommandSupplies = getTopSupply(interestIndex, 10); // 获取该兴趣主题下的Top10耗材
				
				// 选择第一个不是excludeSupplyId且未添加过的耗材
				for(int supplyIndex : recommandSupplies) {
					int supplyId = supplyIndex_ID.get(supplyIndex);
					if(supplyId != excludeSupplyId && !addedSupplyIds.contains(supplyId)) {
						Supply supply = new DBSearch().getSupplyById(supplyId);
						recommandSupplyList.add(supply);
						addedSupplyIds.add(supplyId);
						System.out.println("为兴趣主题 " + interestIndex + " 推荐耗材: " + supply.getNAME());
						break;
					}
				}
			}
		} catch(Exception e) {
			e.printStackTrace();
		}
		
		return recommandSupplyList;
	}
	
	/**
	 * 根据指定的兴趣主题推荐耗材
	 * @param interestId 兴趣主题ID
	 * @param excludeSupplyId 需要排除的耗材ID
	 * @return 推荐的耗材列表
	 */
	private ArrayList<Supply> recommandByInterest(int interestId, int excludeSupplyId) {
		ArrayList<Supply> recommandSupplyList = new ArrayList<>();
		
		try {
			int[] recommandSupplies = getTopSupply(interestId, 10); // 获取该兴趣主题下的Top10耗材
			
			// 选择前几个不是excludeSupplyId的耗材
			Set<Integer> addedSupplyIds = new HashSet<>();
			for(int supplyIndex : recommandSupplies) {
				int supplyId = supplyIndex_ID.get(supplyIndex);
				if(supplyId != excludeSupplyId && !addedSupplyIds.contains(supplyId)) {
					Supply supply = new DBSearch().getSupplyById(supplyId);
					recommandSupplyList.add(supply);
					addedSupplyIds.add(supplyId);
					System.out.println("为兴趣主题 " + interestId + " 推荐耗材: " + supply.getNAME());
					
					// 最多推荐3个耗材
					if(recommandSupplyList.size() >= 3) {
						break;
					}
				}
			}
		} catch(Exception e) {
			e.printStackTrace();
		}
		
		return recommandSupplyList;
	}
	
	/**
	 * 默认推荐方法
	 * @param excludeSupplyId 需要排除的耗材ID
	 * @return 推荐的耗材列表
	 */
	private ArrayList<Supply> recommandDefault(int excludeSupplyId) {
		ArrayList<Supply> recommandSupplyList = new ArrayList<>();
		
		try {
			// 获取供应关系矩阵
			if(supplyRelation == null) {
				supplyRelation = new double[supplyIndex_ID.size()][supplyIndex_ID.size()];
				// 初始化供应关系矩阵（这里可以基于LDA模型或其他方法计算相似度）
				for(int i = 0; i < supplyIndex_ID.size(); i++) {
					for(int j = 0; j < supplyIndex_ID.size(); j++) {
						if(i == j) {
							supplyRelation[i][j] = 0; // 自身相似度为0
						} else {
							// 简单的默认相似度计算（可以根据需要改进）
							supplyRelation[i][j] = 1.0 / (1.0 + Math.abs(i - j));
						}
					}
				}
			}
			
			// 查找当前耗材的索引
			int currentSupplyIndex = -1;
			for(Map.Entry<Integer, Integer> entry : supplyIndex_ID.entrySet()) {
				if(entry.getValue() == excludeSupplyId) {
					currentSupplyIndex = entry.getKey();
					break;
				}
			}
			
			if(currentSupplyIndex >= 0) {
				// 基于相似度排序并推荐
				List<SupplySimilarity> similarities = new ArrayList<>();
				for(int i = 0; i < supplyIndex_ID.size(); i++) {
					if(i != currentSupplyIndex) {
						similarities.add(new SupplySimilarity(i, supplyRelation[currentSupplyIndex][i]));
					}
				}
				
				// 按相似度降序排序
				Collections.sort(similarities, new Comparator<SupplySimilarity>() {
					@Override
					public int compare(SupplySimilarity s1, SupplySimilarity s2) {
						return Double.compare(s2.similarity, s1.similarity);
					}
				});
				
				// 选择前几个推荐
				int count = 0;
				for(SupplySimilarity similarity : similarities) {
					if(count >= 3) break; // 最多推荐3个
					
					int supplyId = supplyIndex_ID.get(similarity.index);
					Supply supply = new DBSearch().getSupplyById(supplyId);
					if(supply != null && supply.getID() > 0) {
						recommandSupplyList.add(supply);
						count++;
						System.out.println("默认推荐耗材: " + supply.getNAME() + " (相似度: " + similarity.similarity + ")");
					}
				}
			}
		} catch(Exception e) {
			e.printStackTrace();
		}
		
		return recommandSupplyList;
	}
	
	/**
	 * 获取病种的TopK兴趣主题
	 * @param index 病种索引
	 * @param top TopK数量
	 * @return 兴趣主题索引数组
	 */
	private int[] getTopInterest(int index, int top){
		int[] recom = new int[top];
		
		Double[] arr = new Double[ldaModel.getTheta()[index].length];
		int[] arr_index = new int[ldaModel.getTheta()[index].length];
		
		for(int i = 0; i < ldaModel.getTheta()[index].length; i++){
			arr_index[i] = i;
			arr[i] = ldaModel.getTheta()[index][i];
		}
		
		// 按照概率从高到低排序
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
		
		for(int i = 0; i < Math.min(top, arr_index.length); i++){
			recom[i] = arr_index[i];
		}
		
		return recom;
	}
	
	/**
	 * 获取兴趣主题下的TopK耗材
	 * @param index 兴趣主题索引
	 * @param top TopK数量
	 * @return 耗材索引数组
	 */
	private int[] getTopSupply(int index, int top){
		int[] recom = new int[top];
		
		double[] arr = new double[ldaModel.getPhi()[index].length];
		int[] arr_index = new int[ldaModel.getPhi()[index].length];
		
		for(int i = 0; i < ldaModel.getPhi()[index].length; i++){
			arr_index[i] = i;
			arr[i] = ldaModel.getPhi()[index][i];
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
		
		for(int i = 0; i < Math.min(top, arr_index.length); i++){
			recom[i] = arr_index[i];
		}
		
		return recom;
	}
	
	// 供应相似度类
	private static class SupplySimilarity {
		int index;
		double similarity;
		
		SupplySimilarity(int index, double similarity) {
			this.index = index;
			this.similarity = similarity;
		}
	}
}