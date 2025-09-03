package model;

import filehelper.FileUtil;
import filehelper.GetUserService;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;


public class LDAModel {
	public Integer[][] UsersServices;
	int interestAmount;
	int userAmount;
	int serviceAmount;

	int[][] UsersInterests;// 计数矩阵
	int[] UsersInterests_sum;

	int[][] InterestsServices;// 计数矩阵
	int[] InterestsServices_sum;

	int[][] z;

	double[][] phi;
	double[][] theta;

	int[][] recommand;

	double alpha;
	double beta;

	public int iterations;
	int saveStep;
	int beginSaveIters;

//	int top_k;



	public LDAModel() {// 修改构造函数，不接收top_k参数

		interestAmount = 80;
		// userAmount = 100;
		// serviceAmount = 2000;

		alpha = 50.0 / interestAmount;
		beta = 0.01;

		iterations = 100;
		saveStep = 10;
		beginSaveIters = 80;
//		this.top_k = top_k;
	}

	public void initializeLDAModel() {
		UsersServices = GetUserService.getUsersServices();
		userAmount = UsersServices.length;
		serviceAmount = UsersServices[0].length;
		UsersInterests = new int[userAmount][interestAmount];
		for (int i = 0; i < UsersInterests.length; i++) {
			for (int j = 0; j < UsersInterests[i].length; j++) {
				UsersInterests[i][j] = 0;
			}
		}

		InterestsServices = new int[interestAmount][serviceAmount];
		for (int i = 0; i < InterestsServices.length; i++) {
			for (int j = 0; j < InterestsServices[i].length; j++) {
				InterestsServices[i][j] = 0;
			}
		}

		UsersInterests_sum = new int[userAmount];
		for (int i = 0; i < UsersInterests_sum.length; i++) {
			UsersInterests_sum[i] = 0;
		}
		InterestsServices_sum = new int[interestAmount];
		for (int i = 0; i < InterestsServices_sum.length; i++) {
			InterestsServices_sum[i] = 0;
		}

		phi = new double[interestAmount][serviceAmount];
		for (int i = 0; i < phi.length; i++) {
			for (int j = 0; j < phi[i].length; j++) {
				phi[i][j] = 0.0;
			}
		}

		theta = new double[userAmount][interestAmount];
		for (int i = 0; i < theta.length; i++) {
			for (int j = 0; j < theta[i].length; j++) {
				theta[i][j] = 0.0;
			}
		}

		z = new int[userAmount][];
		for (int m = 0; m < userAmount; m++) {
			int N = UsersServices[m].length;
			z[m] = new int[N];
			for (int n = 0; n < N; n++) {
				int initInterest = (int) (Math.random() * interestAmount);// From
																			// 0
																			// to
																			// K
																			// -
																			// 1
				z[m][n] = initInterest;
				// number of words in doc m assigned to topic initTopic add 1
				UsersInterests[m][initInterest]++;
				// number of terms doc[m][n] assigned to topic initTopic add 1
				InterestsServices[initInterest][n]++;
				// total number of words assigned to topic initTopic add 1
				InterestsServices_sum[initInterest]++;
			}
			// total number of words in document m is N
			UsersInterests_sum[m] = N;

		}
	}

	public void inferenceModel() {
		if (iterations < saveStep + beginSaveIters) {
			System.err.println("Error: the number of iterations should be larger than " + (saveStep + beginSaveIters));
			System.exit(0);
		}
		for (int i = 0; i < iterations; i++) {
			//System.out.println("Iteration " + i);
			if ((i >= beginSaveIters) && (((i - beginSaveIters) % saveStep) == 0)) {
				// Saving the model
				//System.out.println("Saving model at iteration " + i + " ... ");
				updateEstimatedParameters();
			}

			// Use Gibbs Sampling to update z[][]
			for (int m = 0; m < userAmount; m++) {
				int N = UsersServices[m].length;
				for (int n = 0; n < N; n++) {
					// Sample from p(z_i|z_-i, w)
					int newInterest = sampleInterestZ(m, n);
					z[m][n] = newInterest;
				}
			}
		}

	}

