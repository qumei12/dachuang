package model;

import filehelper.CaseSupplyMatrixService;

import java.util.*;


public class LDAModel {
	public Integer[][] CasesSupplies;  // 病案-耗材矩阵
	int topicAmount;                   // 主题数量
	int caseAmount;                    // 病案数量
	int supplyAmount;                  // 耗材数量

	int[][] CasesTopics;               // 病案-主题计数矩阵
	int[] CasesTopics_sum;             // 每个病案的主题总数

	int[][] TopicsSupplies;            // 主题-耗材计数矩阵
	int[] TopicsSupplies_sum;          // 每个主题的耗材总数

	int[][] z;                         // 主题分配矩阵

	double[][] phi;                    // 主题-耗材分布矩阵
	double[][] theta;                  // 病案-主题分布矩阵

	double alpha;                      // Dirichlet参数
	double beta;                       // Dirichlet参数

	public int iterations;             // 迭代次数
	int saveStep;                      // 保存步长
	int beginSaveIters;                // 开始保存迭代次数

//	int top_k;



	public LDAModel() {
		topicAmount = 50;              // 主题数量
		alpha =0.5;             	   // Dirichlet参数
		beta = 0.01;                   // Dirichlet参数，较小的值有助于稀疏化

		iterations = 5000;              // 迭代次数
		saveStep = 10;                 // 保存步长
		beginSaveIters = 4500;          // 开始保存迭代次数，给模型更多时间收敛
	}

	public void initializeLDAModel() {
		CasesSupplies = CaseSupplyMatrixService.getCaseSupplyMatrix();  // 获取病案-耗材矩阵
		caseAmount = CasesSupplies.length;
		supplyAmount = CasesSupplies[0].length;
		
		// 初始化病案-主题计数矩阵
		CasesTopics = new int[caseAmount][topicAmount];
		for (int i = 0; i < CasesTopics.length; i++) {
			for (int j = 0; j < CasesTopics[i].length; j++) {
				CasesTopics[i][j] = 0;
			}
		}

		// 初始化主题-耗材计数矩阵
		TopicsSupplies = new int[topicAmount][supplyAmount];
		for (int i = 0; i < TopicsSupplies.length; i++) {
			for (int j = 0; j < TopicsSupplies[i].length; j++) {
				TopicsSupplies[i][j] = 0;
			}
		}

		// 初始化病案主题总数数组
		CasesTopics_sum = new int[caseAmount];
		for (int i = 0; i < CasesTopics_sum.length; i++) {
			CasesTopics_sum[i] = 0;
		}
		
		// 初始化主题耗材总数数组
		TopicsSupplies_sum = new int[topicAmount];
		for (int i = 0; i < TopicsSupplies_sum.length; i++) {
			TopicsSupplies_sum[i] = 0;
		}

		// 初始化phi矩阵
		phi = new double[topicAmount][supplyAmount];
		for (int i = 0; i < phi.length; i++) {
			for (int j = 0; j < phi[i].length; j++) {
				phi[i][j] = 0.0;
			}
		}

		// 初始化theta矩阵
		theta = new double[caseAmount][topicAmount];
		for (int i = 0; i < theta.length; i++) {
			for (int j = 0; j < theta[i].length; j++) {
				theta[i][j] = 0.0;
			}
		}

		// 初始化主题分配矩阵
		z = new int[caseAmount][];
		for (int m = 0; m < caseAmount; m++) {
			// 计算当前病案中实际使用的耗材数量
			int N = 0;
			for (int j = 0; j < supplyAmount; j++) {
				if (CasesSupplies[m][j] == 1) {
					N++;
				}
			}
			
			// 如果病案中没有耗材，确保至少有一个元素防止数组越界
			if (N == 0) {
				N = 1;
			}
			
			z[m] = new int[N];
			
			// 为每个实际使用的耗材分配主题
			int position = 0;
			for (int n = 0; n < supplyAmount; n++) {
				if (CasesSupplies[m][n] == 1) {
					int initTopic = (int) (Math.random() * topicAmount);
					z[m][position] = initTopic;
					// 使用列索引n作为耗材索引
					CasesTopics[m][initTopic]++;
					TopicsSupplies[initTopic][n]++;  
					CasesTopics_sum[m]++;
					TopicsSupplies_sum[initTopic]++;
					position++;
				}
			}
			
			// 处理空病案的情况，确保z矩阵至少有一个元素
			if (position == 0) {
				int initTopic = (int) (Math.random() * topicAmount);
				z[m][0] = initTopic;
				// 为避免影响模型，可以为一个虚拟耗材分配主题
				// 或者使用第一个耗材作为默认值
				int defaultSupplyIndex = 0;
				CasesTopics[m][initTopic]++;
				TopicsSupplies[initTopic][defaultSupplyIndex]++;  
				CasesTopics_sum[m]++;
				TopicsSupplies_sum[initTopic]++;
			}
		}
	}

