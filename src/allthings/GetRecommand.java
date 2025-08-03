package allthings;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;

public class GetRecommand {

	public List<List<Integer>> SinglePathRecommand(int[][] testMA,
			double[][] relation, int l, int startApiId, int topk) {

		List<List<Integer>> recommandList = new ArrayList<List<Integer>>();
		
		List<Integer> recommand = null;

		double[] a = new double[relation[0].length];

		for (int i = 0; i < a.length; i++) {
			a[i] = relation[startApiId][i];
		}

		//��һ�����Ƽ�
		List<Integer> result = this.SinglePathSelectSwap(startApiId, a, topk);

		for(int i = 0; i < result.size(); i++){
			double[] simi = new double[relation[0].length];
			int start = result.get(i);
			
			for (int j = 0; j < simi.length; j++) {
				simi[j] = relation[start][j];
			}
			
			//�ڶ������Ƽ�
			List<Integer> result_2 = this.SinglePathSelectSwap(start, simi, topk);
			
			for(int k = 0; k < result_2.size(); k++){
				double[] simi_2 = new double[relation[0].length];
				int start_2 = result_2.get(k);
				
				for(int m = 0; m < simi_2.length; m++){
					simi_2[m] = relation[start_2][m];
				}
				//���������Ƽ�
				List<Integer> result_3 = this.SinglePathSelectSwap(start_2, simi_2, topk);
				for(int n = 0; n < result_3.size(); n++){
					recommand = new ArrayList<Integer>();
					recommand.add(result.get(i));
					recommand.add(result_2.get(k));
					recommand.add(result_3.get(n));
					recommandList.add(recommand);
				}
				
			}
		}

		
		return recommandList;

	}

	/**
	 * �Ƽ�����
	 * @param target
	 * @param a
	 * @param topk
	 * @return
	 */
	private List<Integer> SinglePathSelectSwap(int target, double[] a, int topk) {
		int l = a.length;
		int index[] = new int[l];

		for (int i = 0; i < l; i++) {
			index[i] = i;
		}

		List<Integer> list = new ArrayList<Integer>();

		for (int i = 0; i < l; i++) {
			int max = i;//
			/** ���ҵ� i�������ֱ�����µ� i������λ�� ***/
			for (int j = i + 1; j < a.length; j++) {
				if (a[max] < a[j])
					max = j; // ���½ϴ���λ�ã��ٴαȽϣ�ֱ�����
			}
			/*** ����� i������λ�ò��� i,�򽻻� ****/
			if (i != max) {
				double temp1 = a[i];
				a[i] = a[max];
				a[max] = temp1;

				int temp2 = index[i];
				index[i] = index[max];
				index[max] = temp2;
			}
		}

		// ���topk��arraylist��
		int i = 0;
		for (int j = 0; j < l; j++) {
			if (i < topk && index[j] != target) {
				list.add(index[j]);
				i++;
			}
		}
		return list;
	}
	
	//�±��Ƕಽ�Ƽ�
	public DefaultTreeModel MultiPathRecommend(int[][] testMA,double[][] Similarity,int l,int startApiID,int topk){
  		
  		
  		//java�����鴫�������ķ�ʽΪ��ַ���ݣ����Ҫ����һ��double���������ڴ�Ŵ����ֵ����ֹsimilarity���޸�
  		double []a=new double[Similarity[0].length];
  		double[][]c=new double[Similarity.length][Similarity[0].length];
  		
  		for(int i=0;i<Similarity.length;i++)
  			for(int j=0;j<Similarity[0].length;j++)
  				c[i][j]=Similarity[i][j];

  		//���������API�Ƽ���
  		 DefaultMutableTreeNode root = new DefaultMutableTreeNode (startApiID);
  		 
  		//��root��ʼ���е�һ���Ƽ�
  		  List<Integer> result=new ArrayList<Integer>();
 		  //��Similarity[startApiID]��ֵ��������a
 		  for(int i=0;i<a.length;i++)
 			  a[i]=Similarity[startApiID][i];
		  result = this.SinglePathSelectSwap(startApiID,a, topk);
		  for(int i=0;i<result.size();i++){
				DefaultMutableTreeNode childnode = new DefaultMutableTreeNode (result.get(i).toString());
				root.add(childnode);
			}
  		 
         //�Ƽ����Ĳ���=�Ƽ�·������l���ӵڶ��㿪ʼ�ж��Ƿ���Ҫ�����Ƽ�	
  		 while(root.getDepth()<l){
  			//System.out.println("depth"+root.getDepth());
  			
  			Enumeration<DefaultMutableTreeNode> children = root.depthFirstEnumeration();
  			
  			 while (children.hasMoreElements()) {
  	            DefaultMutableTreeNode child = children.nextElement();
  	          DefaultMutableTreeNode child2 = child;
  	            if (child.isLeaf()){
  	            	ArrayList<Integer> target=new ArrayList<Integer>();
  	            	
  	            	//����Ѿ��Ƽ����Ľ������ArrayList target��
  	            	while(child2.isRoot()==false){
  	            		target.add(Integer.parseInt(child2.toString()));
  	            	    child2=(DefaultMutableTreeNode)child2.getParent();
  	            	}
  	            	target.add(Integer.parseInt(root.toString()));
  	            	
  	            	ArrayList<Integer> childrenresult=new ArrayList<Integer>();
  	                //��Similarity[index]��ֵ��������a

  	            	childrenresult=this.MultiPathSelectSwap(target,c, topk);
  	            	//System.out.println(childrenresult.size());
  	            	
  	            	for(int i = 0;i<childrenresult.size();i++){
          				DefaultMutableTreeNode childnode = new DefaultMutableTreeNode (childrenresult.get(i).toString());
          				//System.out.println("childnode"+childnode.toString());
          				child.add(childnode);
          				//System.out.println("length"+root.getDepth());
          			}
  	            }
  		     } 
  		 }
  		DefaultTreeModel dtm = new DefaultTreeModel(root);

		return dtm;
	}

	
	