	private int sampleInterestZ(int m, int n) {
		// TODO Auto-generated method stub
		// Sample from p(z_i|z_-i, w) using Gibbs upde rule

		// Remove topic label for w_{m,n}
		int oldInterest = z[m][n];
		UsersInterests[m][oldInterest]--;
		InterestsServices[oldInterest][n]--;
		UsersInterests_sum[m]--;
		InterestsServices_sum[oldInterest]--;

		// Compute p(z_i = k|z_-i, w)
		Double[] p = new Double[interestAmount];
		for (int k = 0; k < interestAmount; k++) {
			p[k] = (InterestsServices[k][UsersServices[m][n]] + beta)
					/ (InterestsServices_sum[k] + serviceAmount * beta) * (UsersInterests[m][k] + alpha)
					/ (UsersInterests_sum[m] + interestAmount * alpha);
		}

		// Sample a new topic label for w_{m, n} like roulette
		// Compute cumulated probability for p
		for (int k = 1; k < interestAmount; k++) {
			p[k] += p[k - 1];
		}
		double u = Math.random() * p[interestAmount - 1]; // p[] is unnormalised
		int newInterest;
		for (newInterest = 0; newInterest < interestAmount; newInterest++) {
			if (u < p[newInterest]) {
				break;
			}
		}
		if (newInterest == 10) {
			newInterest = 9;
		}

		// Add new topic label for w_{m, n}
		UsersInterests[m][newInterest]++;
		InterestsServices[newInterest][n]++;
		UsersInterests_sum[m]++;
		InterestsServices_sum[newInterest]++;
		return newInterest;
	}

	private void updateEstimatedParameters() {
		// TODO Auto-generated method stub
		for (int k = 0; k < interestAmount; k++) {
			for (int t = 0; t < serviceAmount; t++) {
				phi[k][t] = (InterestsServices[k][t] + beta) / (InterestsServices_sum[k] + serviceAmount * beta);
			}
		}

		for (int m = 0; m < userAmount; m++) {
			for (int k = 0; k < interestAmount; k++) {
				theta[m][k] = (UsersInterests[m][k] + alpha) / (UsersInterests_sum[m] + interestAmount * alpha);
			}
		}
	}
	
	//Map<Integer, List<apiindex>>
	public Map<Integer, List<Integer>> getAPIWordsBag(){
		int top = 20;
		Map<Integer, List<Integer>> map = new HashMap<>();
		
		for(int i = 0; i < interestAmount; i++){
			List<Integer> tWordsIndexArray = new ArrayList<Integer>();
			List<Integer> list = new ArrayList<>();
			
			for(int j = 0; j < serviceAmount; j++){
				tWordsIndexArray.add(new Integer(j));
			}
			
			Collections.sort(tWordsIndexArray, new LDAModel.TwordsComparable(phi[i]));
			for(int t = 0; t < top; t++){
				list.add(tWordsIndexArray.get(t));
			}
			
			map.put(i, list);
		}
		
		return map;
	}
	
	
	//Map<interest, list<mashupindex>>
	public Map<Integer, List<Integer>> getMashupWordsBag(){
		//int top = 20;
		Map<Integer, List<Integer>> map = new HashMap<>();
		for(int i = 0;i < interestAmount;i++){
			List<Integer> tWordsIndexArray = new ArrayList<Integer>();
			List<Integer> list = new ArrayList<>();
			
			for(int j = 0; j < userAmount; j++){
				tWordsIndexArray.add(new Integer(j));
			}
			double[] ds = new double[userAmount];
			
			for(int m = 0;m < userAmount;m++){
				ds[m] = theta[m][i];
			}
			Collections.sort(tWordsIndexArray, new LDAModel.TwordsComparable(ds));
			for(int t = 0; t < userAmount; t++){
				//if(tWordsIndexArray.get(t) > 0){
					list.add(tWordsIndexArray.get(t));
				//}
				//writer.write(
						//"[Mashup-" + tWordsIndexArray.get(t) + "]" + " " + ds[tWordsIndexArray.get(t)] + "\t");
			}
			
			//writer.write("\n");
			map.put(i, list);
		}
		return map;
	}