	public void inferenceModel() {
		// TODO Auto-generated method stub
		for (int i = 0; i < iterations; i++) {
			//System.out.println("Iteration " + i);
			if ((i >= beginSaveIters) && (((i - beginSaveIters) % saveStep) == 0)) {
				// Saving the model
				//System.out.println("Saving model at iteration " + i + " ... ");
				updateEstimatedParameters();
			}

			// Use Gibbs Sampling to update z[][]
			for (int m = 0; m < caseAmount; m++) {
				// 遍历当前病案中实际使用的耗材数量
				for (int n = 0; n < z[m].length; n++) {
					// Sample from p(z_i|z_-i, w)
					int newTopic = sampleTopicZ(m, n);
					z[m][n] = newTopic;
				}
			}
		}
		
		// 训练结束后更新最终参数
		updateEstimatedParameters();
	}

	// 采样主题分配
	public int sampleTopicZ(int caseIndex, int position) {
		// Remove topic label for w_{m,n}
		int oldTopic = z[caseIndex][position];
		
		// 找到实际的耗材索引
		int supplyIndex = -1;
		int currentPosition = 0;
		for (int j = 0; j < supplyAmount; j++) {
			if (CasesSupplies[caseIndex][j] == 1) {
				if (currentPosition == position) {
					supplyIndex = j;
					break;
				}
				currentPosition++;
			}
		}
		
		// 如果没有找到对应的耗材索引，使用默认值
		// 这种情况可能发生在空病案或position超出范围时
		if (supplyIndex == -1) {
			supplyIndex = 0;
		}
		
		CasesTopics[caseIndex][oldTopic]--;
		TopicsSupplies[oldTopic][supplyIndex]--;
		CasesTopics_sum[caseIndex]--;
		TopicsSupplies_sum[oldTopic]--;

		// Compute p(z_i = k|z_-i, w)
		Double[] p = new Double[topicAmount];
		for (int k = 0; k < topicAmount; k++) {
			p[k] = (TopicsSupplies[k][supplyIndex] + beta)
					/ (TopicsSupplies_sum[k] + supplyAmount * beta)
					* (CasesTopics[caseIndex][k] + alpha) / (CasesTopics_sum[caseIndex] + topicAmount * alpha);
		}

		// Sample a new topic label for w_{m, n} like roulette
		// Compute cumulated probability for p
		for (int k = 1; k < topicAmount; k++) {
			p[k] += p[k - 1];
		}
		
		// 避免除零错误
		if (p[topicAmount - 1] <= 0) {
			// 恢复原来的计数
			CasesTopics[caseIndex][oldTopic]++;
			TopicsSupplies[oldTopic][supplyIndex]++;
			CasesTopics_sum[caseIndex]++;
			TopicsSupplies_sum[oldTopic]++;
			return oldTopic; // 如果概率和为0或负数，保持原主题
		}
		
		double u = Math.random() * p[topicAmount - 1]; // p[] is unnormalised
		int newTopic;
		for (newTopic = 0; newTopic < topicAmount; newTopic++) {
			if (u < p[newTopic]) {
				break;
			}
		}
		
		// 修复数组越界问题：确保newTopic在有效范围内
		if (newTopic >= topicAmount) {
			newTopic = topicAmount - 1;
		}

		// Add new topic label for w_{m, n}
		z[caseIndex][position] = newTopic;
		CasesTopics[caseIndex][newTopic]++;
		TopicsSupplies[newTopic][supplyIndex]++;
		CasesTopics_sum[caseIndex]++;
		TopicsSupplies_sum[newTopic]++;
		return newTopic;
	}

	// 将私有方法改为公共方法，以便在ModelTrainer中调用
	public void updateEstimatedParameters() {
		// TODO Auto-generated method stub
		for (int k = 0; k < topicAmount; k++) {
			for (int t = 0; t < supplyAmount; t++) {
				phi[k][t] = (TopicsSupplies[k][t] + beta) / (TopicsSupplies_sum[k] + supplyAmount * beta);
			}
		}

		for (int m = 0; m < caseAmount; m++) {
			for (int k = 0; k < topicAmount; k++) {
				theta[m][k] = (CasesTopics[m][k] + alpha) / (CasesTopics_sum[m] + topicAmount * alpha);
			}
		}
	}
	
