package allthings;

public class GetRelation {

	int MashupAmount;
	int ApiAmount;

	public double[][] getSimilarityByJS_REA(int[][] trainMA) {

		MashupAmount = trainMA.length;
		ApiAmount = trainMA[0].length;

		int[][] COMMON = this.getArrayCOMMON(trainMA);

		int[][] UNION = this.getArrayUNION(trainMA);

		double[][] similarity = this.getArraySimilarity(COMMON, UNION);

		return similarity;

	}

	private int[][] getArrayUNION(int[][] MA) {

		int[][] UNION = new int[ApiAmount][ApiAmount];
		// 计算UNION数组
		for (int i = 0; i < ApiAmount; i++)
			for (int j = 0; j < MashupAmount; j++)
				UNION[i][i] += MA[j][i];
		for (int i = 0; i < ApiAmount; i++)
			for (int j = i + 1; j < ApiAmount; j++)
				for (int k = 0; k < MashupAmount; k++)
					if ((MA[k][i] == 1) || (MA[k][j] == 1)) {
						UNION[i][j]++;
						UNION[j][i]++;
					}
		return UNION;
	}

	private int[][] getArrayCOMMON(int[][] MA) {

		int[][] COMMON = new int[ApiAmount][ApiAmount];
		// 计算COMMON数组
		for (int i = 0; i < ApiAmount; i++)
			for (int j = 0; j < MashupAmount; j++)
				COMMON[i][i] += MA[j][i];

		for (int i = 0; i < ApiAmount; i++)
			for (int j = i + 1; j < ApiAmount; j++)
				for (int k = 0; k < MashupAmount; k++)
					if (MA[k][i] == 1 && MA[k][j] == 1) {
						COMMON[i][j]++;
						COMMON[j][i]++;
					}
		return COMMON;
	}

	private double[][] getArraySimilarity(int[][] COMMON, int[][] UNION) {
		double[][] PRO1 = new double[ApiAmount][ApiAmount];
		for (int i = 0; i < ApiAmount; i++)
			for (int j = 0; j < ApiAmount; j++)
				PRO1[i][j] = COMMON[i][j] * 1.0 / UNION[i][j];
		return PRO1;
	}

	// 以上为Jaccard算法获取相似度矩阵

	// 以下为物质扩散算法获取相似度矩阵

	public double[][] getSimilarityByMD_REA(int[][] trainMA) {

		MashupAmount = trainMA.length;
		ApiAmount = trainMA[0].length;
		
		double[][] similarity = new double[ApiAmount][ApiAmount];

		for (int i = 0; i < ApiAmount; i++) {
			for (int j = 0; j < ApiAmount; j++) {
				GetDegree gd = new GetDegree();
				int ka = gd.getApiDegree(i, trainMA);
				if(ka == 0){
					similarity[i][j] = 0;
					continue;
				}
				
				
				double temp = 0;
				
				for(int l = 0;l < MashupAmount;l++){
					int il = trainMA[l][i];
					int jl = trainMA[l][j];
					
					int km = gd.getMashupDegree(l, trainMA);
//					if(km == 0){
//						System.out.println("km = 0");
//					}
					temp += (il * jl) * 1.0/km;
					
				}
				
				similarity[i][j] = temp / ka;
				//System.out.println(similarity[i][j]);
				
			}
		}
		
		return similarity;
	}
	
	
	public double[][] getSimilarityByMD_REA() {

		GetMA getMA = new GetMA();
		int[][] MA = getMA.getArrayMA(); 
		
		MashupAmount = MA.length;
		ApiAmount = MA[0].length;
		
		double[][] similarity = new double[ApiAmount][ApiAmount];

		for (int i = 0; i < ApiAmount; i++) {
			for (int j = 0; j < ApiAmount; j++) {
				GetDegree gd = new GetDegree();
				int ka = gd.getApiDegree(i, MA);
				if(ka == 0){
					similarity[i][j] = 0;
					continue;
				}
				
				
				double temp = 0;
				
				for(int l = 0;l < MashupAmount;l++){
					int il = MA[l][i];
					int jl = MA[l][j];
					
					int km = gd.getMashupDegree(l, MA);
					if(km != 0){
						temp += (il * jl) * 1.0/km;
					}
				}
				
				similarity[i][j] = temp / ka;
				//System.out.println(similarity[i][j]);
				
			}
		}
		
		return similarity;
	}
}
