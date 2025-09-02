package com.wangyan.experiment;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.Test;

import allthings.GetMA;
import allthings.GetRelation;
import model.LDAModel;

public class Experiment {
	// LDA算法模型
	LDAModel ldaModel = null;
	// 物质扩散算法结果矩阵
	double[][] apiRelation = null;

	int top_k = 10;
	int[][] recommand = null;

	Map<Integer, Integer> apiSequ = null;
	Map<Integer, List<Integer>> mashupWordsBag = null;
	Map<Integer, List<Integer>> apiWordsBag = null;
	
	
	Map<Integer, Integer> mashupIndex_ID = null;
	Map<Integer, String> mashupIndex_Name = null;
	
	Map<Integer, Integer> apiIndex_id = null;
	Map<Integer, String> apiIndex_Name = null;

	
	
	public Experiment() {
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

		if (apiRelation == null) {
			apiRelation = new GetRelation().getSimilarityByMD_REA();
			System.out.println("物质扩散结果生成");
		}

	}

	@Test
	public void paramPredict() {

		double max = 0;
		int count = 0;

		for (int i = 0; i < apiRelation.length; i++) {
			for (int j = 0; j < i - 1; j++) {
				if (i == j) {
					continue;
				}
				if (apiRelation[i][j] > max) {
					max = apiRelation[i][j];
				}
				count++;
			}
		}

		System.out.println(max);

		int count_jige = 0;
		for (int i = 0; i < apiRelation.length; i++) {
			for (int j = 0; j < i - 1; j++) {
				if (i == j) {
					continue;
				}
				if (apiRelation[i][j] > max * 0.1) {
					count_jige++;
				}
			}
		}

		System.out.println("及格率:" + count_jige * 1.0 / count);
	}

	@Test
	public void testFiveStage() {
		
		Map<Integer, List<List<Integer>>> recommandMap = new HashMap<>();
		
		Set<Integer> key = apiWordsBag.keySet();
		Iterator<Integer> it = key.iterator();
		long start = System.currentTimeMillis();
		while (it.hasNext()) {
			Integer integer = (Integer) it.next();
			List<List<Integer>> recommandList = new ArrayList<>();
			List<Integer> apiIndexList = apiWordsBag.get(integer);


			for (int i = 0; i < top_k; i++) {
				int r1 = apiIndexList.get(i).intValue();
				
				int[] r2 = getTopMA(r1, top_k);
				
				for(int j = 0; j < r2.length; j++){
					int[] r3 = getTopMA(r2[j], top_k);
					
					for(int k = 0;k < r3.length; k++){
						List<Integer> list = new ArrayList<>();
						list.add(r1);
						list.add(r2[j]);
						list.add(r3[k]);
						recommandList.add(list);
					}
				}
			}

			recommandMap.put(integer, recommandList);
			
		}
		long end = System.currentTimeMillis();
		//进行指标测试
		fiveStageAccuracy(recommandMap);
		fiveStageRecallRate(recommandMap);
		fiveStageHamming(recommandMap);
		long time = (end - start);
		System.out.println(time);
		
		//SimpleDateFormat formatter = new SimpleDateFormat("mm分ss.sssssssss秒"); 
		//String formatDate = formatter.format(new Date());
		//System.out.println(formatDate);
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
		
		for(int i = 0; i < top;i++){
			recom[i] = arr_index[i];
		}
		
		return recom;
		
	}

