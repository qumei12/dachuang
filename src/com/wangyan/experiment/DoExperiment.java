package com.wangyan.experiment;

import org.junit.Test;

public class DoExperiment {

	@Test
	public void test1() {
		for (int i = 0; i < 10; i++) {
			System.out.println(i + "---------------------");
			Experiment experiment = new Experiment();
			experiment.testFiveStage();
		}
	}
	
	@Test
	public void test2() {
		for (int i = 0; i < 10; i++) {
			System.out.println(i + "---------------------");
			Experiment experiment = new Experiment();
			experiment.testThreeStage();
		}
	}
}
