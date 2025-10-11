package com.wangyan.index;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;
import org.apache.lucene.analysis.standard.StandardAnalyzer;

import filehelper.CaseSupplyMatrixService;
import dbhelper.DBSearch;

import dbhelper.DBSearch;
import javabean.Supply;
import model.LDAModel;
import model.ModelTrainer;

public class IndexUtil {
	public void createIndex(Map<Integer, String> diseaseIndex_Name){
		Directory directory = null;
		IndexWriter writer = null;
		try {
			directory = FSDirectory.open(new File("index"));

			IndexWriterConfig iwc = new IndexWriterConfig(Version.LUCENE_35	, new StandardAnalyzer(Version.LUCENE_35));

			writer = new IndexWriter(directory, iwc);

			Document document = null;

			Set<Integer> key = diseaseIndex_Name.keySet();
			Iterator<Integer> it = key.iterator();

			while (it.hasNext()) {
				document = new Document();
				Integer integer = (Integer) it.next();
				document.add(new Field("id", integer + "", Field.Store.YES, Field.Index.NOT_ANALYZED_NO_NORMS));

				String name = diseaseIndex_Name.get(integer);
				document.add(new Field("name", name, Field.Store.NO, Field.Index.ANALYZED));

				writer.addDocument(document);
			}

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			if(writer != null){
				try {
					writer.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}


	// 兴趣得分类 - 改为包级私有（去掉private static）
	static class InterestScore {
		int interestId;
		int position;

		public InterestScore(int interestId, int position) {
			this.interestId = interestId;
			this.position = position;
		}
	}

	// 在IndexUtil类中添加以下方法
	/**
	 * 为指定disease推荐Supply，每个兴趣主题推荐一个最相关的Supply
	 * @param diseaseIndex disease索引
	 * @param interestCount 兴趣主题数量
	 * @param ldaModel 已训练的LDA模型
	 * @param supplyIndex_ID Supply索引到ID的映射
	 * @param diseaseId 疾病ID，用于获取该病种下所有病案的索引
	 * @return 推荐的Supply列表，每个兴趣一个Supply
	 */
	public List<Supply> recommendOneSupplyPerInterestByLDAModel(Integer diseaseIndex, int interestCount,
											LDAModel ldaModel, Map<Integer, Integer> supplyIndex_ID, 
											int diseaseId) {
	List<Supply> recommendedSupplys = new ArrayList<>();

	// 检查diseaseIndex是否为null
	if (diseaseIndex == null) {
		System.out.println("Disease索引为空");
		return recommendedSupplys;
	}

	// 确保diseaseIndex在有效范围内
	if (diseaseIndex >= ldaModel.getCaseAmount()) {
		System.out.println("Disease索引超出范围: " + diseaseIndex);
		return recommendedSupplys;
	}

	// 获取用户对各兴趣主题的概率
	// 使用病种下所有病案的平均主题分布
	double[] interestProbs = new double[ldaModel.getTopicAmount()];
	
	// 获取该病种下所有病案的索引
	List<Integer> caseIndexes = CaseSupplyMatrixService.getCaseIndexesByDiseaseId(diseaseId);
	
	if (caseIndexes != null && !caseIndexes.isEmpty()) {
		// 使用病种下所有病案的平均主题分布
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
		
		System.out.println("使用病种ID " + diseaseId + " 下 " + caseIndexes.size() + " 个病案的平均主题分布");
	} else {
		// 如果没有找到相关病案，回退到使用单个病案的分布
		for (int k = 0; k < ldaModel.getTopicAmount(); k++) {
			interestProbs[k] = ldaModel.getTheta()[diseaseIndex][k];
		}
		System.out.println("回退到使用单个病案的主题分布");
	}

	// 输出前10个主题及其概率，便于调试
	System.out.println("IndexUtil中病种的前10个兴趣主题:");
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

	for (int i = 0; i < Math.min(10, sortedIndices.size()); i++) {
		int topicIndex = sortedIndices.get(i);
		System.out.println("  主题 " + topicIndex + " - 概率: " + String.format("%.6f", interestProbs[topicIndex]));
	}

	// 对兴趣主题按概率排序
	List<Integer> interestIndices = sortedIndices; // 直接使用已排序的列表

	DBSearch dbSearch = new DBSearch();

	// 为每个top interest推荐一个最相关的Supply
	Set<Integer> selectedSupplys = new HashSet<>(); // 避免重复推荐同一个Supply

	// 限制循环次数以提高性能，但取消最大数量限制
	int actualInterestCount = Math.min(interestCount, interestIndices.size()); // 限制不超过主题总数
	
	System.out.println("准备处理 " + actualInterestCount + " 个兴趣主题");
	for (int i = 0; i < actualInterestCount; i++) {
		if (i >= interestIndices.size()) {
			break; // 防止索引越界
		}
		
		int interestId = interestIndices.get(i);
		System.out.println("为兴趣主题 " + interestId + " 选择推荐耗材 (第" + (i+1) + "个主题，概率: " + String.format("%.6f", interestProbs[interestId]) + ")");

		// 在该兴趣主题中找出最相关的Supply
		double maxProb = -1;
		int bestSupplyIndex = -1;

		// 添加调试代码：找出该主题下概率前10的耗材
		List<Integer> topSupplyIndices = new ArrayList<>();
		List<Double> topSupplyProbs = new ArrayList<>();
		
		for (int j = 0; j < ldaModel.getPhi()[interestId].length; j++) {
			double prob = ldaModel.getPhi()[interestId][j];
			if (topSupplyIndices.size() < 10) {
				topSupplyIndices.add(j);
				topSupplyProbs.add(prob);
				
				// 保持按概率降序排列
				for (int k = topSupplyProbs.size() - 1; k > 0; k--) {
					if (topSupplyProbs.get(k) > topSupplyProbs.get(k - 1)) {
						// 交换概率
						Double tempProb = topSupplyProbs.get(k);
						topSupplyProbs.set(k, topSupplyProbs.get(k - 1));
						topSupplyProbs.set(k - 1, tempProb);
						
						// 交换索引
						Integer tempIndex = topSupplyIndices.get(k);
						topSupplyIndices.set(k, topSupplyIndices.get(k - 1));
						topSupplyIndices.set(k - 1, tempIndex);
					} else {
						break;
					}
				}
			} else if (prob > topSupplyProbs.get(topSupplyProbs.size() - 1)) {
				topSupplyProbs.set(topSupplyProbs.size() - 1, prob);
				topSupplyIndices.set(topSupplyIndices.size() - 1, j);
				
				// 重新排序
				for (int k = topSupplyProbs.size() - 1; k > 0; k--) {
					if (topSupplyProbs.get(k) > topSupplyProbs.get(k - 1)) {
						// 交换概率
						Double tempProb = topSupplyProbs.get(k);
						topSupplyProbs.set(k, topSupplyProbs.get(k - 1));
						topSupplyProbs.set(k - 1, tempProb);
						
						// 交换索引
						Integer tempIndex = topSupplyIndices.get(k);
						topSupplyIndices.set(k, topSupplyIndices.get(k - 1));
						topSupplyIndices.set(k - 1, tempIndex);
					} else {
						break;
					}
				}
			}
		}
		
		// 输出前10个耗材
		System.out.println("  主题 " + interestId + " 下前10个耗材:");
		for (int idx = 0; idx < topSupplyIndices.size(); idx++) {
			System.out.println("    " + (idx + 1) + ". 索引: " + topSupplyIndices.get(idx) + 
				", 概率: " + String.format("%.10f", topSupplyProbs.get(idx)));
		}

		// 限制搜索范围以提高性能
		int searchLimit = ldaModel.getSupplyAmount(); // 使用所有耗材进行搜索
		for (int j = 0; j < searchLimit; j++) {
			// 确保不重复推荐已选的Supply
			if (!selectedSupplys.contains(j) && ldaModel.getPhi()[interestId][j] > maxProb) {
				maxProb = ldaModel.getPhi()[interestId][j];
				bestSupplyIndex = j;
			}
		}
		
		System.out.println("  主题 " + interestId + " 下最终选择的耗材索引: " + bestSupplyIndex + ", 概率: " + maxProb);

		// 将Supply索引转换为实际Supply对象
		if (bestSupplyIndex != -1 && supplyIndex_ID.containsKey(bestSupplyIndex)) {
			int supplyId = supplyIndex_ID.get(bestSupplyIndex);
			Supply supply = dbSearch.getSupplyById(supplyId);
			if (supply != null && supply.getID() > 0) {
				System.out.println("  为兴趣主题 " + interestId + " 推荐耗材: " + supply.getNAME());
				recommendedSupplys.add(supply);
				selectedSupplys.add(bestSupplyIndex); // 记录已选Supply避免重复
			}
		} else {
			System.out.println("  未能为主题 " + interestId + " 找到合适的耗材");
		}
	}

	return recommendedSupplys;
	}

}