	private ArrayList<Integer> MultiPathSelectSwap(ArrayList<Integer> target,
			double[][] c, int topk) {
		//System.out.println("�ಽ·���Ƽ�");

		double[] a = new double[c[0].length]; // double[][]cΪ���Ƶ�similarity[][],ע�ⲻ�ܽ�similarityֱ��ʹ�ã������ı�similarityֵ

		double[][] init = { { 0, 0, 0, 0, 0, 0, 0, 0 },
				{ 0, 1, 0, 0, 0, 0, 0, 0 }, { 0, 0.3, 0.7, 0, 0, 0, 0, 0 },
				{ 0, 0.1, 0.2, 0.7, 0, 0, 0, 0 },
				{ 0, 0.1, 0.2, 0.3, 0.4, 0, 0, 0 },
				{ 0, 0.1, 0.1, 0.1, 0.3, 0.4, 0, 0 },
				{ 0, 0.1, 0.1, 0.1, 0.1, 0.2, 0.4, 0 },
				{ 0, 0.1, 0.1, 0.1, 0.1, 0.1, 0.2, 0.3 } };
		double[] p = new double[init[0].length];

		// ��P���и�ֵ��p[i]��ʾ��i���Ƽ������Ȩ��
		int length = target.size(); // ����target�ĳ��Ȼ���Ѿ��Ƽ���·������

		for (int i = 0; i < init[0].length; i++)
			p[i] = init[length][i];

		// ���target�е����ݣ���ֵ������aim,ע��target�еĴ洢˳�������ӽڵ㵽���ڵ㣬aim��ӦΪ���ڵ㵽�ӽڵ�
		int[] aim = new int[target.size()];
		for (int i = 0; i < aim.length; i++)
			aim[i] = target.get(i);

		// ������Ƽ�api�����Ƽ�����api֮����ۺ�similarity
		for (int i = 0; i < a.length; i++) {
			for (int j = 1; j < aim.length; j++)
				a[i] += c[aim[j]][i] * p[j];
		}

		int index[] = new int[a.length];
		// ��index���鸳��ֵ
		for (int i = 0; i < a.length; i++)
			index[i] = i;

		ArrayList<Integer> b = new ArrayList<Integer>();

		for (int i = 0; i < a.length; i++) {
			int max = i;//
			/** ���ҵ� i�������ֱ�����µ� i������λ�� ***/
			for (int j = i + 1; j < a.length; j++) {
				if (a[max] < a[j])
					max = j; // ���½ϴ���λ�ã��ٴαȽϣ�ֱ�����
			}
			/*** ����� i������λ�ò��� i,�򽻻� ****/
			if (i != max) {
				double temp1 = a[i];
				a[i] = a[max];
				a[max] = temp1;

				int temp2 = index[i];
				index[i] = index[max];
				index[max] = temp2;
			}
		}

		// ���topk��arraylist��
		b.add(100);
		int i = 0;
		for (int j = 1; j < a.length; j++) {
			boolean same = false;
			/*
			 * for(int k=0;k<target.size();k++){
			 * //ע�⣺�ڶಽ·���Ƽ��У����б��Ƽ�����api�����ٴα��Ƽ�����ArrayList target�г��ֹ���api���ܱ��ӵ�a��
			 * if(index[j]==target.get(k)) same=true; }
			 */

			if (index[j] == target.get(0))
				same = true;
			if (i < topk && same == false) {
				b.add(index[j]);
				i++;
			}

		}
		
		return b;
	}
}
