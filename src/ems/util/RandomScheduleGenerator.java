package ems.util;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Random;

import org.dom4j.Element;




public class RandomScheduleGenerator {

	public static int numOfHomes = 10; 
	
	// Activity and appliance list
	public static String[] activityList = {"WatchingTV","PlayingXBOX","Reading","UsingPC","UsingLaptop","WashingDishes"
					,"Laundry","DryingClothes","Cleaning","Cooking","MakingCoffee","Sleeping"};
			
	public static String[] applianceList = {"TV","XBOX","Radio","WaterColdFan","Fan","Air-conditioner","Light","Lamp"
					,"NightLamp","PC","Laptop","WashingMachine","TumbleDryer","DishWasher","Oven","RiceCooker"
					,"Microwave","CoffeeMaker","RangeHood","VacuumCleaner"};
	
	// Activity schedule
	public static String schedule = "";
	
	// Predefined ERC model
	public static HashMap<String, ArrayList<String>> ercModel = new HashMap<String, ArrayList<String>>();
	
	
	public static void main(String[] args) {
		final String firstName = "subject";
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy:MM:dd");
		buildERCModel();
			
		for (int i = 0; i < numOfHomes; i++) {
			schedule = "";
			sleepGenerator();
			cookGenerator();
			activityGenerator();
			System.out.println(schedule);
			
			String filePath = "/Users/LeeChu/Desktop/MasterThesis/Program/PEMS/schedule_test/" + firstName + "_" + Integer.toString(i+20) + "_"
				+ dateFormat.format(new Date()) + ".xml";
		
			// Set XML file path
			XmlBuilder xmlBuilder = new XmlBuilder();
			xmlBuilder.setFilePath(filePath);
		
			HashMap<String, Integer> activityMap = new HashMap<String, Integer>();
			HashMap<String, Element> elementMap = new HashMap<String, Element>(); 
			// Create root element
			Element root = xmlBuilder.createRootElement("schedule");
			// Split whole schedule into separated activity
			String[] activityList = schedule.split("\n");
				 
			for(int j = 0; j < activityList.length; j++){
				String[] activityInfo = activityList[j].split(",");
				String activityName = activityInfo[0];
				String schedulable = activityInfo[1];
				String[] period = activityInfo[2].split(";");
				String duration = activityInfo[3];
				
				// This activity first appears
				if(!activityMap.containsKey(activityName)){
					activityMap.put(activityName, 1);
					Element activityElement = xmlBuilder.createChildElement("activity", "id", activityName, root);
					elementMap.put(activityName, activityElement);
				}
				else{
					activityMap.put(activityName, activityMap.get(activityName) + 1);
				}
				
				Element times = xmlBuilder.createChildElement("times", "id", activityMap.get(activityName).toString(), elementMap.get(activityName));
				Element schedulablePeriod = xmlBuilder.createChildElement("periodList", times);
				// Append schedulable period
				for(int k = 0; k < period.length; k++){
					Element schedulablePeriodInstance = xmlBuilder.createChildElement("period", "id", Integer.toString(k + 1), schedulablePeriod);
					String[] periodInstance = period[k].split("~"); 
					xmlBuilder.createChildElement("startTime", periodInstance[0], schedulablePeriodInstance);
					xmlBuilder.createChildElement("endTime", periodInstance[1], schedulablePeriodInstance);	
				}
				xmlBuilder.createChildElement("duration", duration, times);
				xmlBuilder.createChildElement("schedulable", schedulable, times);
				Element applianceList = xmlBuilder.createChildElement("applianceList", times);
				for(int k = 4; k < activityInfo.length; k++){
					xmlBuilder.createChildElement("appliance", "id", Integer.toString(k - 3), activityInfo[k], applianceList);
				}
			}
			try {
				xmlBuilder.write();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	public static void buildERCModel() {
		
		ArrayList<String> watchingTV = new ArrayList<String>();
		ArrayList<String> playingXBOX = new ArrayList<String>();
		ArrayList<String> reading = new ArrayList<String>();
		ArrayList<String> usingPC = new ArrayList<String>();
		ArrayList<String> usingLaptop = new ArrayList<String>();
		ArrayList<String> washingDishes = new ArrayList<String>();
		ArrayList<String> laundry = new ArrayList<String>();
		ArrayList<String> dryingClothes = new ArrayList<String>();
		ArrayList<String> cleaning = new ArrayList<String>();
		ArrayList<String> cooking = new ArrayList<String>();
		ArrayList<String> makingCoffee = new ArrayList<String>();
		ArrayList<String> sleeping = new ArrayList<String>();
		
		watchingTV.add("TV");
		watchingTV.add("Light");
		playingXBOX.add("XBOX");
		playingXBOX.add("Light");
		reading.add("Lamp");
		usingPC.add("PC");
		usingPC.add("Light");
		usingLaptop.add("Laptop");
		usingLaptop.add("Light");
		washingDishes.add("DishWasher");
		laundry.add("WashingMachine");
		dryingClothes.add("TumbleDryer");
		cleaning.add("VacuumCleaner");
		cooking.add("RangeHood");
		cooking.add("Microwave");
		makingCoffee.add("CoffeeMaker");
		sleeping.add("NightLamp");
		
		ercModel.put("WatchingTV", watchingTV);
		ercModel.put("PlayingXBOX", playingXBOX);
		ercModel.put("Reading", reading);
		ercModel.put("UsingPC", usingPC);
		ercModel.put("UsingLaptop", usingLaptop);
		ercModel.put("WashingDishes", washingDishes);
		ercModel.put("Laundry", laundry);
		ercModel.put("DryingClothes", dryingClothes);
		ercModel.put("Cleaning", cleaning);
		ercModel.put("Cooking", cooking);
		ercModel.put("MakingCoffee", makingCoffee);
		ercModel.put("Sleeping", sleeping);
	}

	// Random sleeping
	public static void sleepGenerator() {
		boolean earlySleeping = new Random().nextBoolean();
		if (earlySleeping) {
			// First period
			int startTime = new Random().nextInt(3);
			startTime += 20;
			int duration = (23 - startTime) * 60;
			schedule += "Sleeping,";
			schedule += "false,";
			schedule += Integer.toString(startTime) + ":00~23:00,";
			schedule += Integer.toString(duration) + ",";
			ArrayList<String> apps = ercModel.get("Sleeping");
			for (int i = 0; i < apps.size(); i++) {
				if (i == apps.size() - 1) {
					schedule += apps.get(i) + "\n";
				} else {
					schedule += apps.get(i) + ",";
				}
			}
			
		}
		int endTime = new Random().nextInt(6) + 4;
		int duration = endTime * 60;
		// Second period
		schedule += "Sleeping,";
		schedule += "false,";
		schedule += "00:00~0" + Integer.toString(endTime) + ":00,";
		schedule += Integer.toString(duration) + ",";
		ArrayList<String> apps = ercModel.get("Sleeping");
		for (int i = 0; i < apps.size(); i++) {
			if (i == apps.size() - 1) {
				schedule += apps.get(i) + "\n";
			} else {
				schedule += apps.get(i) + ",";
			}
		}
	}

	// Random cooking
	public static void cookGenerator() {
		for (int i = 0; i < 3; i++) {
			boolean cook = new Random().nextBoolean();
			if (cook) {
				boolean schedulable = new Random().nextBoolean();
				if (schedulable) {
					schedule += "Cooking,";
					schedule += "true,";
					if (i == 0) {
						int startTime = new Random().nextInt(8);
						int endTime = new Random().nextInt(9 - startTime - 1) + (startTime + 2);
						schedule += "0" + Integer.toString(startTime) + ":00~0" + endTime + ":00";
						schedule += ",60,";
					} else if (i == 1) {
						int startTime = new Random().nextInt(5) + 10;
						int endTime = new Random().nextInt(16 - startTime - 1) + (startTime + 2);
						schedule += Integer.toString(startTime) + ":00~" + endTime + ":00";
						schedule += ",60,";
					} else {
						int startTime = new Random().nextInt(5) + 17;
						int endTime = new Random().nextInt(23 - startTime - 1) + (startTime + 2);
						schedule += Integer.toString(startTime) + ":00~" + endTime + ":00";
						schedule += ",60,";
					}
				} else {
					schedule += "Cooking,";
					schedule += "false,";
					if (i == 0) {
						int startTime = new Random().nextInt(9);
						int endTime = startTime + 1;
						schedule += "0" + Integer.toString(startTime) + ":00~0" + endTime + ":00";
						schedule += ",60,";
					} else if (i == 1) {
						int startTime = new Random().nextInt(6) + 10;
						int endTime = startTime + 1;
						schedule += Integer.toString(startTime) + ":00~" + endTime + ":00";
						schedule += ",60,";
					} else {
						int startTime = new Random().nextInt(6) + 17;
						int endTime = startTime + 1;
						schedule += Integer.toString(startTime) + ":00~" + endTime + ":00";
						schedule += ",60,";
					}
				}
				
				// Add appliances
				ArrayList<String> apps = ercModel.get("Cooking");
				for (int j = 0; j < applianceList.length; j++) {
					if (!apps.contains(applianceList[j])) {
						double extraApp = new Random().nextDouble();
						if (extraApp > 0.8) {
							schedule += applianceList[j] + ",";
						}
					} else {
						schedule += applianceList[j] + ",";
					}
				}
				// replace last , with \n
				schedule = schedule.substring(0, schedule.length() - 1);
				schedule += "\n";
			}
		}
	}

	public static void activityGenerator() {
		for (int i = 0; i < activityList.length; i++) {
			if (!activityList[i].equals("Sleeping") && !activityList[i].equals("Cooking")) {
				boolean takePlace = new Random().nextBoolean();
				if (takePlace) {
					schedule += activityList[i] + ",";
					boolean schedulable = new Random().nextBoolean();
					int duration = new Random().nextInt(2) + 1;
					if (schedulable) {
						schedule += "true,";
						int periods = new Random().nextInt(3) + 1;
						if (periods == 1) {
							int when = new Random().nextInt(3) + 1;
							if (when == 1) {
								int startTime = new Random().nextInt(9 - duration);
								int endTime = new Random().nextInt(9 - startTime - duration) + (startTime + duration + 1);
								schedule += "0" + Integer.toString(startTime) + ":00~0" + endTime + ":00";
								schedule += "," + Integer.toString(duration * 60) + ",";
							} else if (when == 2) {
								int startTime = new Random().nextInt(6 - duration) + 10;
								int endTime = new Random().nextInt(16 - startTime - duration) + (startTime + duration + 1);
								schedule += Integer.toString(startTime) + ":00~" + endTime + ":00";
								schedule += "," + Integer.toString(duration * 60) + ",";
							} else {
								int startTime = new Random().nextInt(6 - duration) + 17;
								int endTime = new Random().nextInt(23 - startTime - duration) + (startTime + duration + 1);
								schedule += Integer.toString(startTime) + ":00~" + endTime + ":00";
								schedule += "," + Integer.toString(duration * 60) + ",";
							}
						} else if (periods == 2) {
							int when = new Random().nextInt(3) + 1;
							if (when == 1) {
								int startTime = new Random().nextInt(6 - duration) + 10;
								int endTime = new Random().nextInt(16 - startTime - duration) + (startTime + duration + 1);
								schedule += Integer.toString(startTime) + ":00~" + endTime + ":00;";
								startTime = new Random().nextInt(6 - duration) + 17;
								endTime = new Random().nextInt(23 - startTime - duration) + (startTime + duration + 1);
								schedule += Integer.toString(startTime) + ":00~" + endTime + ":00";
								schedule += "," + Integer.toString(duration * 60) + ",";
									
							} else if (when == 2) {
								int startTime = new Random().nextInt(9 - duration);
								int endTime = new Random().nextInt(9 - startTime - duration) + (startTime + duration + 1);
								schedule += "0" + Integer.toString(startTime) + ":00~0" + endTime + ":00;";
								startTime = new Random().nextInt(6 - duration) + 17;
								endTime = new Random().nextInt(23 - startTime - duration) + (startTime + duration + 1);
								schedule += Integer.toString(startTime) + ":00~" + endTime + ":00";
								schedule += "," + Integer.toString(duration * 60) + ",";
							} else {
								int startTime = new Random().nextInt(9 - duration);
								int endTime = new Random().nextInt(9 - startTime - duration) + (startTime + duration + 1);
								schedule += "0" + Integer.toString(startTime) + ":00~0" + endTime + ":00;";
								startTime = new Random().nextInt(6 - duration) + 10;
								endTime = new Random().nextInt(16 - startTime - duration) + (startTime + duration + 1);
								schedule += Integer.toString(startTime) + ":00~" + endTime + ":00";
								schedule += "," + Integer.toString(duration * 60) + ",";
							}
						} else {
							int startTime = new Random().nextInt(9 - duration);
							int endTime = new Random().nextInt(9 - startTime - duration) + (startTime + duration + 1);
							schedule += "0" + Integer.toString(startTime) + ":00~0" + endTime + ":00;";
							startTime = new Random().nextInt(6 - duration) + 10;
							endTime = new Random().nextInt(16 - startTime - duration) + (startTime + duration + 1);
							schedule += Integer.toString(startTime) + ":00~" + endTime + ":00;";
							startTime = new Random().nextInt(6 - duration) + 17;
							endTime = new Random().nextInt(23 - startTime - duration) + (startTime + duration + 1);
							schedule += Integer.toString(startTime) + ":00~" + endTime + ":00";
							schedule += "," + Integer.toString(duration * 60) + ",";
						}
					} else {
						schedule += "false,";
						int when = new Random().nextInt(3) + 1;
						if (when == 1) {
							int startTime = new Random().nextInt(9 - duration);
							int endTime = startTime + duration;
							schedule += "0" + Integer.toString(startTime) + ":00~0" + endTime + ":00";
							schedule += "," + Integer.toString(duration * 60) + ",";
						} else if (when == 2) {
							int startTime = new Random().nextInt(6 - duration) + 10;
							int endTime = startTime + duration;
							schedule += Integer.toString(startTime) + ":00~" + endTime + ":00";
							schedule += "," + Integer.toString(duration * 60) + ",";
						} else {
							int startTime = new Random().nextInt(6 - duration) + 17;
							int endTime = startTime + duration;
							schedule += Integer.toString(startTime) + ":00~" + endTime + ":00";
							schedule += "," + Integer.toString(duration * 60) + ",";
						}
					}
					ArrayList<String> apps = ercModel.get(activityList[i]);
					for (int j = 0; j < applianceList.length; j++) {
						if (!apps.contains(applianceList[j])) {
							double extraApp = new Random().nextDouble();
							if (extraApp > 0.8) {
								schedule += applianceList[j] + ",";
							}
						} else {
							schedule += applianceList[j] + ",";
						}
					}
					// replace last , with \n
					schedule = schedule.substring(0, schedule.length() - 1);
					schedule += "\n";
				}
			}
		}
	}
}
