package de.ismll.blocknewton;

public class TestClass {


	public static void main(String[] args) {

		Data data = new Data();
		
		data.initializeSample2Label(8);
		
		
		for (int i = 0; i < 8 ; i++)
		{
			data.sample2label[i][0] = i;
		}
		
		data.sample2label[0][1] = -1;
		data.sample2label[1][1] = -1;
		data.sample2label[2][1] = -1;
		data.sample2label[3][1] = 1;
		data.sample2label[4][1] = 1;
		data.sample2label[5][1] = 1;
		data.sample2label[6][1] = -1;
		data.sample2label[7][1] = 1;
		
		data.computeUnsmoothWindows();
		
		
		for (int i = 0; i < data.unsmoothWindows.length ; i++)
		{
			System.out.println(data.unsmoothWindows[i][0] + " " + data.unsmoothWindows[i][1]);
		}
		

	}

}