	@Test
	public void testThreeStage() {
		Map<Integer, List<Integer>> recommandMap = new HashMap<>();
		
		Set<Integer> key = apiWordsBag.keySet();
		Iterator<Integer> it = key.iterator();

		long start = System.currentTimeMillis();
		
		//SimpleDateFormat formatter = new SimpleDateFormat("hh点mm分ss.sssssssss秒"); 
		//String formatDate = formatter.format(new Date());
		//System.out.println(formatDate);
		
		while (it.hasNext()) {
			Integer integer = (Integer) it.next();
			List<Integer> list = new ArrayList<>();
			List<Integer> apiIndexList = apiWordsBag.get(integer);
		
			for(int i = 0; i < top_k * top_k * top_k; i++){
				if(apiIndexList.size() < i){
					list.add(apiIndexList.get(i));
				} else {
					break;
				}
			}
			
			recommandMap.put(integer, list);
		}
		try {
			Thread.sleep(3);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		long end = System.currentTimeMillis();
		
		//指标测算
		threeStageAccuracy(recommandMap);
		threeStageRecallRate(recommandMap);
		threeStageHamming(recommandMap);
		long time = (end - start);
		System.out.println(time);
		//SimpleDateFormat formatter2 = new SimpleDateFormat("hh点mm分ss.sssssssss秒"); 
		//String formatDate2 = formatter2.format(new Date());
		//System.out.println(formatDate2);
	}
	
	
	/**
	 * 三步推荐精确度计算
	 * @param map
	 */
	private void threeStageAccuracy(Map<Integer, List<Integer>> map){
		
		Set<Integer> key = map.keySet();
		Iterator<Integer> it = key.iterator();
		int total = 0;
		int total_hit = 0;
		
		while (it.hasNext()) {
			Integer integer = (Integer) it.next();
			
			int standardApi = apiWordsBag.get(integer).get(0).intValue();
			List<Integer> list = map.get(integer);
			
			int hit = 0;
			
			for(int i = 0;i < list.size(); i++){
				int api = list.get(i);
				
				if(apiRelation[standardApi][api] > 0.001){
					hit++;
				}
			}
			
			total += list.size();
			total_hit += hit;
			
			//System.out.println("Interest--" + integer + ":" + list.size() + ";Hit:" + hit);
		}
		double accuracy = total_hit * 1.0 / total;
		NumberFormat nf = NumberFormat.getPercentInstance();
		nf.setMinimumFractionDigits(4);
		System.out.println(nf.format(accuracy));
	}

	/**
	 * 五层推荐精确度测量
	 * @param map
	 */
	private void fiveStageAccuracy(Map<Integer, List<List<Integer>>> map) {
		
		Set<Integer> key = map.keySet();
		Iterator<Integer> it = key.iterator();
		int total = 0;
		int total_hit = 0;
		
		while (it.hasNext()) {
			Integer integer = (Integer) it.next();
			
			int standardApi = apiWordsBag.get(integer).get(0).intValue();
			
			List<List<Integer>> recommandList = map.get(integer);
			
			int hit = 0;
			
			for(int i = 0;i < recommandList.size();i++){
				List<Integer> list = recommandList.get(i);
				
				double grade = 0;
				for(int j = 0; j < list.size(); j++){
					int api = list.get(j).intValue();
					grade += apiRelation[standardApi][api] * 1.0 / 3;
				}
				if(grade >= 0.1){
					hit++;
				}
			}
			
			total_hit += hit;
			total += recommandList.size();
			//System.out.println("Interest--" + integer + ":" + recommandList.size() + ";Hit:" + hit);
		}
		double accuracy = total_hit * 1.0 / total;
		NumberFormat nf = NumberFormat.getPercentInstance();
		nf.setMinimumFractionDigits(4);
		System.out.println(nf.format(accuracy));
		
	}
	
	/**
	 * 三层推荐的召回率计算
	 * @param map
	 */
	private void threeStageRecallRate(Map<Integer, List<Integer>> map){
		Set<Integer> key = map.keySet();
		Iterator<Integer> it = key.iterator();
		int total = 0;
		int total_hit = 0;
		
		while (it.hasNext()) {
			Integer integer = (Integer) it.next();
			
			int standardApi = apiWordsBag.get(integer).get(0).intValue();
		
			Set<Integer> allNotZero = new HashSet<>();
			for(int i = 0; i < apiRelation[standardApi].length; i++){
				
				if(apiRelation[standardApi][i] > 0){
					allNotZero.add(i);
				}
			}
			total += allNotZero.size();
			
			int hit = 0;
			
			List<Integer> list = map.get(integer);
			
			for(int i = 0; i < list.size(); i++){
				if(allNotZero.contains(list.get(i))){
					hit++;
					allNotZero.remove(list.get(i));
				}
			}
			
			total_hit += hit;
		}
		double recall = total_hit * 1.0 / total;
		NumberFormat nf = NumberFormat.getPercentInstance();
		nf.setMinimumFractionDigits(4);
		System.out.println(nf.format(recall));
	}
	
	/**
	 * 五层推荐的召回率计算
	 * @param map
	 */
	private void fiveStageRecallRate(Map<Integer, List<List<Integer>>> map){
		Set<Integer> key = map.keySet();
		Iterator<Integer> it = key.iterator();
		int total = 0;
		int total_hit = 0;
		
		while (it.hasNext()) {
			Integer integer = (Integer) it.next();
			
			int standardApi = apiWordsBag.get(integer).get(0).intValue();
			
			Set<Integer> allNotZero = new HashSet<>();
			for(int i = 0; i < apiRelation[standardApi].length; i++){
				
				if(apiRelation[standardApi][i] > 0){
					allNotZero.add(i);
				}
			}
			
			total += allNotZero.size();
			int hit = 0;
			
			List<List<Integer>> recommandList = map.get(integer);
			for(int i = 0; i < recommandList.size();i++){
				List<Integer> list = recommandList.get(i);
				
				for(int j = 0; j < list.size(); j++){
					if(allNotZero.contains(list.get(j))){
						hit++;
						allNotZero.remove(list.get(j));
					}
				}
			}
			
			total_hit += hit;
		}
		double recall = total_hit * 1.0 / total;
		NumberFormat nf = NumberFormat.getPercentInstance();
		nf.setMinimumFractionDigits(4);
		System.out.println(nf.format(recall));
	}
	
	/**
	 * 五层推荐海明距离测量
	 * @param map
	 */
	private void fiveStageHamming(Map<Integer, List<List<Integer>>> map){
		
		Map<Integer, List<Integer>> mashupMap = ldaModel.getMashupWordsBag();
		
		Set<Integer> mashupMapKey = mashupMap.keySet();
		
		Iterator<Integer> it = mashupMapKey.iterator();
		
		int hamming_total = 0;
		int total = 0;
		
		while (it.hasNext()) {
			Integer integer = (Integer) it.next();
			int mashupIndex = mashupMap.get(integer).get(0).intValue();
			
			int[][] MA = new GetMA().getArrayMA();
			
			int[] apiArray = MA[mashupIndex];
			
			List<Integer> standardMashup = new ArrayList<>();
			
			for(int i = 0; i < apiArray.length; i++){
				if (apiArray[i] == 1) {
					standardMashup.add(i);
				}
			}
			
			List<List<Integer>> recommandList = map.get(integer);
			
			for(int i = 0; i < recommandList.size(); i++){
				List<Integer> list = recommandList.get(i);
				int hamming = 0;
				for(int j = 0; j < list.size(); j++){
					for(int k = 0; k < standardMashup.size(); k++){
						if(standardMashup.get(k) == list.get(j)){
							hamming++;
							break;
						}
					}
				}
				
				hamming_total += hamming;
			}
			
			total += recommandList.size();
			
		}
		//System.out.println(hamming_total + "***" + total);
		double hamming_aver = hamming_total * 1.0 / total;
		int value_int = (int) (hamming_aver * 1000);
		double value_double = value_int / 1000.0;
		
		System.out.println(value_double);
	}
	
	
	private void threeStageHamming(Map<Integer, List<Integer>> map){
		Map<Integer, List<Integer>> mashupMap = ldaModel.getMashupWordsBag();
		
		Set<Integer> mashupMapKey = mashupMap.keySet();
		
		Iterator<Integer> it = mashupMapKey.iterator();
		
		int hamming_total = 0;
		int total = 0;
		
		while (it.hasNext()) {
			Integer integer = (Integer) it.next();
			int mashupIndex = mashupMap.get(integer).get(0).intValue();
			
			int[][] MA = new GetMA().getArrayMA();
			
			int[] apiArray = MA[mashupIndex];
			
			List<Integer> standardMashup = new ArrayList<>();
			
			for(int i = 0; i < apiArray.length; i++){
				if (apiArray[i] == 1) {
					standardMashup.add(i);
				}
			}
			
			List<Integer> originList = map.get(integer);
			
			List<List<Integer>> recommandList = new ArrayList<>();
			
			List<Integer> subList = null;
			for(int i = 0; i < originList.size(); i++){
				//if(i % 3 == 0){
					subList = new ArrayList<>();
				//}
				
				subList.add(originList.get(i));
				
				//if (i % 3 == 2) {
				recommandList.add(subList);
				//}
			}
			
			//System.out.println(recommandList.size());
			
			
			for(int i = 0; i < recommandList.size(); i++){
				List<Integer> list = recommandList.get(i);
				int hamming = 0;
				for(int j = 0; j < list.size(); j++){
					for(int k = 0; k < standardMashup.size(); k++){
						if(standardMashup.get(k) == list.get(j)){
							hamming++;
							break;
						}
					}
				}
				
				hamming_total += hamming;
			}
			
			total += recommandList.size();
			
		}
		//System.out.println(hamming_total + "***" + total);
		double hamming_aver = hamming_total * 1.0 / total;
		int value_int = (int) (hamming_aver * 1000);
		double value_double = value_int / 1000.0;
		
		System.out.println(value_double);
			
	}
	
}
