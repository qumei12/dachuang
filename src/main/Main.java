package main;

import java.io.IOException;
import java.util.Scanner;

import mian.Go;
import model.ICFModel;
import model.LDAModel;
import model.UCFModel;

public class Main {


	public static void main(String[] args) throws IOException {

		boolean exit = true;
		boolean stages = false;// Ϊtrueʱ������Ƽ�

		LDAModel model = null;
		
		while (exit) {
			Scanner scanner = new Scanner(System.in);
			System.out.println("1.LDA");
			System.out.println("2.UCF");
			System.out.println("3.ICF");
			System.out.println("4.TRUST");
			System.out.println("5.EXIT");
			System.out.print("����������ѡ��");

			int choice = scanner.nextInt();

			long start = System.currentTimeMillis();
			int top_k = 3;
			switch (choice) {
			case 1:
				if(model == null){
					model = new LDAModel();
					System.out.println("1 Initialize the model ...");
					model.initializeLDAModel();
					System.out.println("2 Learning and Saving the model ...");
					model.inferenceModel();
					System.out.println("3 Output the final model ...");
					model.saveIteratedModel(model.iterations);
				}
				System.out.println("4 Generate TOP-K Recommand");
				model.topKRecommand(top_k);
				if (stages == false) {
					System.out.println("5 Test Accuracy");
					model.accuracy(top_k);// ����ʱ���ڼ���ָ���
				}
				//model.Hamming(top_k);
				break;
			case 2:

				UCFModel ucfModel = new UCFModel();
				ucfModel.recommand(top_k, stages);

				break;

			case 3:
				ICFModel icfModel = new ICFModel();
				icfModel.recommand(top_k,stages);
				break;
			case 5:
				exit = false;
				System.out.println("�˳���");
				break;
			default:
				break;
			}
			// System.out.println("Done!");
			long end = System.currentTimeMillis();
			System.out.println((end - start));
		}

	}
}
