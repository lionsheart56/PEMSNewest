package ems.datastructure;

import java.util.ArrayList;
import java.util.HashMap;

import ems.singleHome.Scheduler;

public class Environment {
	public ArrayList<ActivityNode> activitySchedule = new ArrayList<ActivityNode>();
	public ArrayList<ActivityNode> schedulableActivity = new ArrayList<ActivityNode>();
	public ArrayList<ActivityNode> nonSchedulableActivity = new ArrayList<ActivityNode>();
	public HashMap<Integer, Double> electricityPriceProfile = new HashMap<Integer, Double>();
	public HashMap<String, Integer> applianceConsumption = new HashMap<String, Integer>();
	public HashMap<String, Double> applianceStandbyPower = new HashMap<String, Double>();
	public HashMap<Integer, Double> solarPowerProfile = new HashMap<Integer, Double>();
	
	public Environment(String schedulePath){
		setProfile(schedulePath);
	}
	
	/* Set schedule/price/appConsumption/solar profile */
	public void setProfile(String schedulePath){
		ProfileLoader loader = new ProfileLoader();
		activitySchedule = loader.loadSchedule(schedulePath);
		electricityPriceProfile = loader.loadElectricityPrice("./_input_data/electricityPrice.xml");
		solarPowerProfile = loader.loadSolar("./_input_data/re.xml", 1, 1, 1);
		applianceConsumption = loader.loadOperationEnergy("./_input_data/energyProfile.xml");
		applianceStandbyPower = loader.loadStandbyEnergy("./_input_data/energyProfile.xml");
		setActPowerConsumption();
		setSchedulabilityOfAct();
	}
	
	/* Set activity's power consumption */
	private void setActPowerConsumption(){
		for(ActivityNode actNode : activitySchedule){
			ArrayList<String> applianceList = actNode.getApplianceList();
			double powerConsumption = 0;
			for(String app : applianceList){
				powerConsumption += applianceConsumption.get(app);
			}
			actNode.setPowerConsumption(powerConsumption);
		}
	}
	
	/* Set schedulableActivity/nonSchedulabeActivity lists */
	private void setSchedulabilityOfAct(){
		for(ActivityNode actNode : activitySchedule){
			boolean schedulability = actNode.getSchedulability();
			if(schedulability) schedulableActivity.add(actNode);
			else nonSchedulableActivity.add(actNode);
		}
	}
}