	public void saveIteratedModel(int iters) throws IOException {
		// TODO Auto-generated method stub
		// lda.params lda.phi lda.theta lda.tassign lda.twords
		// lda.params
		String resPath = "LdaResults/";
		String modelName = "lda_" + iters;
		ArrayList<String> lines = new ArrayList<String>();
		lines.add("alpha = " + alpha);
		lines.add("beta = " + beta);
		lines.add("interestAmount = " + interestAmount);
		lines.add("userAmount = " + userAmount);
		lines.add("serviceAmount = " + serviceAmount);
		lines.add("iterations = " + iterations);
		lines.add("saveStep = " + saveStep);
		lines.add("beginSaveIters = " + beginSaveIters);
		FileUtil.writeLines(resPath + modelName + ".params", lines);

		// lda.phi K*V
		BufferedWriter writer = new BufferedWriter(new FileWriter(resPath + modelName + ".phi"));
		for (int i = 0; i < interestAmount; i++) {
			for (int j = 0; j < serviceAmount; j++) {
				writer.write(phi[i][j] + "\t");
			}
			writer.write("\n");
		}
		writer.close();

		// lda.theta M*K
		writer = new BufferedWriter(new FileWriter(resPath + modelName + ".theta"));
		for (int i = 0; i < userAmount; i++) {
			for (int j = 0; j < interestAmount; j++) {
				writer.write(theta[i][j] + "\t");
			}
			writer.write("\n");
		}
		writer.close();

		// lda.tassign
		writer = new BufferedWriter(new FileWriter(resPath + modelName + ".tassign"));
		for (int m = 0; m < userAmount; m++) {
			for (int n = 0; n < UsersServices[m].length; n++) {
				writer.write(UsersServices[m][n] + ":" + z[m][n] + "\t");
			}
			writer.write("\n");
		}
		writer.close();

		// lda.twords phi[][] K*V
		writer = new BufferedWriter(new FileWriter(resPath + modelName + ".twords"));
		int topNum = 20; // Find the top 20 topic words in each topic
		for(int i = 0;i < interestAmount;i++){
			Set<String> set = new HashSet<String>();
			List<Integer> tWordsIndexArray = new ArrayList<Integer>();
			
			for(int j = 0; j < userAmount; j++){
				tWordsIndexArray.add(new Integer(j));
			}
			double[] ds = new double[userAmount];
			
			for(int m = 0;m < userAmount;m++){
				ds[m] = theta[m][i];
			}
			Collections.sort(tWordsIndexArray, new LDAModel.TwordsComparable(ds));
			for(int t = 0; t < 20; t++){
				writer.write(
						"[Mashup-" + tWordsIndexArray.get(t) + "]" + " " + ds[tWordsIndexArray.get(t)] + "\t");
			}
			writer.write("\n");
		}
//		for (int i = 0; i < interestAmount; i++) {
//			List<Integer> tWordsIndexArray = new ArrayList<Integer>();
//			for (int j = 0; j < serviceAmount; j++) {
//				tWordsIndexArray.add(new Integer(j));
//			}
//			Collections.sort(tWordsIndexArray, new LDAModel.TwordsComparable(phi[i]));
//			writer.write("interest " + i + "\t:\t");
//			for (int t = 0; t < topNum; t++) {
//				writer.write(
//						"[Service-" + tWordsIndexArray.get(t) + "]" + " " + phi[i][tWordsIndexArray.get(t)] + "\t");
//			}
//			writer.write("\n");
//		}
		writer.close();
	}

