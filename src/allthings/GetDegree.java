package allthings;

public class GetDegree {

	public int getMashupDegree(int i, int[][] MA) {
		int k = 0;
		for (int j = 0; j < MA[i].length; j++)
			k += MA[i][j];
		return k;
	}

	public int getApiDegree(int i, int[][] MA) {
		int k = 0;
		for (int j = 0; j < MA.length; j++)
			k += MA[j][i];
		return k;
	}

}
