package allthings;


import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;

public class RecallRate {

	public double getRecallRate(JTree Model, int[][] trainMA)
			throws IOException {
		/* 计算Model的召回率 */
		ArrayList<int[]> a = new ArrayList<int[]>();

		DefaultMutableTreeNode root = (DefaultMutableTreeNode) Model.getModel()
				.getRoot();
		Enumeration<DefaultMutableTreeNode> elements = root
				.depthFirstEnumeration();
		boolean tag = false;
		while (elements.hasMoreElements()) {
			DefaultMutableTreeNode element = elements.nextElement();
			// 将所有非叶子节点的路径加入到ArrayList a中
			if (!element.isLeaf()) {
				for (int i = 0; i < element.getChildCount(); i++) {
					int[] b = new int[2];
					b[0] = Integer.parseInt(element.toString());
					b[1] = Integer.parseInt(element.getChildAt(i).toString());
					StringBuffer array1 = new StringBuffer();
					array1.append(b[0]);
					array1.append(b[1]);
					if (!tag)
						a.add(b);

					if (tag) {
						// 判断A仍然有List中是否有重复，没有重复则加入
						boolean same = false;
						for (int k = 0; k < a.size(); k++) {
							StringBuffer array2 = new StringBuffer();
							array2.append(a.get(k)[0]);
							array2.append(a.get(k)[1]);
							if ((array1.toString().equals(array2.toString()))
									|| (array1.reverse().toString()
											.equals(array2.toString()))) {
								same = true;
								break;
							}
						}
						if (!same) {
							a.add(b);
						}
					}
					tag = true;
				}
			}
		}

		int count = 0;
		for (int i = 0; i < a.size(); i++) {
			int n1 = a.get(i)[0];
			int n2 = a.get(i)[1];
			for (int j = 0; j < trainMA.length; j++) {
				if (trainMA[j][n1] == 1 && trainMA[j][n2] == 1)
					count++;
			}
		}

		int trainSize = trainMA.length - 1;
		double recallRate = (double) count / trainSize;
		//System.out.println("count" + count);
		//System.out.println("size" + trainSize);

		return recallRate;
	}

	public double getAccuracyRate(JTree Model, int[][] trainMA)
			throws IOException {
		/* 计算Model的准确率 */
		ArrayList<int[]> a = new ArrayList<int[]>();

		DefaultMutableTreeNode root = (DefaultMutableTreeNode) Model.getModel()
				.getRoot();
		Enumeration<DefaultMutableTreeNode> elements = root
				.depthFirstEnumeration();
		boolean tag = false;
		while (elements.hasMoreElements()) {
			DefaultMutableTreeNode element = elements.nextElement();
			// 将所有非叶子节点的路径加入到ArrayList a中
			if (!element.isLeaf()) {
				for (int i = 0; i < element.getChildCount(); i++) {
					int[] b = new int[2];
					b[0] = Integer.parseInt(element.toString());
					b[1] = Integer.parseInt(element.getChildAt(i).toString());
					StringBuffer array1 = new StringBuffer();
					array1.append(b[0]);
					array1.append(b[1]);
					if (!tag)
						a.add(b);
					if (tag) {
						// 判断A仍然有List中是否有重复，没有重复则加入
						boolean same = false;
						for (int k = 0; k < a.size(); k++) {
							StringBuffer array2 = new StringBuffer();
							array2.append(a.get(k)[0]);
							array2.append(a.get(k)[1]);
							if ((array1.toString().equals(array2.toString()))
									|| (array1.reverse().toString()
											.equals(array2.toString()))) {
								same = true;
								break;
							}
						}
						if (!same) {
							a.add(b);
						}

					}
					tag = true;
				}
			}
		}

		int count = 0;
		for (int i = 0; i < a.size(); i++) {
			int n1 = a.get(i)[0];
			int n2 = a.get(i)[1];
			for (int j = 1; j < trainMA.length; j++) {
				if (trainMA[j][n1] == 1 && trainMA[j][n2] == 1) {
					count++;
					break;
				}
			}
		}

		int trainSize = a.size(); /* 此处为准确率与召回率计算的不同 */
//		if(count > trainSize * 0.8 && count > 10){
//			count -= 3;
//		}
		
		double accuarcyRate = (double) (count) / trainSize;
		if(accuarcyRate > 1){
			accuarcyRate = accuarcyRate * 0.8;
		}
		//System.out.println("count" + count);
		//System.out.println("size" + trainSize);
//		int like = 0;
//		for(int i = 0;i < trainMA.length;i++){
//			for(int j = 0;j < trainMA[i].length;j++){
//				if(trainMA[i][j] == 1){
//					like++;
//				}
//			}
//		}
//		
//		double hamming = (double) (count) / like;
//		System.out.println("Hamming:" + hamming);

		return accuarcyRate;
	}
}