	//Map<Integer, List<supplyIndex>>
	public Map<Integer, List<Integer>> getAPIWordsBag(){
		int top = 20;
		Map<Integer, List<Integer>> map = new HashMap<Integer, List<Integer>>();
		
		for(int i = 0;i < topicAmount;i++){
			List<Integer> list = new ArrayList<Integer>();
			Double[] array = new Double[supplyAmount];
			for(int j = 0;j < supplyAmount;j++){
				array[j] = phi[i][j];
			}
			
			for(int t = 0;t < top;t++){
				Double max = 0.0;
				int index = -1;
				for(int j = 0;j < supplyAmount;j++){
					if(array[j] > max){
						max = array[j];
						index = j;
					}
				}
				if(index != -1){
					list.add(index);
					array[index] = 0.0;
				}
			}
			map.put(i, list);
		}
		return map;
	}
	
	public int[][] getCasesTopics() {
		return CasesTopics;
	}

	public void setCasesTopics(int[][] casesTopics) {
		CasesTopics = casesTopics;
	}

	public int[] getCasesTopics_sum() {
		return CasesTopics_sum;
	}

	public void setCasesTopics_sum(int[] casesTopics_sum) {
		CasesTopics_sum = casesTopics_sum;
	}

	public int[][] getTopicsSupplies() {
		return TopicsSupplies;
	}

	public void setTopicsSupplies(int[][] topicsSupplies) {
		TopicsSupplies = topicsSupplies;
	}

	public int[] getTopicsSupplies_sum() {
		return TopicsSupplies_sum;
	}

	public void setTopicsSupplies_sum(int[] topicsSupplies_sum) {
		TopicsSupplies_sum = topicsSupplies_sum;
	}

	public double[][] getPhi() {
		return phi;
	}

	public double[][] getTheta() {
		return theta;
	}
	
	// 添加setter方法，用于加载预训练模型
	public void setPhi(double[][] phi) {
		this.phi = phi;
	}

	public void setTheta(double[][] theta) {
		this.theta = theta;
	}

	public int getTopicAmount() {
		return topicAmount;
	}
	
	public void setTopicAmount(int topicAmount) {
		this.topicAmount = topicAmount;
	}

	public int getSupplyAmount() {
		return supplyAmount;
	}
	
	public void setSupplyAmount(int supplyAmount) {
		this.supplyAmount = supplyAmount;
	}

	public int getCaseAmount() {
		return caseAmount;
	}
	
	public void setCaseAmount(int caseAmount) {
		this.caseAmount = caseAmount;
	}
	
	public double getAlpha() {
		return alpha;
	}
	
	public void setAlpha(double alpha) {
		this.alpha = alpha;
	}
	
	public double getBeta() {
		return beta;
	}
	
	public void setBeta(double beta) {
		this.beta = beta;
	}
	
	public int getIterations() {
		return iterations;
	}
	
	public void setIterations(int iterations) {
		this.iterations = iterations;
	}

	/**
	 * 基于病种的耗材组合推荐方法
	 * @param diseaseCases 该病种的所有病案索引列表
	 * @param threshold 主题概率阈值（默认0.5）
	 * @param topM 每个主题选取的候选耗材数量（默认10）
	 * @return 推荐的耗材索引列表
	 */
	public List<Integer> recommendSuppliesForDisease(List<Integer> diseaseCases, double threshold, int topM) {
		// 步骤1: 计算该病种的典型主题分布θ_disease
		double[] diseaseTopicDistribution = computeDiseaseTopicDistribution(diseaseCases);
		
		// 步骤2: 获取候选耗材池
		Map<Integer, List<CandidateSupply>> candidateSupplies = getCandidateSupplies(diseaseTopicDistribution, threshold, topM);
		
		// 步骤3: 组合优化，选出最佳组合包
		List<Integer> recommendedSupplies = optimizeCombination(candidateSupplies);
		
		return recommendedSupplies;
	}
	
