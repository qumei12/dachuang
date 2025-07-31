package com.wangyan.experiment;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
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
import org.junit.Test;

import com.wangyan.index.APIMap;
import com.wangyan.index.MashupMap;

import allthings.GetRelation;
import dbhelper.DBSearch;
import javabean.API;
import javabean.Mashup;
import model.LDAModel;

public class Product {
		// LDA算法模型
		LDAModel ldaModel = null;
		// 物质扩散算法结果矩阵
		double[][] apiRelation = null;

		int top_k = 3;
		int[][] recommand = null;

		Map<Integer, Integer> apiSequ = null;
		Map<Integer, List<Integer>> mashupWordsBag = null;
		Map<Integer, List<Integer>> apiWordsBag = null;
		
		
		Map<Integer, Integer> mashupIndex_ID = null;
		Map<Integer, String> mashupIndex_Name = null;
		
		Map<Integer, Integer> apiIndex_ID = null;
		Map<Integer, String> apiIndex_Name = null;

		
		
		public Product() {
			if (ldaModel == null) {
				ldaModel = new LDAModel(top_k);
				//System.out.println("1 Initialize the model ...");
				ldaModel.initializeLDAModel();
				//System.out.println("2 Learning and Saving the model ...");
				ldaModel.inferenceModel();
				//System.out.println("LDAModel creation finished!");
				mashupWordsBag = ldaModel.getMashupWordsBag();
				apiWordsBag = ldaModel.getAPIWordsBag();
				//System.out.println("词袋子生成完毕");
			}

			if (apiRelation == null) {
				apiRelation = new GetRelation().getSimilarityByMD_REA();
				//System.out.println("物质扩散结果生成");
			}
			
			mashupIndex_ID = new HashMap<>();
			mashupIndex_Name = new HashMap<>();
			new MashupMap().setMap(mashupIndex_ID, mashupIndex_Name);
			
			apiIndex_ID = new HashMap<>();
			apiIndex_Name = new HashMap<>();
			new APIMap().setMap(apiIndex_ID, apiIndex_Name);

			//File file = new File("./index");
			//if(file.exists()){
				//file.delete();
			createIndex();
			//}
		}
		
		private void createIndex(){
			Directory directory = null;
			IndexWriter writer = null;
			try {
				directory = FSDirectory.open(new File("./index"));
				
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
		
		@Test
		public void testName() throws Exception {
			query("Mapping");
		}
		
		private void query(String str){
			Directory directory = null;
			
			
			try {
				directory = FSDirectory.open(new File("./index"));
				
				IndexReader reader = IndexReader.open(directory);
				
				IndexSearcher indexSearcher = new IndexSearcher(reader);
				
				QueryParser queryParser = new QueryParser(Version.LUCENE_35, "name", new StandardAnalyzer(Version.LUCENE_35));
				Query query = queryParser.parse(str);//搜索内容包含str的文档
				
				TopDocs topDocs = indexSearcher.search(query, 200);//显示10条
				
				ScoreDoc[] scoreDocs = topDocs.scoreDocs;//获取搜索结果的id数组
				
				List<Mashup> mashupList = new ArrayList<>();
				List<API> apiList = new ArrayList<>();
				
				Set<Integer> interestSet = new HashSet<>();
				
				for(ScoreDoc scoreDoc : scoreDocs){
					Document document = indexSearcher.doc(scoreDoc.doc);
					int id = Integer.parseInt(document.get("id"));
					int interest = getInterest(id);
					String string = mashupIndex_Name.get(id);
					System.out.println(string + "属于兴趣" + interest);
					interestSet.add(interest);
				}
				
				
				Iterator<Integer> iterator = interestSet.iterator();
				
				Set<Integer> apiIndexSet = new HashSet<>();
				
				DBSearch dbSearch = new DBSearch();
				while (iterator.hasNext()) {
					Integer interest = (Integer) iterator.next();
					//推荐该兴趣前Top-k个Mashup
					List<Integer> list_m = mashupWordsBag.get(interest);
					for(int i = 0; i < top_k; i++){
						int mashupIndex = list_m.get(i);
						int mashupId = mashupIndex_ID.get(mashupIndex);
						mashupList.add(dbSearch.getMashupById(mashupId));
					}
					
					List<Integer> list_a = apiWordsBag.get(interest);
					for(int i = 0; i < top_k; i++){
						int apiIndex = list_a.get(i);
						apiIndexSet.add(apiIndex);
						//int mashupId = mashupIndex_ID.get(mashupIndex);
						//mashupList.add(dbSearch.getMashupById(mashupId));
					}
				}
				
				Iterator<Integer> apiIndexSet_it = apiIndexSet.iterator();
				while (apiIndexSet_it.hasNext()) {
					Integer apiIndex = (Integer) apiIndexSet_it.next();
					int apiId = apiIndex_ID.get(apiIndex);
					apiList.add(dbSearch.getApiById(apiId));
					
				}
				
				System.out.println("----------------Mashup----------------");
				for(int i = 0; i < mashupList.size(); i++){
					System.out.println(mashupList.get(i).getN_ID() + "--" + mashupList.get(i).getC_NAME());
				}
				
				System.out.println("----------------API----------------");
				for(int i = 0; i < apiList.size(); i++){
					System.out.println(apiList.get(i).getN_ID() + "--" + apiList.get(i).getC_NAME());
				}
				
			}  	catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}	catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		private int getInterest(int id){
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
