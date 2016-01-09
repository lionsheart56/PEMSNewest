package ems.datastructure;

import java.util.ArrayList;
import java.util.HashMap;

public class ActivityNode {
	String name;
	private boolean schedulability;
	//HashMap<Integer, Integer> startEndTime;  // <startTime, endTime>
	int startTime;
	int endTime;
	int duration;
	ArrayList<String> applianceList;
	double powerConsumption;
	boolean renew;
	int id;

	public ActivityNode(){
		this.applianceList = new ArrayList<String>();
		this.renew = false;
	}
	//Hi Its branch
	public void setID(int i){ this.id = i;}

	public int getID(){ return this.id; }

	public void setName(String name){
		this.name = name;
	}
	
	public String getName(){
		return this.name;
	}
	
	public void setSchedulability(boolean schedulability){
		this.schedulability = schedulability;
	}
	
	public boolean getSchedulability(){
		return this.schedulability;
	}
	
	//public void setStartEndTime(int startTime, int endTime){
	//	startEndTime.put(startTime, endTime);
	//}
	
	//public HashMap<Integer, Integer> getStartEndTime(){
	//	return this.startEndTime;
	//}

	public void setStartTime(int startTime){
		this.startTime = startTime;
	}

	public void setEndTime(int endTime){
		this.endTime = endTime;
	}

	public int getStartTime(){
		return this.startTime;
	}

	public int getEndTime(){
		return this.endTime;
	}

	public void setDuration(int duration){
		this.duration = duration;
	}
	
	public int getDuration(){
		return this.duration;
	}
	
	public void addAppliance(String appliance){
		this.applianceList.add(appliance);
	}
	
	public ArrayList<String> getApplianceList(){
		return this.applianceList;
	}
	
	public void setPowerConsumption(double powerConsumption){
		this.powerConsumption = powerConsumption;
	}
	
	public double getPowerConsumption(){
		return this.powerConsumption;
	}

	public void changeType(boolean type, int startTime, int endTime){
		this.schedulability = type;
		this.startTime = startTime;
		this.endTime = endTime;
		this.renew = true;
	}

	public boolean getRenew(){
		return this.renew;
	}
}