	public int[][] topKRecommand(int top_k) {
		double x[][] = new double[this.userAmount][this.serviceAmount];
		for (int i = 0; i < x.length; i++) {
			for (int j = 0; j < x[i].length; j++) {
				x[i][j] = 0.0;
			}
		}

		// for(int i = 0;i < this.theta[0].length; i++){
		//
		// for(int j = 0;j < this.phi.length;j++){
		// theta[i][i] * phi[]
		// }
		// }

		for (int i = 0; i < userAmount; i++) {
			for (int j = 0; j < serviceAmount; j++) {
				double temp = 0;
				for (int k = 0; k < this.theta[0].length; k++) {
					temp += this.theta[i][k] * this.phi[k][j];
				}
				x[i][j] = temp;
			}
		}

//		for (int i = 0; i < top_k; i++) {
//			for (int j = 0; j < top_k; j++) {
//				System.out.print(x[i][j] + "\t");
//			}
//			System.out.println();
//		}

		//System.out.println("*****************");

		// List<Map<Integer,Double>> list = new ArrayList<>();
		// for (int i = 0; i < x.length; i++) {
		// Map<Integer, Double> map = new HashMap<Integer, Double>();
		// for (int j = 0; j < x[i].length; j++) {
		// map.put(j, x[i][j]);
		// }
		// list.add(map);
		// }
 
		recommand = new int[userAmount][top_k];

		// Iterator<Map<Integer, Double>> it = list.iterator();

		int[][] index = new int[x.length][x[1].length];

		for (int i = 0; i < index.length; i++) {
			for (int j = 0; j < index[i].length; j++) {
				index[i][j] = j;
			}
		}

		for (int i = 0; i < x.length; i++) {
			for (int j = 0; j < x[i].length - 1; j++) {
				for (int k = 0; k < x[i].length - j - 1; k++) {
					if (x[i][k] < x[i][k + 1]) {
						double temp = x[i][k];
						x[i][k] = x[i][k + 1];
						x[i][k + 1] = temp;

						int m = index[i][k];
						index[i][k] = index[i][k + 1];
						index[i][k + 1] = m;
					}
				}
			}
		}

//		BufferedWriter bw = null;
//		try {
//			File file = new File("abc.txt");
//			FileWriter fw = new FileWriter(file);
//			bw = new BufferedWriter(fw);
//
//			for (int i = 0; i < userAmount; i++) {
//				for (int j = 0; j < serviceAmount; j++) {
//					bw.write(index[i][j] + "\t");
//				}
//				bw.newLine();
//				bw.flush();
//			}
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} finally {
//			try {
//				bw.close();
//			} catch (IOException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//		}

		for (int i = 0; i < recommand.length; i++) {
			for (int j = 0; j < recommand[i].length; j++) {
				recommand[i][j] = index[i][j];
			}
		}

		return recommand;

//			Go.mian(recommand[43],top_k);
		//if(stages == true){
		//Go.mian(recommand[(int)((Math.random() * recommand.length) - 1)],top_k);			
		//}
		
		
	}

	public void accuracy(int top_k) {
		int[] count = new int[this.userAmount];

		for (int i = 0; i < userAmount; i++) {
			for (int k = 0; k < recommand[i].length; k++) {
				if (UsersServices[i][this.recommand[i][k]] == 1) {
					count[i]++;
				}
			}
		}
		int sum = 0;
		for (int i = 0; i < count.length; i++) {
			sum += count[i];
		}
		System.out.println("Accuracy:" + ((double) sum * 10) / (top_k * userAmount));

		// 召回率
		int like = 0;
		for (int i = 0; i < userAmount; i++) {
			for (int j = 0; j < serviceAmount; j++) {
				if (UsersServices[i][j] == 1) {
					like++;
				}
			}
		}

		double recall = (double) sum/ like;
		System.out.println("Recall rate: " + recall);

		// 海明距离

		double[][] Q = new double[userAmount][userAmount];
		for (int i = 0; i < userAmount - 1; i++) {
			for (int j = i + 1; j < userAmount; j++) {

				int equal = 0;
				for (int k = 0; k < top_k; k++) {
					for (int l = 0; l < top_k; l++) {
						if (recommand[i][k] == recommand[j][l]) {
							equal++;
							break;
						}
					}
				}

				Q[i][j] = ((double) equal) / top_k;

			}
		}
		double hamming = 0;
		double sum_q = 0;
		for (int i = 0; i < userAmount; i++) {
			for (int j = i; j < userAmount; j++) {
				sum_q += Q[i][j];
			}
		}
		hamming = 1 - sum_q / ((userAmount - 1) * userAmount / 2);
		System.out.println("Hamming: " + hamming);
	}
	
