package model;

import filehelper.FileUtil;
import filehelper.GetUserService;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;


public class LDAModel {
	public Integer[][] DiseasesSupplies;
	int interestAmount;
	int diseaseAmount;
	int supplyAmount;

	int[][] DiseasesInterests;// 计数矩阵
	int[] DiseasesInterests_sum;

	int[][] InterestsSupplies;// 计数矩阵
	int[] InterestsSupplies_sum;

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

		interestAmount = 80; // 进一步减少主题数以加快收敛
		// diseaseAmount = 100;
		// supplyAmount = 2000;

		alpha = 0.1; // 使用固定alpha值而不是基于主题数计算
		beta = 0.05; // 调整beta值以平衡稀疏数据处理

		iterations = 1000;   // 减少迭代次数以加快训练
		saveStep = 10;       // 更频繁地保存步长
		beginSaveIters = 20; // 更早开始保存迭代次数
//		this.top_k = top_k;
	}

	public void initializeLDAModel() {
		DiseasesSupplies = GetUserService.getUsersServices();
		diseaseAmount = DiseasesSupplies.length;
		supplyAmount = DiseasesSupplies[0].length;
		DiseasesInterests = new int[diseaseAmount][interestAmount];
		for (int i = 0; i < DiseasesInterests.length; i++) {
			for (int j = 0; j < DiseasesInterests[i].length; j++) {
				DiseasesInterests[i][j] = 0;
			}
		}

		InterestsSupplies = new int[interestAmount][supplyAmount];
		for (int i = 0; i < InterestsSupplies.length; i++) {
			for (int j = 0; j < InterestsSupplies[i].length; j++) {
				InterestsSupplies[i][j] = 0;
			}
		}

		DiseasesInterests_sum = new int[diseaseAmount];
		for (int i = 0; i < DiseasesInterests_sum.length; i++) {
			DiseasesInterests_sum[i] = 0;
		}
		InterestsSupplies_sum = new int[interestAmount];
		for (int i = 0; i < InterestsSupplies_sum.length; i++) {
			InterestsSupplies_sum[i] = 0;
		}

		phi = new double[interestAmount][supplyAmount];
		for (int i = 0; i < phi.length; i++) {
			for (int j = 0; j < phi[i].length; j++) {
				phi[i][j] = 0.0;
			}
		}

		theta = new double[diseaseAmount][interestAmount];
		for (int i = 0; i < theta.length; i++) {
			for (int j = 0; j < theta[i].length; j++) {
				theta[i][j] = 0.0;
			}
		}

		z = new int[diseaseAmount][];
		for (int m = 0; m < diseaseAmount; m++) {
			int N = DiseasesSupplies[m].length;
			z[m] = new int[N];
			for (int n = 0; n < N; n++) {
				int initTopic = (int) (Math.random() * interestAmount);
				z[m][n] = initTopic;
				DiseasesInterests[m][initTopic]++;
				InterestsSupplies[initTopic][DiseasesSupplies[m][n]]++;
				DiseasesInterests_sum[m]++;
				InterestsSupplies_sum[initTopic]++;
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
			for (int m = 0; m < diseaseAmount; m++) {
				int N = DiseasesSupplies[m].length;
				for (int n = 0; n < N; n++) {
					// Sample from p(z_i|z_-i, w)
					int newInterest = sampleInterestZ(m, n);
					z[m][n] = newInterest;
				}
			}
		}

	}

	// 将私有方法改为公共方法，以便在ModelTrainer中调用
	public int sampleInterestZ(int m, int n) {
		// TODO Auto-generated method stub
		// Sample from p(z_i|z_-i, w) using Gibbs update rule

		// Remove topic label for w_{m,n}
		int oldInterest = z[m][n];
		DiseasesInterests[m][oldInterest]--;
		InterestsSupplies[oldInterest][n]--;
		DiseasesInterests_sum[m]--;
		InterestsSupplies_sum[oldInterest]--;

		// Compute p(z_i = k|z_-i, w)
		Double[] p = new Double[interestAmount];
		for (int k = 0; k < interestAmount; k++) {
			p[k] = (InterestsSupplies[k][DiseasesSupplies[m][n]] + beta)
					/ (InterestsSupplies_sum[k] + supplyAmount * beta) * (DiseasesInterests[m][k] + alpha)
					/ (DiseasesInterests_sum[m] + interestAmount * alpha);
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
		
		// 修复数组越界问题：确保newInterest在有效范围内
		if (newInterest >= interestAmount) {
			newInterest = interestAmount - 1;
		}

		// Add new topic label for w_{m, n}
		DiseasesInterests[m][newInterest]++;
		InterestsSupplies[newInterest][n]++;
		DiseasesInterests_sum[m]++;
		InterestsSupplies_sum[newInterest]++;
		return newInterest;
	}

	// 将私有方法改为公共方法，以便在ModelTrainer中调用
	public void updateEstimatedParameters() {
		// TODO Auto-generated method stub
		for (int k = 0; k < interestAmount; k++) {
			for (int t = 0; t < supplyAmount; t++) {
				phi[k][t] = (InterestsSupplies[k][t] + beta) / (InterestsSupplies_sum[k] + supplyAmount * beta);
			}
		}

		for (int m = 0; m < diseaseAmount; m++) {
			for (int k = 0; k < interestAmount; k++) {
				theta[m][k] = (DiseasesInterests[m][k] + alpha) / (DiseasesInterests_sum[m] + interestAmount * alpha);
			}
		}
	}
	
	//Map<Integer, List<apiindex>>
	public Map<Integer, List<Integer>> getAPIWordsBag(){
		int top = 20;
		Map<Integer, List<Integer>> map = new HashMap<Integer, List<Integer>>();
		for (int i = 0; i < supplyAmount; i++) {
			List<Integer> list = new ArrayList<Integer>();
			Double[] arr = new Double[interestAmount];
			int[] arr_index = new int[interestAmount];
			for (int j = 0; j < interestAmount; j++) {
				arr_index[j] = j;
				arr[j] = phi[j][i];
			}
			for (int k = 0; k < arr.length; k++) {
				for (int j = 0; j < arr.length - k - 1; j++) {
					if (arr[j] < arr[j + 1]) {
						double temp = arr[j];
						arr[j] = arr[j + 1];
						arr[j + 1] = temp;

						int temp1 = arr_index[j];
						arr_index[j] = arr_index[j + 1];
						arr_index[j + 1] = temp1;
					}
				}
			}
			for (int j = 0; j < top; j++) {
				list.add(arr_index[j]);
			}
			map.put(i, list);
		}
		return map;
	}

	public Map<Integer, List<Integer>> getMashupWordsBag() {
		int top = 20;
		Map<Integer, List<Integer>> map = new HashMap<Integer, List<Integer>>();
		for (int i = 0; i < diseaseAmount; i++) {
			List<Integer> list = new ArrayList<Integer>();
			Double[] arr = new Double[interestAmount];
			int[] arr_index = new int[interestAmount];
			for (int j = 0; j < interestAmount; j++) {
				arr_index[j] = j;
				arr[j] = theta[i][j];
			}
			for (int k = 0; k < arr.length; k++) {
				for (int j = 0; j < arr.length - k - 1; j++) {
					if (arr[j] < arr[j + 1]) {
						double temp = arr[j];
						arr[j] = arr[j + 1];
						arr[j + 1] = temp;

						int temp1 = arr_index[j];
						arr_index[j] = arr_index[j + 1];
						arr_index[j + 1] = temp1;
					}
				}
			}
			for (int j = 0; j < top; j++) {
				list.add(arr_index[j]);
			}
			map.put(i, list);
		}
		return map;
	}
	
	public int getInterestAmount() {
		return interestAmount;
	}
	
	public int getSupplyAmount() {
		return supplyAmount;
	}
	
	public int getDiseaseAmount() {
		return diseaseAmount;
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
	
	public void setInterestAmount(int interestAmount) {
		this.interestAmount = interestAmount;
	}
	
	public void setDiseaseAmount(int diseaseAmount) {
		this.diseaseAmount = diseaseAmount;
	}
	
	public void setSupplyAmount(int supplyAmount) {
		this.supplyAmount = supplyAmount;
	}
	
	public void setAlpha(double alpha) {
		this.alpha = alpha;
	}
	
	public void setBeta(double beta) {
		this.beta = beta;
	}
	
	public void setIterations(int iterations) {
		this.iterations = iterations;
	}
}