package ems.datastructure;

import java.util.ArrayList;

import ems.singleHome.Scheduler;

public class HybridParticle {
	private ArrayList<Integer> scheduleData;
	private ArrayList<Double> batteryData;
	private ArrayList<Integer> pBestScheduleData;
	private ArrayList<Double> pBestBatteryData;
	private ArrayList<Double> scheduleVel;
	private ArrayList<Double> batteryVel;
	private ArrayList<Double> powerFromUtility;
	private double pBestValue = 0.0;
	
	// Initialization
	public HybridParticle(int numOfSchedulableAct){
		this.scheduleData = new ArrayList<Integer>(numOfSchedulableAct);
		this.batteryData = new ArrayList<Double>(Scheduler.TIME_SLOTS);
		this.pBestScheduleData = new ArrayList<Integer>(numOfSchedulableAct);
		this.pBestBatteryData = new ArrayList<Double>(Scheduler.TIME_SLOTS);
		this.scheduleVel = new ArrayList<Double>(numOfSchedulableAct);
		this.batteryVel = new ArrayList<Double>(Scheduler.TIME_SLOTS);
		this.powerFromUtility = new ArrayList<Double>(Scheduler.TIME_SLOTS);
		for(int i = 0; i < numOfSchedulableAct; i++){
			this.scheduleData.add(0);
			this.pBestScheduleData.add(0);
			this.scheduleVel.add(0.0);
		}
		for(int t = 0; t < Scheduler.TIME_SLOTS; t++){
			this.batteryData.add(0.0);
			this.pBestBatteryData.add(0.0);
			this.batteryVel.add(0.0);
			this.powerFromUtility.add(0.0);
		}
	}

	public void addPowerFromUtility(int key, double consumption){
		this.powerFromUtility.set(key, consumption);
	}

	public ArrayList<Double> getPower(){
		return this.powerFromUtility;
	}

	public ArrayList<Integer> getScheduleData(){
		return this.scheduleData;
	}
	
	public int getScheduleData(int index){
		return this.scheduleData.get(index);
	}
	
	public ArrayList<Double> getBatteryData(){
		return this.batteryData;
	}
	
	public double getBatteryData(int index){
		return this.batteryData.get(index);
	}
	
	public void setScheduleData(int index, int value){
		this.scheduleData.set(index, value);
	}
	
	public void setBatteryData(int index, double value){
		this.batteryData.set(index, value);
	}
	
	public ArrayList<Integer> getPBestScheduleData(){
		return this.pBestScheduleData;
	}
	
	public int getPBestScheduleData(int index){
		return this.pBestScheduleData.get(index);
	}
	
	public ArrayList<Double> getPBestBatteryData(){
		return this.pBestBatteryData;
	}
	
	public double getPBestBatteryData(int index){
		return this.pBestBatteryData.get(index);
	}
	
	public void setPBestScheduleData(int index, int value){
		this.pBestScheduleData.set(index, value);
	}
	
	public void setPBestBatteryData(int index, double value){
		this.pBestBatteryData.set(index, value);
	}
	
	public double getPBestValue(){
		return this.pBestValue;
	}
	
	public void setPBestValue(double value){
		this.pBestValue = value;
	}
	
	public ArrayList<Double> getScheduleVel(){
		return this.scheduleVel;
	}
	
	public double getScheduleVel(int index){
		return this.scheduleVel.get(index);
	}
	
	public ArrayList<Double> getBatteryVel(){
		return this.batteryVel;
	}
	
	public double getBatteryVel(int index){
		return this.batteryVel.get(index);
	}
	
	public void setScheduleVel(int index, double value){
		this.scheduleVel.set(index, value);
	}
	
	public void setBatteryVel(int index, double value){
		this.batteryVel.set(index, value);
	}
}