	public void Hamming(int top_k){
		double[][] Q = new double[userAmount][userAmount];
		for (int i = 0; i < userAmount - 1; i++) {
			for (int j = i + 1; j < userAmount; j++) {

				int equal = 0;
				for (int k = 0; k < top_k; k++) {
					for (int l = 0; l < top_k; l++) {
						if (recommand[i][k] == recommand[j][l]) {
							equal++;
							break;
						}
					}
				}

				Q[i][j] = ((double) equal) / top_k;

			}
		}
		double hamming = 0;
		double sum_q = 0;
		for (int i = 0; i < userAmount; i++) {
			for (int j = i; j < userAmount; j++) {
				sum_q += Q[i][j];
			}
		}
		hamming = 1 - sum_q / ((userAmount - 1) * userAmount / 2);
		System.out.println("海明距离：" + hamming);
	}

	public class TwordsComparable implements Comparator<Integer> {

		public double[] sortProb; // Store probability of each word in topic k

		public TwordsComparable(double[] phi) {
			this.sortProb = phi;
		}

		@Override
		public int compare(Integer o1, Integer o2) {
			// TODO Auto-generated method stub
			// Sort topic word index according to the probability of each word
			// in topic k
			if (sortProb[o1] > sortProb[o2])
				return -1;
			else if (sortProb[o1] < sortProb[o2])
				return 1;
			else
				return 0;
		}
	}

	/**
	 * 为指定用户(Mashup)推荐API，每个兴趣主题推荐一个最相关的API
	 * @param userId 用户ID（即Mashup ID）
	 * @param topK 兴趣主题数量
	 * @return 推荐的API索引列表
	 */
	public List<Integer> recommandOneAPIPerInterest(int userId, int topK) {
		List<Integer> recommandedAPIs = new ArrayList<>();

		System.out.println("为用户ID " + userId + " 推荐API，选取Top " + topK + " 个兴趣主题");

		// 获取用户对各个兴趣主题的概率
		Double[] interestProbs = new Double[interestAmount];
		for (int k = 0; k < interestAmount; k++) {
			interestProbs[k] = theta[userId][k];
		}

		// 找出概率最高的topK个兴趣主题
		List<Integer> topInterestIndices = new ArrayList<>();
		for (int i = 0; i < interestAmount; i++) {
			topInterestIndices.add(i);
		}

		// 按照概率排序
		Collections.sort(topInterestIndices, new Comparator<Integer>() {
			@Override
			public int compare(Integer o1, Integer o2) {
				if (interestProbs[o1] > interestProbs[o2]) return -1;
				else if (interestProbs[o1] < interestProbs[o2]) return 1;
				else return 0;
			}
		});

		// 为每个topK兴趣主题推荐一个最相关的API
		for (int i = 0; i < Math.min(topK, interestAmount); i++) {
			int interestId = topInterestIndices.get(i);

			// 在该兴趣主题中找出最相关的API
			double maxProb = -1;
			int bestAPIId = -1;

			for (int apiId = 0; apiId < serviceAmount; apiId++) {
				if (phi[interestId][apiId] > maxProb) {
					maxProb = phi[interestId][apiId];
					bestAPIId = apiId;
				}
			}

			if (bestAPIId != -1) {
				recommandedAPIs.add(bestAPIId);
				System.out.println("  兴趣主题 " + interestId + " (概率: " + String.format("%.6f", interestProbs[interestId]) +
						") 推荐API索引 " + bestAPIId + " (概率: " + String.format("%.6f", maxProb) + ")");
			}
		}

		return recommandedAPIs;
	}

	// 添加公共getter方法
	public int getInterestAmount() {
		return interestAmount;
	}

	public int getUserAmount() {
		return userAmount;
	}

	public int getServiceAmount() {
		return serviceAmount;
	}

	public double[][] getTheta() {
		return theta;
	}

	public double[][] getPhi() {
		return phi;
	}

}


