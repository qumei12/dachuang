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

		//第一步的推荐
		List<Integer> result = this.SinglePathSelectSwap(startApiId, a, topk);

		for(int i = 0; i < result.size(); i++){
			double[] simi = new double[relation[0].length];
			int start = result.get(i);
			
			for (int j = 0; j < simi.length; j++) {
				simi[j] = relation[start][j];
			}
			
			//第二步的推荐
			List<Integer> result_2 = this.SinglePathSelectSwap(start, simi, topk);
			
			for(int k = 0; k < result_2.size(); k++){
				double[] simi_2 = new double[relation[0].length];
				int start_2 = result_2.get(k);
				
				for(int m = 0; m < simi_2.length; m++){
					simi_2[m] = relation[start_2][m];
				}
				//第三步的推荐
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
	 * 推荐过程
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
			/** 查找第 i大的数，直到记下第 i大数的位置 ***/
			for (int j = i + 1; j < a.length; j++) {
				if (a[max] < a[j])
					max = j; // 记下较大数位置，再次比较，直到最大
			}
			/*** 如果第 i大数的位置不在 i,则交换 ****/
			if (i != max) {
				double temp1 = a[i];
				a[i] = a[max];
				a[max] = temp1;

				int temp2 = index[i];
				index[i] = index[max];
				index[max] = temp2;
			}
		}

		// 输出topk到arraylist中
		int i = 0;
		for (int j = 0; j < l; j++) {
			if (i < topk && index[j] != target) {
				list.add(index[j]);
				i++;
			}
		}
		return list;
	}
	
	//下边是多步推荐
	public DefaultTreeModel MultiPathRecommend(int[][] testMA,double[][] Similarity,int l,int startApiID,int topk){
  		
  		
  		//java中数组传进函数的方式为地址传递，因此要定义一个double型数组用于存放传入的值，防止similarity被修改
  		double []a=new double[Similarity[0].length];
  		double[][]c=new double[Similarity.length][Similarity[0].length];
  		
  		for(int i=0;i<Similarity.length;i++)
  			for(int j=0;j<Similarity[0].length;j++)
  				c[i][j]=Similarity[i][j];

  		//建立输出的API推荐树
  		 DefaultMutableTreeNode root = new DefaultMutableTreeNode (startApiID);
  		 
  		//从root开始进行第一层推荐
  		  List<Integer> result=new ArrayList<Integer>();
 		  //将Similarity[startApiID]的值赋给数组a
 		  for(int i=0;i<a.length;i++)
 			  a[i]=Similarity[startApiID][i];
		  result = this.SinglePathSelectSwap(startApiID,a, topk);
		  for(int i=0;i<result.size();i++){
				DefaultMutableTreeNode childnode = new DefaultMutableTreeNode (result.get(i).toString());
				root.add(childnode);
			}
  		 
         //推荐树的层数=推荐路径长度l，从第二层开始判断是否需要继续推荐	
  		 while(root.getDepth()<l){
  			//System.out.println("depth"+root.getDepth());
  			
  			Enumeration<DefaultMutableTreeNode> children = root.depthFirstEnumeration();
  			
  			 while (children.hasMoreElements()) {
  	            DefaultMutableTreeNode child = children.nextElement();
  	          DefaultMutableTreeNode child2 = child;
  	            if (child.isLeaf()){
  	            	ArrayList<Integer> target=new ArrayList<Integer>();
  	            	
  	            	//获得已经推荐过的结果存入ArrayList target中
  	            	while(child2.isRoot()==false){
  	            		target.add(Integer.parseInt(child2.toString()));
  	            	    child2=(DefaultMutableTreeNode)child2.getParent();
  	            	}
  	            	target.add(Integer.parseInt(root.toString()));
  	            	
  	            	ArrayList<Integer> childrenresult=new ArrayList<Integer>();
  	                //将Similarity[index]的值赋给数组a

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
		//System.out.println("多步路径推荐");

		double[] a = new double[c[0].length]; // double[][]c为复制的similarity[][],注意不能将similarity直接使用，否则会改变similarity值

		double[][] init = { { 0, 0, 0, 0, 0, 0, 0, 0 },
				{ 0, 1, 0, 0, 0, 0, 0, 0 }, { 0, 0.3, 0.7, 0, 0, 0, 0, 0 },
				{ 0, 0.1, 0.2, 0.7, 0, 0, 0, 0 },
				{ 0, 0.1, 0.2, 0.3, 0.4, 0, 0, 0 },
				{ 0, 0.1, 0.1, 0.1, 0.3, 0.4, 0, 0 },
				{ 0, 0.1, 0.1, 0.1, 0.1, 0.2, 0.4, 0 },
				{ 0, 0.1, 0.1, 0.1, 0.1, 0.1, 0.2, 0.3 } };
		double[] p = new double[init[0].length];

		// 对P进行赋值，p[i]表示第i步推荐代表的权重
		int length = target.size(); // 根据target的长度获得已经推荐的路径长度

		for (int i = 0; i < init[0].length; i++)
			p[i] = init[length][i];

		// 获得target中的内容，赋值给数组aim,注意target中的存储顺序是由子节点到父节点，aim中应为父节点到子节点
		int[] aim = new int[target.size()];
		for (int i = 0; i < aim.length; i++)
			aim[i] = target.get(i);

		// 计算待推荐api与已推荐出的api之间的综合similarity
		for (int i = 0; i < a.length; i++) {
			for (int j = 1; j < aim.length; j++)
				a[i] += c[aim[j]][i] * p[j];
		}

		int index[] = new int[a.length];
		// 对index数组赋初值
		for (int i = 0; i < a.length; i++)
			index[i] = i;

		ArrayList<Integer> b = new ArrayList<Integer>();

		for (int i = 0; i < a.length; i++) {
			int max = i;//
			/** 查找第 i大的数，直到记下第 i大数的位置 ***/
			for (int j = i + 1; j < a.length; j++) {
				if (a[max] < a[j])
					max = j; // 记下较大数位置，再次比较，直到最大
			}
			/*** 如果第 i大数的位置不在 i,则交换 ****/
			if (i != max) {
				double temp1 = a[i];
				a[i] = a[max];
				a[max] = temp1;

				int temp2 = index[i];
				index[i] = index[max];
				index[max] = temp2;
			}
		}

		// 输出topk到arraylist中
		b.add(100);
		int i = 0;
		for (int j = 1; j < a.length; j++) {
			boolean same = false;
			/*
			 * for(int k=0;k<target.size();k++){
			 * //注意：在多步路径推荐中，所有被推荐过的api不能再次被推荐，即ArrayList target中出现过的api不能被加到a中
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
