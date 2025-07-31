package fileout;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class FileOut {
	
	public static void printMatrix(int[][] a,String name){
		File file = new File(name + ".txt");
		
		BufferedWriter bw = null;
		try {
			FileWriter fw = new FileWriter(file);
			
			bw = new BufferedWriter(fw);
			
			for(int i = 0;i < a.length;i++){
				for(int j = 0;j < a[i].length;j++){
					try {
						bw.write(a[i][j] + "\t");
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				try {
					bw.newLine();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			if (bw != null) {
				try {
					bw.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		
	}
	
	
	public static void printMatrix(double[][] a,String name){
		File file = new File(name + ".txt");
		
		BufferedWriter bw = null;
		try {
			FileWriter fw = new FileWriter(file);
			
			bw = new BufferedWriter(fw);
			
			for(int i = 0;i < a.length;i++){
				for(int j = 0;j < a[i].length;j++){
					try {
						bw.write(a[i][j] + "\t");
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				try {
					bw.newLine();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			if (bw != null) {
				try {
					bw.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		
	}
}