	/**
	 * 计算病种的典型主题分布（θ_disease）
	 * @param diseaseCases 该病种的所有病案索引列表
	 * @return 该病种的平均主题分布向量
	 */
	private double[] computeDiseaseTopicDistribution(List<Integer> diseaseCases) {
		double[] diseaseTopicDistribution = new double[topicAmount];
		
		// 计算所有病案的平均主题分布
		for (int caseIndex : diseaseCases) {
			for (int topicIndex = 0; topicIndex < topicAmount; topicIndex++) {
				diseaseTopicDistribution[topicIndex] += theta[caseIndex][topicIndex];
			}
		}
		
		// 计算平均值
		for (int i = 0; i < diseaseTopicDistribution.length; i++) {
			diseaseTopicDistribution[i] /= diseaseCases.size();
		}
		
		return diseaseTopicDistribution;
	}
	
	/**
	 * 根据病种主题分布获取候选耗材池
	 * @param diseaseTopicDistribution 病种主题分布
	 * @param threshold 主题概率阈值
	 * @param topM 每个主题选取的候选耗材数量
	 * @return 候选耗材池，key为主题索引，value为该主题下的候选耗材列表
	 */
	private Map<Integer, List<CandidateSupply>> getCandidateSupplies(
			double[] diseaseTopicDistribution, double threshold, int topM) {
		
		Map<Integer, List<CandidateSupply>> candidateSupplies = new HashMap<>();
		
		// 遍历所有主题
		for (int topicIndex = 0; topicIndex < diseaseTopicDistribution.length; topicIndex++) {
			// 只处理概率高于阈值的主题
			if (diseaseTopicDistribution[topicIndex] >= threshold) {
				// 获取该主题下概率最高的topM个耗材
				List<CandidateSupply> supplies = getTopSuppliesForTopic(topicIndex, topM);
				candidateSupplies.put(topicIndex, supplies);
			}
		}
		
		return candidateSupplies;
	}
	
	/**
	 * 获取指定主题下概率最高的前M个耗材
	 * @param topicIndex 主题索引
	 * @param topM 候选耗材数量
	 * @return 候选耗材列表
	 */
	private List<CandidateSupply> getTopSuppliesForTopic(int topicIndex, int topM) {
		List<CandidateSupply> supplies = new ArrayList<>();
		
		// 创建临时数组存储概率和索引
		Double[] probabilities = new Double[supplyAmount];
		for (int supplyIndex = 0; supplyIndex < supplyAmount; supplyIndex++) {
			probabilities[supplyIndex] = phi[topicIndex][supplyIndex];
		}
		
		// 选择前topM个耗材
		for (int i = 0; i < Math.min(topM, supplyAmount); i++) {
			double maxProb = -1.0;
			int maxIndex = -1;
			
			// 找到概率最高的耗材
			for (int j = 0; j < probabilities.length; j++) {
				if (probabilities[j] != null && probabilities[j] > maxProb) {
					maxProb = probabilities[j];
					maxIndex = j;
				}
			}
			
			// 如果找到了有效耗材
			if (maxIndex != -1) {
				supplies.add(new CandidateSupply(maxIndex, maxProb, topicIndex));
				probabilities[maxIndex] = null; // 标记为已选择
			}
		}
		
		return supplies;
	}
	
	/**
	 * 组合优化：从候选池中选出最佳组合包
	 * @param candidateSupplies 候选耗材池
	 * @return 最佳耗材组合
	 */
	private List<Integer> optimizeCombination(Map<Integer, List<CandidateSupply>> candidateSupplies) {
		List<Integer> recommendedSupplies = new ArrayList<>();
		
		// 方法A：基于规则的筛选（简单实用）
		// 1. 功能覆盖规则：每个必需主题至少选择一件耗材
		// 2. 去重规则：同一主题内选择概率最高的耗材
		// 3. 冲突避免规则：确保组合内耗材无冲突
		
		// 遍历所有必需主题
		for (Map.Entry<Integer, List<CandidateSupply>> entry : candidateSupplies.entrySet()) {
			List<CandidateSupply> candidates = entry.getValue();
			
			// 按概率排序，优先选择概率高的
			candidates.sort((a, b) -> Double.compare(b.probability, a.probability));
			
			// 在当前主题的候选耗材中选择最适合的（这里简化处理，选择概率最高的）
			if (!candidates.isEmpty()) {
				recommendedSupplies.add(candidates.get(0).supplyIndex);
			}
		}
		
		return recommendedSupplies;
	}
	
	// 内部类：候选耗材
	public static class CandidateSupply {
		public int supplyIndex;    // 耗材索引
		public double probability; // 概率
		public int topicIndex;     // 主题索引
		
		public CandidateSupply(int supplyIndex, double probability, int topicIndex) {
			this.supplyIndex = supplyIndex;
			this.probability = probability;
			this.topicIndex = topicIndex;
		}
	}
}