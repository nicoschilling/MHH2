package de.ismll.secondversion;

import gnu.trove.map.hash.TFloatIntHashMap;
import gnu.trove.map.hash.TIntIntHashMap;

import java.io.File;
import java.io.IOException;

import de.ismll.mhh.io.DataInterpretation;
import de.ismll.myfm.util.IO;

public class IntraSplits implements Runnable {

	private String swallowBaseDir = "/data/mhh/ECDA-Swallows";
	private String splitBaseDir = "/data/mhh/ECDA2014/Splits/intra";
	//	private String swallowBaseDir = "/home/schilling/mhh/ECDA-Swallows";


	@Override
	public void run() {

		for (int proband = 1 ; proband < 11 ; proband++) {

			for (int split=1; split < 11 ; split++) {
				
				File splitDirectory = new File(getSplitBaseDir()+File.separator+"Proband"+proband+File.separator+"Split"+split);
				
				splitDirectory.mkdirs();

				File probandDir = new File(getSwallowBaseDir()+File.separator+"Proband"+proband);

				File[] allProbandSchluckDirs = probandDir.listFiles();

				TIntIntHashMap testMap = new TIntIntHashMap();
				TIntIntHashMap valMap = new TIntIntHashMap();
				TIntIntHashMap trainMap = new TIntIntHashMap();

				for (int i = 0 ; i < allProbandSchluckDirs.length ; i++ ) {
					trainMap.put(i, 1);
				}

				TFloatIntHashMap acidsNotFoundTest = new TFloatIntHashMap();

				acidsNotFoundTest.put(7, 1);
				acidsNotFoundTest.put(5, 1);
				acidsNotFoundTest.put(3, 1);
				acidsNotFoundTest.put(1.8f, 1);

				TFloatIntHashMap acidsNotFoundVal = new TFloatIntHashMap();

				acidsNotFoundVal.put(7, 1);
				acidsNotFoundVal.put(5, 1);
				acidsNotFoundVal.put(3, 1);
				acidsNotFoundVal.put(1.8f, 1);

				boolean notCompletedTest=true;

				boolean notCompletedVal=true;


				while(notCompletedTest) {
					int pick = (int) (Math.random()*allProbandSchluckDirs.length);
					DataInterpretation currentSwallow = new DataInterpretation();
					currentSwallow.setDataInterpretation(allProbandSchluckDirs[pick]);
					currentSwallow.run();
					float acidLevel = Float.parseFloat(currentSwallow.getAcid_level() );

					if (acidsNotFoundTest.containsKey(acidLevel)) {
						testMap.put(pick, 1);
						trainMap.remove(pick);
						acidsNotFoundTest.remove(acidLevel);
					}

					if (acidsNotFoundTest.keys().length==0) {
						notCompletedTest = false;
					}


				}


				while(notCompletedVal) {
					int pick = (int) (Math.random()*allProbandSchluckDirs.length);

					if (testMap.contains(pick)) {
						continue;
					}
					DataInterpretation currentSwallow = new DataInterpretation();
					currentSwallow.setDataInterpretation(allProbandSchluckDirs[pick]);
					currentSwallow.run();
					float acidLevel = Float.parseFloat(currentSwallow.getAcid_level() );

					if (acidsNotFoundVal.containsKey(acidLevel)) {
						valMap.put(pick, 1);
						trainMap.remove(pick);
						acidsNotFoundVal.remove(acidLevel);
					}

					if (acidsNotFoundVal.keys().length==0) {
						notCompletedVal = false;
					}

				}

				System.out.println("Found 4 test and 4 val swallows for split: " + split);

				int[] trainKeys = trainMap.keys();
				int[] testKeys = testMap.keys();
				int[] valKeys = valMap.keys();
				
				String[] trainSwallows = new String[trainKeys.length];
				String[] testSwallows = new String[testKeys.length];
				String[] valSwallows = new String[valKeys.length];

				for (int train = 0; train < trainKeys.length ; train++) {
					trainSwallows[train] = String.valueOf(trainKeys[train]);
				}

				for (int val = 0; val < valKeys.length ; val++) {
					valSwallows[val] = String.valueOf(valKeys[val]);
				}

				for (int test = 0; test < testKeys.length ; test++) {
					testSwallows[test] = String.valueOf(testKeys[test]);
				}

				try {
					IO.writeStringVector(trainSwallows, new File(splitDirectory.getAbsolutePath()+File.separator+"train1"));
					IO.writeStringVector(testSwallows, new File(splitDirectory.getAbsolutePath()+File.separator+"test1"));
					IO.writeStringVector(valSwallows, new File(splitDirectory.getAbsolutePath()+File.separator+"val1"));
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}




			}

		}


	}


	public String getSwallowBaseDir() {
		return swallowBaseDir;
	}


	public void setSwallowBaseDir(String swallowBaseDir) {
		this.swallowBaseDir = swallowBaseDir;
	}


	public String getSplitBaseDir() {
		return splitBaseDir;
	}


	public void setSplitBaseDir(String splitBaseDir) {
		this.splitBaseDir = splitBaseDir;
	}



}
