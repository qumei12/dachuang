package mian;

import java.util.ArrayList;
import java.util.List;

import javax.swing.tree.DefaultTreeModel;

import allthings.GetRecommand;
import allthings.GetRelation;
import allthings.GetMA;
import allthings.Show;

public class Go {

	static int[][] trainMA;
	static int[][] testMA;

	public static List<List<Integer>> mian(int[] recommand, int topk) {
		// TODO Auto-generated method stub
		if (trainMA == null || testMA == null) {
			double trainset = 0.3;// 训练集的比例
			long start = System.currentTimeMillis();
			
			GetMA gm = new GetMA();
			int[][] MA = gm.getArrayMA();
			
			int row = (int) (MA.length * trainset);
			
			trainMA = gm.getTrainSet(MA, row);
			testMA = gm.getTestSet(MA, row);
		}

			// System.out.print("关系提取算法:");

			GetRelation gr = new GetRelation();

			// Jaccard算法
			// System.out.println("Jaccard算法");
			// double[][] relation = gr.getSimilarityByJS_REA(trainMA);

			// 物质扩散算法
			System.out.println("物质扩散算法");
			double[][] relation = gr.getSimilarityByMD_REA(trainMA);
			
		List<List<Integer>> returnList = new ArrayList<List<Integer>>();

		for (int i = 0; i < recommand.length; i++) {
			int l = 3;// 推荐树的深度
			int startApiId = recommand[i];// 起始推荐点

			// System.out.print("推荐算法：");
			GetRecommand grc = new GetRecommand();

			// 单步推荐算法
			System.out.println("单步推荐算法");
			//DefaultTreeModel dtm = grc.SinglePathRecommand(trainMA, relation, l, startApiId, topk);

			List<List<Integer>> list = grc.SinglePathRecommand(testMA, relation, l, startApiId, topk);
			//System.out.println(list);
			returnList.addAll(list);
			
			// 多步推荐算法
			// System.out.println("多步推荐算法");
			// DefaultTreeModel dtm = grc.MultiPathRecommend(testMA, relation,
			// l, startApiId, topk);

			// System.out.println("训练集比例为:" + trainset);
			// System.out.println("推荐树的深度为:" + l);
			// System.out.println("推荐起始点为:" + (startApiId + 1));
			// System.out.println("topk为:" + topk);

			//System.out.println(trainset);
			//System.out.println(l);
			//System.out.println((startApiId + 1));
			//System.out.println(topk);
			//Show s = new Show();
			//s.show(dtm, testMA);

			// trainset -= 0.1;
			//long end = System.currentTimeMillis();
			// System.out.println(end-start);
			// System.out.println();

		}
		
		return returnList;
	}
}