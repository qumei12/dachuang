package com.wangyan.index;

import static org.junit.Assert.*;

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

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;

import dbhelper.DBSearch;
import javabean.API;
import javabean.Mashup;

public class IndexUtil {
	public void createIndex(Map<Integer, String> mashupIndex_Name){
		Directory directory = null;
		IndexWriter writer = null;
		try {
			directory = FSDirectory.open(new File("index"));

			IndexWriterConfig iwc = new IndexWriterConfig(Version.LUCENE_35	, new StandardAnalyzer(Version.LUCENE_35));

			writer = new IndexWriter(directory, iwc);

			Document document = null;

			Set<Integer> key = mashupIndex_Name.keySet();
			Iterator<Integer> it = key.iterator();

			while (it.hasNext()) {
				document = new Document();
				Integer integer = (Integer) it.next();
				document.add(new Field("id", integer + "", Field.Store.YES, Field.Index.NOT_ANALYZED_NO_NORMS));

				String name = mashupIndex_Name.get(integer);
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

	public void query(String str, int top_k,
					  Map<Integer, String> mashupIndex_Name, Map<Integer,Integer> mashupIndex_ID,
					  Map<Integer,String> apiIndex_Name, Map<Integer, Integer> apiIndex_ID,
					  Map<Integer, List<Integer>> mashupWordsBag, Map<Integer, List<Integer>> apiWordsBag,
					  List<API> apiList){

		try {
			// 精确匹配：只找完全匹配的mashup
			int targetMashupId = -1;
			for(Map.Entry<Integer, String> entry : mashupIndex_Name.entrySet()){
				if(entry.getValue().equalsIgnoreCase(str)){
					targetMashupId = entry.getKey();
					break;
				}
			}

			if(targetMashupId == -1){
				System.out.println("未找到精确匹配的mashup: " + str);
				return;
			}

			// 获取该mashup的多个兴趣（默认获取前3个最相关的兴趣）
			List<InterestScore> interests = getTopInterests(targetMashupId, mashupWordsBag, 3);

			System.out.println("找到 " + interests.size() + " 个相关兴趣:");
			for(InterestScore interestScore : interests) {
				System.out.println(mashupIndex_Name.get(targetMashupId) + " 属于兴趣" + interestScore.interestId +
						" (排名位置: " + interestScore.position + ")");
			}

			// 基于多个兴趣推荐API
			recommendAPIsFromMultipleInterests(interests, top_k, apiWordsBag, apiIndex_ID, apiList);

			System.out.println("----------------API----------------");
			for(int i = 0; i < apiList.size(); i++){
				System.out.println(apiList.get(i).getN_ID() + "--" + apiList.get(i).getC_NAME());
			}

		} catch (Exception e) {
			e.printStackTrace();
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

	// 获取前N个最相关的兴趣
	private List<InterestScore> getTopInterests(int id, Map<Integer, List<Integer>> mashupWordsBag, int topN){
		List<InterestScore> interestScores = new ArrayList<>();

		// 遍历所有兴趣，计算目标mashup在每个兴趣中的排名
		for(Map.Entry<Integer, List<Integer>> entry : mashupWordsBag.entrySet()){
			int interestId = entry.getKey();
			List<Integer> list = entry.getValue();
			int position = list.indexOf(id);

			// 如果该mashup在该兴趣中存在
			if(position != -1){
				interestScores.add(new InterestScore(interestId, position));
			}
		}

		// 按排名位置排序（位置越小越靠前）
		Collections.sort(interestScores, new Comparator<InterestScore>() {
			@Override
			public int compare(InterestScore o1, InterestScore o2) {
				return Integer.compare(o1.position, o2.position);
			}
		});

		// 返回前topN个兴趣
		return interestScores.subList(0, Math.min(topN, interestScores.size()));
	}

	// 基于多个兴趣推荐API
	private void recommendAPIsFromMultipleInterests(List<InterestScore> interests, int top_k,
													Map<Integer, List<Integer>> apiWordsBag,
													Map<Integer, Integer> apiIndex_ID,
													List<API> apiList) {
		if(interests.isEmpty()) {
			return;
		}

		// 使用加权方式：排名越靠前的兴趣权重越高
		Map<Integer, Double> apiScores = new HashMap<>();

		for(int i = 0; i < interests.size(); i++) {
			InterestScore interestScore = interests.get(i);
			int interestId = interestScore.interestId;
			int position = interestScore.position;

			// 计算权重：排名越靠前权重越高
			double weight = 1.0 / (position + 1);

			// 获取该兴趣相关的API列表
			List<Integer> apiListForInterest = apiWordsBag.get(interestId);
			if(apiListForInterest != null) {
				// 为每个API分配分数（排名越靠前分数越高）
				for(int j = 0; j < apiListForInterest.size(); j++) {
					int apiIndex = apiListForInterest.get(j);
					double apiScore = weight * (1.0 / (j + 1)); // API排名权重

					// 累加分数
					apiScores.put(apiIndex, apiScores.getOrDefault(apiIndex, 0.0) + apiScore);
				}
			}
		}

		// 按分数排序API
		List<Map.Entry<Integer, Double>> sortedApis = new ArrayList<>(apiScores.entrySet());
		Collections.sort(sortedApis, new Comparator<Map.Entry<Integer, Double>>() {
			@Override
			public int compare(Map.Entry<Integer, Double> o1, Map.Entry<Integer, Double> o2) {
				return o2.getValue().compareTo(o1.getValue()); // 降序排列
			}
		});

		// 获取前top_k个API
		DBSearch dbSearch = new DBSearch();
		int count = 0;
		for(Map.Entry<Integer, Double> entry : sortedApis) {
			if(count >= top_k) break;

			int apiIndex = entry.getKey();
			if(apiIndex_ID.containsKey(apiIndex)) {
				int apiId = apiIndex_ID.get(apiIndex);
				apiList.add(dbSearch.getApiById(apiId));
				count++;
			}
		}
	}

	// 保留原来的单兴趣方法以备不时之需
	private int getInterest(int id, Map<Integer, List<Integer>> mashupWordsBag){
		Set<Integer> key = mashupWordsBag.keySet();
		Iterator<Integer> it = key.iterator();

		int min = 100000;
		int interest = -1;

		while (it.hasNext()) {
			Integer integer = (Integer) it.next();
			List<Integer> list = mashupWordsBag.get(integer);
			int position = list.indexOf(id);
			if(position < min){
				min = position;
				interest = integer;
			}
		}

		return interest;
	}
}
