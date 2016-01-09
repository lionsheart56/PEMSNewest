package ems.datastructure;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.Node;
import org.dom4j.io.SAXReader;

import ems.datastructure.ActivityNode;
import ems.util.XMLDataFetcher;

public class ProfileLoader{
	
	public ProfileLoader(){
	}
	
	/* Load schedule from specified schedule xml */
	public ArrayList<ActivityNode> loadSchedule(String filePath){
		ArrayList<ActivityNode> scheduleProfile = new ArrayList<ActivityNode>();
		File xml = new File(filePath);
		try{
			SAXReader reader = new SAXReader();
			Document document = reader.read(xml);
			List actList = document.selectNodes("schedule/activity");
			Iterator actIter = actList.iterator();
			// Go through all the activities
			while(actIter.hasNext()){
				Element actElement = (Element)actIter.next();
				// Parse activity's name
				String actName = actElement.attributeValue("id");
				// How many times will this activity happen
				List timesList = actElement.selectNodes("times");
				Iterator timesIter = timesList.iterator();
				int temp = 0;
				while(timesIter.hasNext()){
					ActivityNode actNode = new ActivityNode();
					// Set activity name
					actNode.setName(actName);
					actNode.setID(temp);
					Element timesElement = (Element)timesIter.next();
					
					// Get node/list of
					// 1. duration
					// 2. schedulability
					// 3. periodList
					// 4. applianceList
					Node durationNode = timesElement.selectSingleNode("duration");
					Node schedulabilityNode = timesElement.selectSingleNode("schedulable");
					List periodList = timesElement.selectNodes("periodList/period");
					List applianceList = timesElement.selectNodes("applianceList/appliance");
					
					// Parse and store aforementioned info
					// Duration
					int duration = Integer.parseInt(durationNode.getText());
					actNode.setDuration(duration / 60);
					// Schedulability
					boolean schedulability = Boolean.parseBoolean(schedulabilityNode.getText());
					actNode.setSchedulability(schedulability);
					// Periods <startTime, endTime>
					Iterator periodIter = periodList.iterator();
					int count = 0;
					while(periodIter.hasNext()) {
						if (count == temp) {
							Element periodElement = (Element) periodIter.next();
							Node startTimeNode = periodElement.selectSingleNode("startTime");
							Node endTimeNode = periodElement.selectSingleNode("endTime");
							String[] startTimeToken = startTimeNode.getText().split(":");
							String[] endTimeToken = endTimeNode.getText().split(":");
							int startTime = Integer.parseInt(startTimeToken[0]);
							int endTime = Integer.parseInt(endTimeToken[0]);
							actNode.setStartTime(startTime);
							actNode.setEndTime(endTime);
						}
						count++;
					}
					// Appliances
					Iterator applianceIter = applianceList.iterator();
					while(applianceIter.hasNext()){
						Element appElement = (Element)applianceIter.next();
						String appName = appElement.getText();
						actNode.addAppliance(appName);
					}
					scheduleProfile.add(actNode);
					temp++;
				}
			}
		} catch(DocumentException e){
			e.printStackTrace();
		}
		return scheduleProfile;
	}

	/* Load TOU/CPP profile */
	public HashMap<Integer, Double> loadElectricityPrice(String filePath){
		HashMap<Integer, Double> electricityPrice = new HashMap<Integer, Double>();
		File xml = new File(filePath);
		try {
			SAXReader reader = new SAXReader();
			Document document = reader.read(xml);
			// Get Time-Of-Use and Critical Peak Price
			List touCpp = document.selectNodes("/electricityPrice/TOUCPP/price");
			Iterator touCppIter = touCpp.iterator();
			while(touCppIter.hasNext()){
				Element priceElement = (Element)touCppIter.next();
				int timeSlot = Integer.parseInt(priceElement.attributeValue("id"));
				double price = Double.parseDouble(priceElement.getText());
				electricityPrice.put(timeSlot, price);
			}
		} catch (DocumentException e){
			e.printStackTrace();
		}
		return electricityPrice;
	}

	/* Load operation consumption profile of appliance */
	public HashMap<String, Integer> loadOperationEnergy(String filePath){
		HashMap<String, Integer> energyProfile = new HashMap<String, Integer>();
		File xml = new File(filePath);
		try{
			SAXReader reader = new SAXReader();
			Document document = reader.read(xml);
			List energyList = document.selectNodes("/energyConsumption/energy");
			Iterator energyIter = energyList.iterator();
			while(energyIter.hasNext()){
				Element energyElement = (Element)energyIter.next();
				String appName = energyElement.attributeValue("id");
				Element operationElement = energyElement.element("operation");
				int operationConsumption = Integer.parseInt(operationElement.getText());
				energyProfile.put(appName, operationConsumption);
			}
		} catch (DocumentException e){
			e.printStackTrace();
		}
		return energyProfile;
	}

	/* Load standby consumption profile of appliance */
	public HashMap<String, Double> loadStandbyEnergy(String filePath){
		HashMap<String, Double> energyProfile = new HashMap<String, Double>();
		File xml = new File(filePath);
		try{
			SAXReader reader = new SAXReader();
			Document document = reader.read(xml);
			List energyList = document.selectNodes("/energyConsumption/energy");
			Iterator energyIter = energyList.iterator();
			while(energyIter.hasNext()){
				Element energyElement = (Element)energyIter.next();
				String appName = energyElement.attributeValue("id");
				Element standbyElement = energyElement.element("standby");
				double standbyConsumption = Double.parseDouble(standbyElement.getText());
				energyProfile.put(appName, standbyConsumption);
			}
		} catch (DocumentException e){
			e.printStackTrace();
		}
		return energyProfile;
	}
	
	/* Load solar generation profile */
	public HashMap<Integer, Double> loadSolar(String filePath, int month, int day, int size){
		HashMap<Integer, Double> solarProfile = new HashMap<Integer, Double>();
		XMLDataFetcher fetcher = new XMLDataFetcher(filePath);
		double[] solar = fetcher.getSolarByDate(month, day);
		for(int i = 0; i < solar.length; i++){
			solarProfile.put(i, (solar[i] * size)/1000);
		}
		return solarProfile;
	}
}
