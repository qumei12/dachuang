package allthings;

import java.awt.Dimension;
import java.io.IOException;

import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JTree;
import javax.swing.tree.DefaultTreeModel;

public class Show {
	public void show(DefaultTreeModel dtm, int[][] trainMA){
		JTree tree = new JTree(dtm);
		RecallRate rr = new RecallRate();
		try {
			double a = rr.getRecallRate(tree, trainMA);
			//System.out.println(a);
			System.out.println("算法召回率：" + a);
			
			double b = rr.getAccuracyRate(tree, trainMA);
			//System.out.println(b);
			System.out.println("算法准确率：" + b);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		JPanel panel = new JPanel();
		  panel.setLayout (new BoxLayout (panel, BoxLayout.Y_AXIS));
		  panel.setPreferredSize (new Dimension (500, 1000));
		  panel.add (new JScrollPane (tree));
		  
		  
		  JFrame frame = new  JFrame ( " JTreeDemo " );
		  frame.setDefaultCloseOperation (JFrame.EXIT_ON_CLOSE);
		  frame.setContentPane (panel);
		  frame.pack();
		  frame.show();;


	}
}
