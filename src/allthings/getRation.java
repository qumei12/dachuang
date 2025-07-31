package allthings;

import java.util.List;


import fileout.FileOut;

public class getRation {
	
	int[][] MA = null;
	double[][] similarity = null;
	
	private getRation(){
		GetMA getMA = new GetMA();
		MA = getMA.getArrayMA();
		getSimilarityByMD_REA(MA);
		FileOut.printMatrix(similarity, "api相似度");
	}
	
	public static getRation getInstance(){
		getRation gr = new getRation();
		return gr;
	}
	//@Test
	public void getAccuracy(List<Integer> origin, List<Integer> recom){
		double sim = 1;
		for(int i = 0; i < origin.size(); i++){
			int api_1 = origin.get(i); 
			for(int j = 0; j < recom.size(); j++){
				int api_2 = recom.get(j);
				double simi = similarity[api_1][api_2];
				if(simi == 0){
					sim *= 1E-5;
				} else {
					sim *= simi;
				}
			}
		}
		System.out.println(sim);
//		GetMA getMA = new GetMA();
//		int[][] MA = getMA.getArrayMA();
//		double[][] similarity = getSimilarityByMD_REA(getMA.getArrayMA());
		
		
	}
	
	//// 以下为物质扩散算法获取相似度矩阵
	private double[][] getSimilarityByMD_REA(int[][] mashupApiMatrix) {

		int mashupAmount = mashupApiMatrix.length;
		int apiAmount = mashupApiMatrix[0].length;
		
		double min = 1;
		
		System.out.println(mashupAmount+ "**");
		System.out.println(apiAmount+ "##");
		
		double[][] similarity = new double[apiAmount][apiAmount];

		for (int i = 0; i < apiAmount; i++) {
			for (int j = 0; j < apiAmount; j++) {
				GetDegree gd = new GetDegree();
				int ka = gd.getApiDegree(i, mashupApiMatrix);
				if(ka == 0){
					similarity[i][j] = 0;
					continue;
				}
				
				
				double temp = 0;
				
				for(int l = 0;l < mashupAmount;l++){
					int il = mashupApiMatrix[l][i];
					int jl = mashupApiMatrix[l][j];
					
					int km = gd.getMashupDegree(l, mashupApiMatrix);
					//System.out.println("km" + km);
					if(km != 0){
						temp = temp + il * jl * 1.0 / km;
					}
//					if(l == mashupAmount - 1){
//						System.out.println("temp : " + temp);
//					}
				}
				
				similarity[i][j] = temp / ka;
				//System.out.println("ka:" + ka + "temp:" + temp);
				//System.out.println(similarity[i][j]);
				if(similarity[i][j] < min && similarity[i][j] != 0){
					min = similarity[i][j];
				}
			}
		}
		System.out.println("非0最小相似度为：" + min);
		return similarity;
	}
}
