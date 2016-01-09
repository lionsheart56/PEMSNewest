package ems.singleHome;

import ems.datastructure.Environment;
import java.util.HashMap;
import java.util.List; 
import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;
import java.util.Set;
import java.util.Calendar;
import ems.datastructure.ActivityNode;


public class Scheduler {
	public static final int TIME_SLOTS = 24;
	public boolean follow = true;
	public Scheduler(){
	}
	public static void main(String[] args) {
		String schedulePath = args[0];
		HybridScheduler hs = new HybridScheduler(schedulePath);
	
		//if(follow){
		hs.PSOAlgorithm();
		//}
		//else{

		int interruptTime = 14;
		String interruptAct = "WashingDishes";
		hs.setRenew(interruptTime, interruptAct);
	



		hs.PSOAlgorithm();




	}
}
