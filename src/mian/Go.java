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
			double trainset = 0.3;// ѵ�����ı���
			long start = System.currentTimeMillis();
			
			GetMA gm = new GetMA();
			int[][] MA = gm.getArrayMA();
			
			int row = (int) (MA.length * trainset);
			
			trainMA = gm.getTrainSet(MA, row);
			testMA = gm.getTestSet(MA, row);
		}

			// System.out.print("��ϵ��ȡ�㷨:");

			GetRelation gr = new GetRelation();

			// Jaccard�㷨
			// System.out.println("Jaccard�㷨");
			// double[][] relation = gr.getSimilarityByJS_REA(trainMA);

			// ������ɢ�㷨
			System.out.println("������ɢ�㷨");
			double[][] relation = gr.getSimilarityByMD_REA(trainMA);
			
		List<List<Integer>> returnList = new ArrayList<List<Integer>>();

		for (int i = 0; i < recommand.length; i++) {
			int l = 3;// �Ƽ��������
			int startApiId = recommand[i];// ��ʼ�Ƽ���

			// System.out.print("�Ƽ��㷨��");
			GetRecommand grc = new GetRecommand();

			// �����Ƽ��㷨
			System.out.println("�����Ƽ��㷨");
			//DefaultTreeModel dtm = grc.SinglePathRecommand(trainMA, relation, l, startApiId, topk);

			List<List<Integer>> list = grc.SinglePathRecommand(testMA, relation, l, startApiId, topk);
			//System.out.println(list);
			returnList.addAll(list);
			
			// �ಽ�Ƽ��㷨
			// System.out.println("�ಽ�Ƽ��㷨");
			// DefaultTreeModel dtm = grc.MultiPathRecommend(testMA, relation,
			// l, startApiId, topk);

			// System.out.println("ѵ��������Ϊ:" + trainset);
			// System.out.println("�Ƽ��������Ϊ:" + l);
			// System.out.println("�Ƽ���ʼ��Ϊ:" + (startApiId + 1));
			// System.out.println("topkΪ:" + topk);

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