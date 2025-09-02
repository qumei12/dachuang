package model;

import filehelper.GetUserService;
import mian.Go;

public class UCFModel {
	Integer[][] UsersServices;
	double[][] similarity_matrix;
	int userAmount;
	int serviceAmount;

	public void recommand(int top_k, boolean stages) {
		UsersServices = GetUserService.getUsersServices();
		similarity_matrix = new double[UsersServices.length][UsersServices.length];
		userAmount = UsersServices.length;
		serviceAmount = UsersServices[0].length;

		for (int i = 0; i < this.similarity_matrix.length; i++) {
			for (int j = 0; j < this.similarity_matrix.length; j++) {
				if (i == j) {
					similarity_matrix[i][j] = -1;
				} else {
					double sim = new ComputeSimilarity().computeSimilarity(UsersServices[i], UsersServices[j]);
					similarity_matrix[i][j] = sim;
				}
			}
		}

		// for (int i = 0; i < this.similarity_matrix.length; i++) {
		// for (int j = 0; j < this.similarity_matrix.length; j++) {
		// System.out.print(similarity_matrix[i][j] + " ");
		// }
		// System.out.println();
		// }

		int[][] index = new int[userAmount][userAmount];
		for (int i = 0; i < userAmount; i++) {
			for (int j = 0; j < userAmount; j++) {
				index[i][j] = j;
			}
		}

		for (int i = 0; i < similarity_matrix.length; i++) {
			for (int j = 0; j < similarity_matrix[i].length - 1; j++) {
				for (int k = 0; k < similarity_matrix[i].length - j - 1; k++) {
					if (similarity_matrix[i][k] < similarity_matrix[i][k + 1]) {
						double temp = similarity_matrix[i][k];
						similarity_matrix[i][k] = similarity_matrix[i][k + 1];
						similarity_matrix[i][k + 1] = temp;

						int temp1 = index[i][k + 1];
						index[i][k + 1] = index[i][k];
						index[i][k] = temp1;
					}
				}
			}
		}

		int[][] recommand = new int[UsersServices.length][top_k];

		for (int i = 0; i < index.length; i++) {
			int count = 0;
			for (int j = 0; count < top_k; j++) {
				// recommand[i][count] = index[i][j];
				for (int m = 0; m < UsersServices[index[i][j]].length; m++) {
					if (UsersServices[j][m] == 1 && count < top_k) {
						recommand[i][count] = m;
						count++;
					}
				}
			}
		}

		// Go.mian(recommand[43],top_k);
		if (stages == true) {
			Go.mian(recommand[(int) ((Math.random() * recommand.length) - 1)], top_k);
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
			// 5层的海明距离单算
		} else {
			accuracy(recommand, top_k);
		}

	}

	public void accuracy(int[][] recommand, int top_k) {
		int[] count = new int[userAmount];
		// System.out.println(userAmount);
		// System.out.println(recommand.length);
		// System.out.println(UsersServices.length);
		// System.out.println(UsersServices[0].length);

		for (int i = 0; i < userAmount; i++) {
			for (int k = 0; k < recommand[i].length; k++) {
				if (UsersServices[i][recommand[i][k]] == 1) {
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
				//System.out.println("equal:" + equal);

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

}
