package ems.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Set;

import ems.datastructure.ActivityNode;
import ems.datastructure.Environment;


/*
 * This program summarize/output user's schedule into excel format
 * You better use it with shell script
 */

public class ScheduleSummarizer {

	public static void main(String[] args) {
		System.out.println("Activity	Schedulability	Duration(h)	Start time	End time");
		String schedulePath = args[0];
		Environment env = new Environment(schedulePath);
		ArrayList<ActivityNode> allSchedule = env.activitySchedule;
		for(ActivityNode actNode : allSchedule){
			String name = actNode.getName();
			boolean schedulable = actNode.getSchedulability();
			double duration = actNode.getDuration();
			// Sort start time/end time
			HashMap<Integer, Integer> startEndTime = actNode.getStartEndTime();
			Set<Integer> startTimeSet = startEndTime.keySet();
			ArrayList<Integer> startTimeList = new ArrayList<Integer>();
			startTimeList.addAll(startTimeSet);
			Collections.sort(startTimeList);
			
			System.out.print(name);
			System.out.print("	");
			if(schedulable){
				System.out.print("Schedulable");
				System.out.print("	");
			}
			else{
				System.out.print("Nonschedulable");
				System.out.print("	");
			}
			System.out.print(duration);
			System.out.print("	");
			boolean flag = true;
			for(int startTime : startTimeList){
				int endTime = startEndTime.get(startTime);
				if(flag){
					System.out.print(startTime + ":00");
					System.out.print("	");
					System.out.println(endTime + ":00");
					flag = false;
				}
				else{
					System.out.print("	");
					System.out.print("	");
					System.out.print("	");
					System.out.print(startTime + ":00");
					System.out.print("	");
					System.out.println(endTime + ":00");
				}
			}	
		}
	}
}
