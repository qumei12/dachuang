package filehelper;


public class GetUserService {
	
	public static Integer[][] getUsersServices(){
		
		double trainset = 1;
		
		GetMA gm = new GetMA();
		int[][] MA = gm.getArrayMA();
		
		int row = (int)(MA.length * trainset);
		
		int[][] trainMA = gm.getTrainSet(MA, row);
		//int[][] testMA = gm.getTestSet(MA, row);
		Integer[][] UsersServices = new Integer[trainMA.length][trainMA[0].length];
		
		for(int i = 0;i < UsersServices.length;i++){
			for(int j = 0;j < UsersServices[i].length;j++){
				UsersServices[i][j] = trainMA[i][j];
			}
		}
		
		return UsersServices;
		
	}
}
