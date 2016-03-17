package ems.MultipleHome;

import java.util.*;

import ems.datastructure.ActivityNode;
import ems.datastructure.Environment;
import ems.datastructure.HybridParticle;

// How to use others profile to calculate PAR and Fitness 2016/01/24


public class SingleScheduler {
    private final int MAX_PARTICLES = 1000;
    private final int TIME_SLOTS = MultiScheduler.TIME_SLOTS;
    private final int NUM_BATTERY = 25;
    private final int BATTERY_AH = 65;
    private final int BATTERY_VOL = 12;
    private final double MAX_BATTERY_CAPACITY = (double)(NUM_BATTERY * BATTERY_AH * BATTERY_VOL) / 1000;

    public int gBest = 0;
    public double tempCost = 0.0;
    public double electricityCost = 0.0;
    public double xCost = 0.0;
    public double maxPower = 0.0;
    /* Information from _input_data */
    private ArrayList<ActivityNode> schedulableActivity = new ArrayList<ActivityNode>();
    private ArrayList<ActivityNode> nonSchedulableActivity = new ArrayList<ActivityNode>();
    private ArrayList<ActivityNode> allAct = new ArrayList<ActivityNode>();
    private HashMap<Integer, Double> solarPowerProfile = new HashMap<Integer, Double>();
    private HashMap<Integer, Double> electricityPriceProfile = new HashMap<Integer, Double>();
    private HashMap<String, Double> applianceStandbyPower = new HashMap<String, Double>();

    ArrayList<Double> totalPowerConsumption = new ArrayList<Double>(TIME_SLOTS);
    ArrayList<Double> powerFromUtility = new ArrayList<Double>(TIME_SLOTS);
    ArrayList<Double> totalElectricityCost = new ArrayList<Double>(TIME_SLOTS);
    ArrayList<Double> charge = new ArrayList<Double>(TIME_SLOTS);
    ArrayList<Double> discharge = new ArrayList<Double>(TIME_SLOTS);

    /* Used to store all the particles */
    private ArrayList<HybridParticle> particleList;

    /* Used to store all the schedule defined by user */
    private HashMap<Integer, ArrayList<ActivityNode>> allSchedule;

    /* Used to represent baseline of battery power, (solar) */
    private ArrayList<Double> batteryPower;

    private HashMap<Integer, List<String>> scheduleList;

    private ArrayList<Double> other;

    /* Get
     * 1. Schedulable activity list
     * 2. NonSchedulalbe activity list
     * 3. Solar power profile
     * 4. Electricity tariff profile
     */
    public SingleScheduler(String schedulePath) {
        // Set environmental information
        Environment env = new Environment(schedulePath);
        this.schedulableActivity = env.schedulableActivity;
        this.nonSchedulableActivity = env.nonSchedulableActivity;
        this.solarPowerProfile = env.solarPowerProfile;
        this.electricityPriceProfile = env.electricityPriceProfile;
        this.applianceStandbyPower = env.applianceStandbyPower;
        this.allAct = env.activitySchedule;

        // New instances of all objects

    }

    /* Main PSO algorithm
     * 1. Initialization
     * 2. Find global best
     * 3. Set velocity
     * 4. Update particle
     * 5. Check epoch
     */
    public void PSOAlgorithm() {

        particleList = new ArrayList<HybridParticle>(MAX_PARTICLES);
        allSchedule = new HashMap<Integer, ArrayList<ActivityNode>>();
        scheduleList = new HashMap<Integer, List<String>>();
        batteryPower = new ArrayList<Double>(TIME_SLOTS);
        for (int i = 0; i < TIME_SLOTS; i++) {
            batteryPower.add(0.0);
        }

        long startTime = Calendar.getInstance().getTimeInMillis();
        int historyGBestIndex = 0;
        int currentGBestIndex = 0;
        int epoch = 0;
        boolean done = false;
        int limitCount = 0;

        initialize();
        while (!done) {
            int MAX_EPOCHS;
            MAX_EPOCHS = 1000;
            if (epoch < MAX_EPOCHS) {
                // Get index of gBest particle for current epoch
                currentGBestIndex = getBestParticle();
                // Update gBest for current epoch
                HybridParticle currentGBest = particleList.get(currentGBestIndex);
                HybridParticle historyGBest = particleList.get(historyGBestIndex);
                double currentGBestFitness = currentGBest.getPBestValue();
                double historyGBestFitness = historyGBest.getPBestValue();
                if(currentGBestFitness - historyGBestFitness < 1){
                    limitCount +=1;
                }else if(historyGBestFitness - currentGBestFitness < 1){
                    limitCount +=1;
                }
                if (currentGBestFitness < historyGBestFitness) {
                    historyGBestIndex = currentGBestIndex;
                }

                // Update velocity for each particle
                setVelocity(historyGBestIndex);

                // Update particle according to its velocity
                updateParticle();
                epoch++;
                if(limitCount > 20) break;
            } else {
                done = true;
            }
        }
        long endTime = Calendar.getInstance().getTimeInMillis();
        long duration = endTime - startTime;

        printSchedule(historyGBestIndex);
        printSolution(historyGBestIndex);
        print();
        gBest = historyGBestIndex;

        System.out.println("Second:" + duration/1000);
        System.out.println("Minute:" + duration/60000);
    }

    public void PSOAlgorithm(ArrayList<Double> other) {

        particleList = new ArrayList<HybridParticle>(MAX_PARTICLES);
        allSchedule = new HashMap<Integer, ArrayList<ActivityNode>>();
        scheduleList = new HashMap<Integer, List<String>>();
        batteryPower = new ArrayList<Double>(TIME_SLOTS);
        for (int i = 0; i < TIME_SLOTS; i++) {
            batteryPower.add(0.0);
        }

        long startTime = Calendar.getInstance().getTimeInMillis();
        int historyGBestIndex = 0;
        int currentGBestIndex = 0;
        int epoch = 0;
        boolean done = false;
        int limitCount = 0;

        initialize(other);
        while (!done) {
            int MAX_EPOCHS;
            MAX_EPOCHS = 1000;
            if (epoch < MAX_EPOCHS) {
                // Get index of gBest particle for current epoch
                currentGBestIndex = getBestParticle();
                // Update gBest for current epoch
                HybridParticle currentGBest = particleList.get(currentGBestIndex);
                HybridParticle historyGBest = particleList.get(historyGBestIndex);
                double currentGBestFitness = currentGBest.getPBestValue();
                double historyGBestFitness = historyGBest.getPBestValue();
                if(currentGBestFitness - historyGBestFitness < 0.1){
                    limitCount +=1;
                }else if(historyGBestFitness - currentGBestFitness < 0.1){
                    limitCount +=1;
                }
                if (currentGBestFitness < historyGBestFitness) {
                    historyGBestIndex = currentGBestIndex;
                }

                // Update velocity for each particle
                setVelocity(historyGBestIndex);

                // Update particle according to its velocity
                updateParticle(other);
                epoch++;
                if(limitCount > 20) break;
            } else {
                done = true;
            }
        }
        long endTime = Calendar.getInstance().getTimeInMillis();
        long duration = endTime - startTime;

        //printSchedule(historyGBestIndex);
        printSolution(historyGBestIndex);
        gBest = historyGBestIndex;

        //System.out.println("Second:" + duration/1000);
        //System.out.println("Minute:" + duration/60000);

    }


    public HashMap<Integer, ArrayList<ActivityNode>> getAllSchedule(){
        return this.allSchedule;
    }

    public void setRenew(int interruptTime, String interruptAct){

        this.schedulableActivity.removeAll(this.schedulableActivity);
        this.nonSchedulableActivity.removeAll(nonSchedulableActivity);

        HashMap<Integer, List<String>> tempSchedule = this.getSchedule();
        HashMap<Integer, ArrayList<ActivityNode>> allSchedule = this.getAllSchedule();
        //for(ActivityNode oAct : allAct) {
        //	System.out.println(oAct.getName() + " " + oAct.getID() + " [" + oAct.getStartTime() + "=" + oAct.getEndTime() + "] " + oAct.getSchedulability() + " " + oAct.getRenew());
        //}
        //System.out.println("====================");
        Set<Integer> allTime = tempSchedule.keySet();
        ArrayList<Integer> allTimeList = new ArrayList<Integer>();
        allTimeList.addAll(allTime);
        Collections.sort(allTimeList);
        int step = 0;
        for(int i=0;i<24;i++){
            if(allTimeList.contains(i)){
                //System.out.println(i);
                int time = allTimeList.get(step);
                step++;
                ArrayList<ActivityNode> tempAct = allSchedule.get(time);   // In this time, original schedule activity.

                if(time < interruptTime){
                    for(ActivityNode actNode : tempAct) {
                        for (ActivityNode oAct : allAct) {
                            if(Objects.equals(oAct.getName(),actNode.getName()) && !oAct.getRenew()){
                                int key = oAct.getStartTime(), val = oAct.getEndTime();
                                if(time >= key && time <=val) {
                                    int endTime = oAct.getDuration() + time;
                                    oAct.changeType(false, time, endTime);
                                }else
                                    break;
                            }
                        }
                    }
                }else if(time == interruptTime){
                    for(ActivityNode oAct : allAct){
                        if (Objects.equals(oAct.getName(), interruptAct) && !oAct.getRenew()) {
                            int endTime = i + oAct.getDuration();
                            oAct.changeType(false,i,endTime);
                            break;
                        }
                    }
                }
            }else{
                if(i == interruptTime){
                    for(ActivityNode oAct : allAct){
                        if (Objects.equals(oAct.getName(), interruptAct) && !oAct.getRenew()) {
                            int endTime = i + oAct.getDuration();
                            oAct.changeType(false,i,endTime);
                            break;
                        }
                    }
                }
            }
        }
        for(ActivityNode act : allAct){
            if(act.getSchedulability()){
                int startTime = act.getStartTime();
                if(startTime < interruptTime){
                    act.changeType(act.getSchedulability(), interruptTime, act.getEndTime());
                }
            }
        }
        //for(ActivityNode oAct : allAct) {
        //		System.out.println(oAct.getName() + " " + oAct.getID() + " [" + oAct.getStartTime() + "=" + oAct.getEndTime() + "] " + oAct.getSchedulability() + " " + oAct.getRenew());
        //	}
        //	System.out.println("====================");

        for(ActivityNode actNode : allAct){
            boolean schedulability = actNode.getSchedulability();
            if(schedulability) schedulableActivity.add(actNode);
            else nonSchedulableActivity.add(actNode);
        }

    }

    public ArrayList<Double> getPowerUsage(){
        HybridParticle temp = particleList.get(gBest);
        ArrayList<Double> result = temp.getPower();
        return result;
    }
    public ArrayList<Double> getPowerUsage(int index){
        HybridParticle temp = particleList.get(index);
        ArrayList<Double> result = temp.getPower();
        return result;
    }

    /* Generate initial particles */
    private void initialize() {

        for (int i = 0; i < MAX_PARTICLES; i++) {
            int numOfSchedulableAct = schedulableActivity.size();

            // Construct a new particle
            HybridParticle newParticle = new HybridParticle(numOfSchedulableAct);

            // Random start time of each schedulable activity
            for (int j = 0; j < numOfSchedulableAct; j++) {
                // Get activity node and corresponding duration, start/end time
                ActivityNode actNode = schedulableActivity.get(j);
                int duration = actNode.getDuration();
                int startTime = actNode.getStartTime();
                int endTime = actNode.getEndTime();
                int period = endTime - duration;
                int n = period - startTime;
                double m = Math.random()*n;
                int initStartTime = (int)m+startTime;

                // Set start time of corresponding activity
                newParticle.setScheduleData(j, initStartTime);
                newParticle.setPBestScheduleData(j, initStartTime);
            }

            // Set allSchedule
            setScheduledList(newParticle.getScheduleData());


            ArrayList<Double> currentPowerConsumption = new ArrayList<Double>();
            currentPowerConsumption.clear();
            double totalPowerConsumption = 0.0;

            // Calculate power consumption
            for (int j = 0; j < TIME_SLOTS; j++) {
                HashMap<String, Double> standbyPowerClone = (HashMap<String, Double>) applianceStandbyPower.clone();
                Set<String> appSet = standbyPowerClone.keySet();
                double powerConsumption = 0.0;
                currentPowerConsumption.add(0.0);

                // Sum up power consumption of all the activities in current time slot
                // Remove appliance from standby state
                if (allSchedule.containsKey(j)) {
                    ArrayList<ActivityNode> activityList = allSchedule.get(j);
                    for (ActivityNode actNode : activityList) {
                        powerConsumption += actNode.getPowerConsumption();
                        ArrayList<String> appList = actNode.getApplianceList();

                        // Remove appliance from standby mode
                        for (String appName : appList) {
                            if (appSet.contains(appName)) {
                                appSet.remove(appName);
                            }
                        }
                    }
                }

                // Add standby power consumption
                for (String appName : appSet) {
                    double standbyPower = applianceStandbyPower.get(appName);
                    powerConsumption += standbyPower;
                }

                // Change watts to kw
                powerConsumption /= 1000;
                totalPowerConsumption += powerConsumption;
                currentPowerConsumption.set(j, powerConsumption);
            }

            for (int j = 0; j < TIME_SLOTS; j++) {
                totalPowerConsumption -= solarPowerProfile.get(j);
            }


            // Reset battery power
            for (int j = 0; j < TIME_SLOTS; j++) {
                batteryPower.set(j, 0.0);
            }

            // Random battery operation with constraints
            for (int j = 0; j < TIME_SLOTS; j++) {
                if (j == 0) {
                    batteryPower.set(j, MAX_BATTERY_CAPACITY * 0.2);
                } else {
                    // Update batteryPower according to b(t-1)
                    double currentBatteryPower = 0.0;
                    double previousBatteryPower = batteryPower.get(j - 1);
                    double previousBatteryOperation = newParticle.getBatteryData(j - 1);
                    currentBatteryPower = solarPowerProfile.get(j - 1) + (previousBatteryPower - previousBatteryOperation);
                    batteryPower.set(j, currentBatteryPower);
                }

                if (batteryPower.get(j) > MAX_BATTERY_CAPACITY) {
                    batteryPower.set(j, MAX_BATTERY_CAPACITY);
                }

                // Set maximum amount of power can be charged
                double constraint_1 = MAX_BATTERY_CAPACITY - solarPowerProfile.get(j) - batteryPower.get(j);
                double constraint_2 = (NUM_BATTERY * BATTERY_VOL * 0.3 * BATTERY_AH) / 1000;
                double maxChargeConstraint = Math.min(constraint_1, constraint_2);

                // Random feasible r
                // Goal: -(maxChargeConstraint) <= r <= b(t) - (0.2*MAX_B)
                // (1) 0 <= r <= 1
                // (2) 0 <= r <= b(t) - (0.2*MAX_B) + maxChargeConstraint
                // (3) -(maxChargeConstraint) <= r <= b(t) - (0.2*MAX_B)
                double adjustParameter_1 = batteryPower.get(j) - (0.2 * MAX_BATTERY_CAPACITY) + maxChargeConstraint;
                double adjustParameter_2 = -maxChargeConstraint;
                double batteryOperation = new Random().nextDouble() * adjustParameter_1 + adjustParameter_2;

                if (batteryOperation < 0) {
                    if (totalPowerConsumption == 0) {
                        batteryOperation = 0;
                    } else if (totalPowerConsumption > 0){
                        totalPowerConsumption += batteryOperation;
                        if (totalPowerConsumption < 0) {
                            totalPowerConsumption = 0;
                        }		//allow first over charge to reuse tomorrow ( Init Value )
                    }
                } else {
                    if (batteryOperation > currentPowerConsumption.get(j)) {
                        batteryOperation = currentPowerConsumption.get(j);
                    } 		//fang dian
                }

                // Store r
                newParticle.setBatteryData(j, batteryOperation);
                newParticle.setPBestBatteryData(j, batteryOperation);
            }

            // Evaluate and store particle
            double fitnessValue = particleEval(newParticle);
            newParticle.setPBestValue(fitnessValue);
            particleList.add(newParticle);
        }
    }
    private void initialize(ArrayList<Double> other) {

        for (int i = 0; i < MAX_PARTICLES; i++) {
            int numOfSchedulableAct = schedulableActivity.size();

            // Construct a new particle
            HybridParticle newParticle = new HybridParticle(numOfSchedulableAct);

            // Random start time of each schedulable activity
            for (int j = 0; j < numOfSchedulableAct; j++) {
                // Get activity node and corresponding duration, start/end time
                ActivityNode actNode = schedulableActivity.get(j);
                int duration = actNode.getDuration();
                int startTime = actNode.getStartTime();
                int endTime = actNode.getEndTime();
                int period = endTime - duration;
                int n = period - startTime;
                double m = Math.random()*n;
                int initStartTime = (int)m+startTime;

                // Set start time of corresponding activity
                newParticle.setScheduleData(j, initStartTime);
                newParticle.setPBestScheduleData(j, initStartTime);
            }

            // Set allSchedule
            setScheduledList(newParticle.getScheduleData());


            ArrayList<Double> currentPowerConsumption = new ArrayList<Double>();
            currentPowerConsumption.clear();
            double totalPowerConsumption = 0.0;

            // Calculate power consumption
            for (int j = 0; j < TIME_SLOTS; j++) {
                HashMap<String, Double> standbyPowerClone = (HashMap<String, Double>) applianceStandbyPower.clone();
                Set<String> appSet = standbyPowerClone.keySet();
                double powerConsumption = 0.0;
                currentPowerConsumption.add(0.0);

                // Sum up power consumption of all the activities in current time slot
                // Remove appliance from standby state
                if (allSchedule.containsKey(j)) {
                    ArrayList<ActivityNode> activityList = allSchedule.get(j);
                    for (ActivityNode actNode : activityList) {
                        powerConsumption += actNode.getPowerConsumption();
                        ArrayList<String> appList = actNode.getApplianceList();

                        // Remove appliance from standby mode
                        for (String appName : appList) {
                            if (appSet.contains(appName)) {
                                appSet.remove(appName);
                            }
                        }
                    }
                }

                // Add standby power consumption
                for (String appName : appSet) {
                    double standbyPower = applianceStandbyPower.get(appName);
                    powerConsumption += standbyPower;
                }

                // Change watts to kw
                powerConsumption /= 1000;
                totalPowerConsumption += powerConsumption;
                currentPowerConsumption.set(j, powerConsumption);
            }

            for (int j = 0; j < TIME_SLOTS; j++) {
                totalPowerConsumption -= solarPowerProfile.get(j);
            }


            // Reset battery power
            for (int j = 0; j < TIME_SLOTS; j++) {
                batteryPower.set(j, 0.0);
            }

            // Random battery operation with constraints
            for (int j = 0; j < TIME_SLOTS; j++) {
                if (j == 0) {
                    batteryPower.set(j, MAX_BATTERY_CAPACITY * 0.2);
                } else {
                    // Update batteryPower according to b(t-1)
                    double currentBatteryPower = 0.0;
                    double previousBatteryPower = batteryPower.get(j - 1);
                    double previousBatteryOperation = newParticle.getBatteryData(j - 1);
                    currentBatteryPower = solarPowerProfile.get(j - 1) + (previousBatteryPower - previousBatteryOperation);
                    batteryPower.set(j, currentBatteryPower);
                }

                if (batteryPower.get(j) > MAX_BATTERY_CAPACITY) {
                    batteryPower.set(j, MAX_BATTERY_CAPACITY);
                }

                // Set maximum amount of power can be charged
                double constraint_1 = MAX_BATTERY_CAPACITY - solarPowerProfile.get(j) - batteryPower.get(j);
                double constraint_2 = (NUM_BATTERY * BATTERY_VOL * 0.3 * BATTERY_AH) / 1000;
                double maxChargeConstraint = Math.min(constraint_1, constraint_2);

                // Random feasible r
                // Goal: -(maxChargeConstraint) <= r <= b(t) - (0.2*MAX_B)
                // (1) 0 <= r <= 1
                // (2) 0 <= r <= b(t) - (0.2*MAX_B) + maxChargeConstraint
                // (3) -(maxChargeConstraint) <= r <= b(t) - (0.2*MAX_B)
                double adjustParameter_1 = batteryPower.get(j) - (0.2 * MAX_BATTERY_CAPACITY) + maxChargeConstraint;
                double adjustParameter_2 = -maxChargeConstraint;
                double batteryOperation = new Random().nextDouble() * adjustParameter_1 + adjustParameter_2;

                if (batteryOperation < 0) {
                    if (totalPowerConsumption == 0) {
                        batteryOperation = 0;
                    } else if (totalPowerConsumption > 0){
                        totalPowerConsumption += batteryOperation;
                        if (totalPowerConsumption < 0) {
                            totalPowerConsumption = 0;
                        }		//allow first over charge to reuse tomorrow ( Init Value )
                    }
                } else {
                    if (batteryOperation > currentPowerConsumption.get(j)) {
                        batteryOperation = currentPowerConsumption.get(j);
                    } 		//fang dian
                }

                // Store r
                newParticle.setBatteryData(j, batteryOperation);
                newParticle.setPBestBatteryData(j, batteryOperation);
            }

            // Evaluate and store particle
            double fitnessValue = particleEval(newParticle);
            newParticle.setPBestValue(fitnessValue);
            particleList.add(newParticle);
        }
    }
    /* Calculate Fitness */
    private double particleEval(HybridParticle particle) {
        // Set allSchedule
        setScheduledList(particle.getScheduleData());

        // Calculate electricity cost
        double electricityCost = 0;

        // Reset battery power
        for (int i = 0; i < TIME_SLOTS; i++) {
            batteryPower.set(i, 0.0);
        }

        for (int i = 0; i < TIME_SLOTS; i++) {
            HashMap<String, Double> standbyPowerClone = (HashMap<String, Double>) applianceStandbyPower.clone();
            Set<String> appSet = standbyPowerClone.keySet();
            double currentPowerConsumption = 0;

            // Sum up power consumption of all the activities in current time slot
            // Remove appliance from standby state
            if (allSchedule.containsKey(i)) {
                ArrayList<ActivityNode> activityList = allSchedule.get(i);
                for (ActivityNode actNode : activityList) {
                    currentPowerConsumption += actNode.getPowerConsumption();
                    ArrayList<String> appList = actNode.getApplianceList();

                    // Remove appliance from standby mode
                    for (String appName : appList) {
                        if (appSet.contains(appName)) {
                            appSet.remove(appName);
                        }
                    }
                }
            }

            // Add standby power consumption
            for (String appName : appSet) {
                double standbyPower = applianceStandbyPower.get(appName);
                currentPowerConsumption += standbyPower;
            }

            // Change watts to kw
            currentPowerConsumption /= 1000;

            if (i == 0) {
                batteryPower.set(i, 0.2 * MAX_BATTERY_CAPACITY);
            } else {
                // Update batteryPower according to b(t-1)
                double currentBatteryPower = 0.0;
                double previousBatteryPower = batteryPower.get(i - 1);
                double previousBatteryOperation = particle.getBatteryData(i - 1);
                currentBatteryPower = solarPowerProfile.get(i - 1) + (previousBatteryPower - previousBatteryOperation);
                batteryPower.set(i, currentBatteryPower);
            }

            if (batteryPower.get(i) > MAX_BATTERY_CAPACITY) {
                batteryPower.set(i, MAX_BATTERY_CAPACITY);
            }

            // Set quantity of charge/discharge
            double batteryOperation = particle.getBatteryData(i);
            double currentBatteryPower = batteryPower.get(i);
            double charge = 0.0;
            double discharge = 0.0;
            if (batteryOperation < 0) {
                charge = Math.abs(batteryOperation);
            } else {
                discharge = batteryOperation;
            }

            // Calculate electricity cost
            double currentElectricityPrice = electricityPriceProfile.get(i);
            double neededGridPower = currentPowerConsumption - discharge;
            if (neededGridPower < 0) {
                neededGridPower = 0;
            }
            particle.addPowerFromUtility(i,neededGridPower + charge);
            electricityCost += (neededGridPower + charge) * currentElectricityPrice;
        }
        //tempCost = electricityCost;
        return electricityCost;
    }
    private double particleEval(HybridParticle particle, ArrayList<Double> other) {
        // Set allSchedule
        setScheduledList(particle.getScheduleData());

        // Calculate electricity cost
        double electricityCost = 0;

        // Reset battery power
        for (int i = 0; i < TIME_SLOTS; i++) {
            batteryPower.set(i, 0.0);
        }

        for (int i = 0; i < TIME_SLOTS; i++) {
            HashMap<String, Double> standbyPowerClone = (HashMap<String, Double>) applianceStandbyPower.clone();
            Set<String> appSet = standbyPowerClone.keySet();
            double currentPowerConsumption = 0;

            // Sum up power consumption of all the activities in current time slot
            // Remove appliance from standby state
            if (allSchedule.containsKey(i)) {
                ArrayList<ActivityNode> activityList = allSchedule.get(i);
                for (ActivityNode actNode : activityList) {
                    currentPowerConsumption += actNode.getPowerConsumption();
                    ArrayList<String> appList = actNode.getApplianceList();

                    // Remove appliance from standby mode
                    for (String appName : appList) {
                        if (appSet.contains(appName)) {
                            appSet.remove(appName);
                        }
                    }
                }
            }

            // Add standby power consumption
            for (String appName : appSet) {
                double standbyPower = applianceStandbyPower.get(appName);
                currentPowerConsumption += standbyPower;
            }

            // Change watts to kw
            currentPowerConsumption /= 1000;

            if (i == 0) {
                batteryPower.set(i, 0.2 * MAX_BATTERY_CAPACITY);
            } else {
                // Update batteryPower according to b(t-1)
                double currentBatteryPower = 0.0;
                double previousBatteryPower = batteryPower.get(i - 1);
                double previousBatteryOperation = particle.getBatteryData(i - 1);
                currentBatteryPower = solarPowerProfile.get(i - 1) + (previousBatteryPower - previousBatteryOperation);
                batteryPower.set(i, currentBatteryPower);
            }

            if (batteryPower.get(i) > MAX_BATTERY_CAPACITY) {
                batteryPower.set(i, MAX_BATTERY_CAPACITY);
            }

            // Set quantity of charge/discharge
            double batteryOperation = particle.getBatteryData(i);
            double currentBatteryPower = batteryPower.get(i);
            double charge = 0.0;
            double discharge = 0.0;
            if (batteryOperation < 0) {
                charge = Math.abs(batteryOperation);
            } else {
                discharge = batteryOperation;
            }

            // Calculate electricity cost
            double currentElectricityPrice = electricityPriceProfile.get(i);
            double neededGridPower = currentPowerConsumption - discharge;
            if (neededGridPower < 0) {
                neededGridPower = 0;
            }
            particle.addPowerFromUtility(i,neededGridPower + charge);
            electricityCost += (neededGridPower + charge) * currentElectricityPrice;
        }


        double par = getPar(this.getPowerUsage(), other);
        tempCost = electricityCost;
        return par;
        //return electricityCost;
    }

    private double particleEval(HybridParticle particle, ArrayList<Double> other, int index) {
        // Set allSchedule
        setScheduledList(particle.getScheduleData());

        // Calculate electricity cost
        double electricityCost = 0;

        // Reset battery power
        for (int i = 0; i < TIME_SLOTS; i++) {
            batteryPower.set(i, 0.0);
        }

        for (int i = 0; i < TIME_SLOTS; i++) {
            HashMap<String, Double> standbyPowerClone = (HashMap<String, Double>) applianceStandbyPower.clone();
            Set<String> appSet = standbyPowerClone.keySet();
            double currentPowerConsumption = 0;

            // Sum up power consumption of all the activities in current time slot
            // Remove appliance from standby state
            if (allSchedule.containsKey(i)) {
                ArrayList<ActivityNode> activityList = allSchedule.get(i);
                for (ActivityNode actNode : activityList) {
                    currentPowerConsumption += actNode.getPowerConsumption();
                    ArrayList<String> appList = actNode.getApplianceList();

                    // Remove appliance from standby mode
                    for (String appName : appList) {
                        if (appSet.contains(appName)) {
                            appSet.remove(appName);
                        }
                    }
                }
            }

            // Add standby power consumption
            for (String appName : appSet) {
                double standbyPower = applianceStandbyPower.get(appName);
                currentPowerConsumption += standbyPower;
            }

            // Change watts to kw
            currentPowerConsumption /= 1000;

            if (i == 0) {
                batteryPower.set(i, 0.2 * MAX_BATTERY_CAPACITY);
            } else {
                // Update batteryPower according to b(t-1)
                double currentBatteryPower = 0.0;
                double previousBatteryPower = batteryPower.get(i - 1);
                double previousBatteryOperation = particle.getBatteryData(i - 1);
                currentBatteryPower = solarPowerProfile.get(i - 1) + (previousBatteryPower - previousBatteryOperation);
                batteryPower.set(i, currentBatteryPower);
            }

            if (batteryPower.get(i) > MAX_BATTERY_CAPACITY) {
                batteryPower.set(i, MAX_BATTERY_CAPACITY);
            }

            // Set quantity of charge/discharge
            double batteryOperation = particle.getBatteryData(i);
            double currentBatteryPower = batteryPower.get(i);
            double charge = 0.0;
            double discharge = 0.0;
            if (batteryOperation < 0) {
                charge = Math.abs(batteryOperation);
            } else {
                discharge = batteryOperation;
            }

            // Calculate electricity cost
            double currentElectricityPrice = electricityPriceProfile.get(i);
            double neededGridPower = currentPowerConsumption - discharge;
            if (neededGridPower < 0) {
                neededGridPower = 0;
            }
            particle.addPowerFromUtility(i,neededGridPower + charge);
            electricityCost += (neededGridPower + charge) * currentElectricityPrice;
        }

        double par = getPar(this.getPowerUsage(index), other);

        return par;
        //return electricityCost;
    }

    public double getPar(ArrayList<Double> own, ArrayList<Double> other){
        double PAR = 0.0;
        double MAX = 0.0;
        double Avg = 0.0;
        for(int i=0;i<MultiScheduler.TIME_SLOTS; i++){
            double temp = own.get(i) + other.get(i);
            Avg = Avg + temp;
            if(temp > MAX) MAX = temp;
        }
        Avg = Avg / 24.0;
        PAR = MAX / Avg;
        this.maxPower = MAX;
        return PAR;
    }

    /* Return the best particle at current epoch */
    private int getBestParticle() {
        int bestParticleIndex = 0;
        for (int i = 1; i < MAX_PARTICLES; i++) {
            HybridParticle bestParticle = particleList.get(bestParticleIndex);
            HybridParticle currentParticle = particleList.get(i);
            double bestFitness = bestParticle.getPBestValue();
            double currentFitness = currentParticle.getPBestValue();
            if (currentFitness < bestFitness) {
                bestParticleIndex = i;
            }
        }
        return bestParticleIndex;
    }

    private void setVelocity(int historyGBestIndex) {
        double vValue = 0.0;
        int numOfSchedulableAct = schedulableActivity.size();
        HybridParticle particle = null;
        HybridParticle gBestParticle = particleList.get(historyGBestIndex);

        for (int i = 0; i < MAX_PARTICLES; i++) {
            particle = particleList.get(i);
            double randomParameter_1 = new Random().nextDouble();
            double randomParameter_2 = new Random().nextDouble();
            double s_max = 5;
            double b_max = 10;
            double weight_w = 0;
            double w = 1;

            // Set schedule velocity
            for (int j = 0; j < numOfSchedulableAct; j++) {
                weight_w = w * particle.getScheduleVel(j);

                // Calculate velocity value
                vValue = weight_w + 2 * randomParameter_1 * (particle.getPBestScheduleData(j) - particle.getScheduleData(j))
                        + 2 * randomParameter_2 * (gBestParticle.getPBestScheduleData(j) - particle.getScheduleData(j));

                // Bound velocity value
                if (vValue > s_max) {
                    vValue = s_max;
                } else if(vValue < -s_max) {
                    vValue = -s_max;
                }

                // Set velocity value
                particle.setScheduleVel(j, vValue);
            }

            // Set battery velocity
            for(int j = 0; j < TIME_SLOTS; j++){
                weight_w = w * particle.getBatteryVel(j);

                // Calculate velocity value
                vValue = weight_w + 2 * randomParameter_1 * (particle.getPBestBatteryData(j) - particle.getBatteryData(j))
                        + 2 * randomParameter_2 * (gBestParticle.getPBestBatteryData(j) - particle.getBatteryData(j));

                // Bound velocity value
                if(vValue > b_max){
                    vValue = b_max;
                } else if(vValue < -b_max){
                    vValue = -b_max;
                }

                // Set velocity value
                particle.setBatteryVel(j, vValue);
            }
        }
    }

    /* Move particle and check feasibility and update pBest if necessary */
    private void updateParticle() {
        int numOfSchedulableAct = schedulableActivity.size();
        for (int i = 0; i < MAX_PARTICLES; i++) {
            HybridParticle currentParticle = particleList.get(i);

            // Set allSchedule list
            setScheduledList(currentParticle.getScheduleData());

            for(int j = 0 ; j < numOfSchedulableAct; j++){

                //boolean isValid = true;   // Check old startime + velocity is valid
                int oldStartTime = currentParticle.getScheduleData(j);
                double vel = currentParticle.getScheduleVel(j);
                int newStartTime = (int)(oldStartTime + Math.round(vel));

                ActivityNode actNode = schedulableActivity.get(j);
                int preferStart = actNode.getStartTime();
                int preferEnd = actNode.getEndTime();
                int preferDuration = actNode.getDuration();
                int deadline = preferEnd - preferDuration;
                if(deadline < preferStart) System.out.println("Fuck you");
                if(newStartTime > deadline){
                    currentParticle.setScheduleData(j, deadline);
                }else if(newStartTime < preferStart){
                    currentParticle.setScheduleData(j, preferStart);
                }else
                    currentParticle.setScheduleData(j, newStartTime);
            }

            // Reset battery power
            for (int j = 0; j < TIME_SLOTS; j++) {
                batteryPower.set(j, 0.0);
            }

            for (int j = 0; j < TIME_SLOTS; j++) {
                HashMap<String, Double> standbyPowerClone = (HashMap<String, Double>) applianceStandbyPower.clone();
                Set<String> appSet = standbyPowerClone.keySet();

                // Calculate power consumption as constraint fix value
                double currentPowerConsumption = 0;

                // Sum up power consumption of all activities in current time slot
                if (allSchedule.containsKey(j)) {
                    ArrayList<ActivityNode> activityList = allSchedule.get(j);
                    for (ActivityNode actNode : activityList) {
                        currentPowerConsumption += actNode.getPowerConsumption();
                        ArrayList<String> appList = actNode.getApplianceList();

                        // Remove appliance from standby mode
                        for (String appName : appList) {
                            if (appSet.contains(appName)) {
                                appSet.remove(appName);
                            }
                        }
                    }
                }

                // Add standby power consumption
                for (String appName : appSet) {
                    double standbyPower = applianceStandbyPower.get(appName);
                    currentPowerConsumption += standbyPower;
                }

                // Change watts to kw
                currentPowerConsumption /= 1000;

                if (j == 0) {
                    batteryPower.set(j, MAX_BATTERY_CAPACITY * 0.2);
                } else {
                    // Update battery power according to b(t-1)
                    double currentBatteryPower = 0.0;
                    double previousBatteryPower = batteryPower.get(j - 1);
                    double previousBatteryOperation = currentParticle.getBatteryData(j - 1);
                    currentBatteryPower = solarPowerProfile.get(j - 1) + (previousBatteryPower - previousBatteryOperation);
                    batteryPower.set(j, currentBatteryPower);
                }

                if (batteryPower.get(j) > MAX_BATTERY_CAPACITY) {
                    batteryPower.set(j, MAX_BATTERY_CAPACITY);
                }

                // Update new battery operation according to constraints
                // Here we set value to boundary if it exceeds the boundary
                double oldBatteryOperation = currentParticle.getBatteryData(j);
                double velocity = currentParticle.getBatteryVel(j);
                double newBatteryOperation = oldBatteryOperation + velocity;
                double currentBatteryPower = batteryPower.get(j);
                double constraint_1 = MAX_BATTERY_CAPACITY - solarPowerProfile.get(j) - currentBatteryPower;
                double constraint_2 = (NUM_BATTERY * BATTERY_VOL * 0.3 * BATTERY_AH) / 1000;
                double maxChargeConstraint = Math.min(constraint_1, constraint_2);
                double upperBound = currentBatteryPower - 0.2 * MAX_BATTERY_CAPACITY;
                double lowerBound = -maxChargeConstraint;

                // We deal with 3 kinds of bad operations
                // 1. If overuse the battery power
                // 2. If exceeding the upper bound
                // 3. If exceeding the lower bound
                if (currentPowerConsumption == 0 && batteryPower.get(j) == 0.2 * MAX_BATTERY_CAPACITY) {
                    if (newBatteryOperation > 0) {
                        newBatteryOperation = 0;
                    } else if (newBatteryOperation < lowerBound) {
                        newBatteryOperation = lowerBound;
                    }
                } else if (newBatteryOperation > currentPowerConsumption && newBatteryOperation <= upperBound) {
                    if (currentPowerConsumption < batteryPower.get(j)) {
                        newBatteryOperation = currentPowerConsumption;
                    }
                } else if (newBatteryOperation > upperBound) {
                    newBatteryOperation = upperBound;
                } else if (newBatteryOperation < lowerBound) {
                    newBatteryOperation = lowerBound;
                }

                // Set new data
                currentParticle.setBatteryData(j, newBatteryOperation);
            }

            // Update pBestData and pBestValue if particle's currentFitness < pPbestFitness
            double currentFitness = particleEval(currentParticle);
            double pBestFitness = currentParticle.getPBestValue();
            if (currentFitness < pBestFitness) {
                currentParticle.setPBestValue(currentFitness);
                currentParticle.setBestCost(tempCost);
                for (int j = 0; j < numOfSchedulableAct; j++) {
                    int newStartTime = currentParticle.getScheduleData(j);
                    currentParticle.setPBestScheduleData(j, newStartTime);
                }
                for (int j = 0; j < TIME_SLOTS; j++) {
                    double newBatteryUsage = currentParticle.getBatteryData(j);
                    currentParticle.setPBestBatteryData(j, newBatteryUsage);
                }
            }
        }
    }

    private void updateParticle(ArrayList<Double> other) {
        int numOfSchedulableAct = schedulableActivity.size();
        for (int i = 0; i < MAX_PARTICLES; i++) {
            HybridParticle currentParticle = particleList.get(i);

            // Set allSchedule list
            setScheduledList(currentParticle.getScheduleData());

            for(int j = 0 ; j < numOfSchedulableAct; j++){

                //boolean isValid = true;   // Check old startime + velocity is valid
                int oldStartTime = currentParticle.getScheduleData(j);
                double vel = currentParticle.getScheduleVel(j);
                int newStartTime = (int)(oldStartTime + Math.round(vel));

                ActivityNode actNode = schedulableActivity.get(j);
                int preferStart = actNode.getStartTime();
                int preferEnd = actNode.getEndTime();
                int preferDuration = actNode.getDuration();
                int deadline = preferEnd - preferDuration;
                if(deadline < preferStart) System.out.println("Fuck you");
                if(newStartTime > deadline){
                    currentParticle.setScheduleData(j, deadline);
                }else if(newStartTime < preferStart){
                    currentParticle.setScheduleData(j, preferStart);
                }else
                    currentParticle.setScheduleData(j, newStartTime);
            }

            // Reset battery power
            for (int j = 0; j < TIME_SLOTS; j++) {
                batteryPower.set(j, 0.0);
            }

            for (int j = 0; j < TIME_SLOTS; j++) {
                HashMap<String, Double> standbyPowerClone = (HashMap<String, Double>) applianceStandbyPower.clone();
                Set<String> appSet = standbyPowerClone.keySet();

                // Calculate power consumption as constraint fix value
                double currentPowerConsumption = 0;

                // Sum up power consumption of all activities in current time slot
                if (allSchedule.containsKey(j)) {
                    ArrayList<ActivityNode> activityList = allSchedule.get(j);
                    for (ActivityNode actNode : activityList) {
                        currentPowerConsumption += actNode.getPowerConsumption();
                        ArrayList<String> appList = actNode.getApplianceList();

                        // Remove appliance from standby mode
                        for (String appName : appList) {
                            if (appSet.contains(appName)) {
                                appSet.remove(appName);
                            }
                        }
                    }
                }

                // Add standby power consumption
                for (String appName : appSet) {
                    double standbyPower = applianceStandbyPower.get(appName);
                    currentPowerConsumption += standbyPower;
                }

                // Change watts to kw
                currentPowerConsumption /= 1000;

                if (j == 0) {
                    batteryPower.set(j, MAX_BATTERY_CAPACITY * 0.2);
                } else {
                    // Update battery power according to b(t-1)
                    double currentBatteryPower = 0.0;
                    double previousBatteryPower = batteryPower.get(j - 1);
                    double previousBatteryOperation = currentParticle.getBatteryData(j - 1);
                    currentBatteryPower = solarPowerProfile.get(j - 1) + (previousBatteryPower - previousBatteryOperation);
                    batteryPower.set(j, currentBatteryPower);
                }

                if (batteryPower.get(j) > MAX_BATTERY_CAPACITY) {
                    batteryPower.set(j, MAX_BATTERY_CAPACITY);
                }

                // Update new battery operation according to constraints
                // Here we set value to boundary if it exceeds the boundary
                double oldBatteryOperation = currentParticle.getBatteryData(j);
                double velocity = currentParticle.getBatteryVel(j);
                double newBatteryOperation = oldBatteryOperation + velocity;
                double currentBatteryPower = batteryPower.get(j);
                double constraint_1 = MAX_BATTERY_CAPACITY - solarPowerProfile.get(j) - currentBatteryPower;
                double constraint_2 = (NUM_BATTERY * BATTERY_VOL * 0.3 * BATTERY_AH) / 1000;
                double maxChargeConstraint = Math.min(constraint_1, constraint_2);
                double upperBound = currentBatteryPower - 0.2 * MAX_BATTERY_CAPACITY;
                double lowerBound = -maxChargeConstraint;

                // We deal with 3 kinds of bad operations
                // 1. If overuse the battery power
                // 2. If exceeding the upper bound
                // 3. If exceeding the lower bound
                if (currentPowerConsumption == 0 && batteryPower.get(j) == 0.2 * MAX_BATTERY_CAPACITY) {
                    if (newBatteryOperation > 0) {
                        newBatteryOperation = 0;
                    } else if (newBatteryOperation < lowerBound) {
                        newBatteryOperation = lowerBound;
                    }
                } else if (newBatteryOperation > currentPowerConsumption && newBatteryOperation <= upperBound) {
                    if (currentPowerConsumption < batteryPower.get(j)) {
                        newBatteryOperation = currentPowerConsumption;
                    }
                } else if (newBatteryOperation > upperBound) {
                    newBatteryOperation = upperBound;
                } else if (newBatteryOperation < lowerBound) {
                    newBatteryOperation = lowerBound;
                }

                // Set new data
                currentParticle.setBatteryData(j, newBatteryOperation);
            }

            // Update pBestData and pBestValue if particle's currentFitness < pPbestFitness
            double currentFitness = particleEval(currentParticle, other);
            double pBestFitness = currentParticle.getPBestValue();
            if (currentFitness < pBestFitness) {
                currentParticle.setPBestValue(currentFitness);
                currentParticle.setBestCost(tempCost);
                for (int j = 0; j < numOfSchedulableAct; j++) {
                    int newStartTime = currentParticle.getScheduleData(j);
                    currentParticle.setPBestScheduleData(j, newStartTime);
                }
                for (int j = 0; j < TIME_SLOTS; j++) {
                    double newBatteryUsage = currentParticle.getBatteryData(j);
                    currentParticle.setPBestBatteryData(j, newBatteryUsage);
                }
            }
        }
    }

    public HashMap<Integer, List<String>> getSchedule(){
        return this.scheduleList;
    }

    /* Print out details of the best solution */
    public void printSolution(int historyGBestIndex) {
        HybridParticle particle = particleList.get(historyGBestIndex);

        // Set all schedule list
        setScheduledList(particle.getPBestScheduleData());
        /*
        ArrayList<Double> totalPowerConsumption = new ArrayList<Double>(TIME_SLOTS);
        ArrayList<Double> powerFromUtility = new ArrayList<Double>(TIME_SLOTS);
        ArrayList<Double> totalElectricityCost = new ArrayList<Double>(TIME_SLOTS);
        ArrayList<Double> charge = new ArrayList<Double>(TIME_SLOTS);
        ArrayList<Double> discharge = new ArrayList<Double>(TIME_SLOTS);
        */
        for (int i = 0; i < TIME_SLOTS; i++) {
            charge.add(0.0);
            discharge.add(0.0);
            totalPowerConsumption.add(0.0);
            powerFromUtility.add(0.0);
            totalElectricityCost.add(0.0);
        }
        electricityCost = 0;

        // Reset battery state
        for (int i = 0; i < TIME_SLOTS; i++) {
            batteryPower.set(i, 0.0);
        }

        for (int i = 0; i < TIME_SLOTS; i++) {
            HashMap<String, Double> standbyPowerClone = (HashMap<String, Double>) applianceStandbyPower.clone();
            Set<String> appSet = standbyPowerClone.keySet();

            // Calculate power consumption as constraint fix value
            double currentPowerConsumption = 0;

            // Sum up power consumption of all the activities in current time slot
            if (allSchedule.containsKey(i)) {
                ArrayList<ActivityNode> activityList = allSchedule.get(i);
                for (ActivityNode actNode : activityList) {
                    currentPowerConsumption += actNode.getPowerConsumption();
                    ArrayList<String> appList = actNode.getApplianceList();

                    // Remove appliance from standby mode
                    for(String appName : appList){
                        if(appSet.contains(appName)){
                            appSet.remove(appName);
                        }
                    }
                }
            }

            // Add standby power consumption
            for(String appName : appSet){
                double standbyPower = applianceStandbyPower.get(appName);
                currentPowerConsumption += standbyPower;
            }

            // Change watts to kw
            currentPowerConsumption /= 1000;

            totalPowerConsumption.set(i, currentPowerConsumption);

            if (i == 0) {
                batteryPower.set(i, 0.2 * MAX_BATTERY_CAPACITY);
            } else {
                // Update battery power according to b(t-1)
                double currentBatteryPower = 0.0;
                double previousBatteryPower = batteryPower.get(i - 1);
                double previousBatteryOperation = particle.getPBestBatteryData(i - 1);
                currentBatteryPower = solarPowerProfile.get(i - 1) + (previousBatteryPower - previousBatteryOperation);
                batteryPower.set(i, currentBatteryPower);
            }

            if (batteryPower.get(i) > MAX_BATTERY_CAPACITY) {
                batteryPower.set(i, MAX_BATTERY_CAPACITY);
            }

            // Set quantity of charge/discharge
            double batteryOperation = particle.getPBestBatteryData(i);
            if (batteryOperation < 0) {
                charge.set(i, Math.abs(batteryOperation));
            } else {
                discharge.set(i, batteryOperation);
            }

            // Calculate electricity cost
            double currentElectricityPrice = electricityPriceProfile.get(i);
            double neededGridPower = currentPowerConsumption - discharge.get(i);
            if(neededGridPower < 0) neededGridPower = 0;
            double currentElectricityCost = (neededGridPower + charge.get(i)) * currentElectricityPrice;
            powerFromUtility.set(i, neededGridPower + charge.get(i));
            totalElectricityCost.set(i, currentElectricityCost);
            electricityCost += currentElectricityCost;
        }

    }

    public double getCost(){
        HybridParticle particle = particleList.get(gBest);
        return particle.getBestCost();
    }

    public void print(){
        System.out.println("Algorithm	Time	Power Consumption	Power From Utility Company	Discharge	Charge	Solar Power	Battery State	Electricity Cost");
        for (int i = 0; i < TIME_SLOTS; i++) {
            System.out.print("Our Method");
            System.out.print("\t");
            System.out.print(i);
            System.out.print("\t");
            System.out.print((double)Math.round(totalPowerConsumption.get(i) * 1000) / 1000);
            System.out.print("\t");
            System.out.print((double)Math.round(powerFromUtility.get(i) * 1000) / 1000);
            System.out.print("\t");
            System.out.print((double)Math.round(discharge.get(i) * 1000) / 1000);
            System.out.print("\t");
            System.out.print((double)Math.round(charge.get(i) * 1000) / 1000);
            System.out.print("\t");
            System.out.print((double)Math.round(solarPowerProfile.get(i) * 1000) / 1000);
            System.out.print("\t");
            System.out.print((double)Math.round(batteryPower.get(i) * 1000) / 1000);
            System.out.print("\t");
            System.out.println((double)Math.round(totalElectricityCost.get(i) * 1000) / 1000);
        }
        xCost = electricityCost;
        System.out.println("Total Electricity Cost:" + (double)Math.round(electricityCost * 1000) / 1000);
    }

    /* Print out the final schedule */
    public void printSchedule(int historyGBestIndex) {
        HybridParticle particle = particleList.get(historyGBestIndex);

        // Sort final schedule
        setScheduledList(particle.getPBestScheduleData());
        Set<Integer> allTime = allSchedule.keySet();
        ArrayList<Integer> allTimeList = new ArrayList<Integer>();
        allTimeList.addAll(allTime);
        Collections.sort(allTimeList);

        // Print final schedule
        for (int time : allTimeList) {
            ArrayList<ActivityNode> allActivity = allSchedule.get(time);
            System.out.print(time + ":00~" + (time+1) + ":00");
            System.out.print("	");
            List<String> tempList = new ArrayList<>();
            for (ActivityNode actNode : allActivity) {
                System.out.print(actNode.getName() + ",");
                tempList.add(actNode.getName());
            }
            scheduleList.put(time, tempList);
            System.out.println();
        }
    }

    public void printBestResult(int historyGBestIndex) {
        HybridParticle particle = particleList.get(historyGBestIndex);
        System.out.println("Cost:" + particle.getPBestValue());
    }

    /* Set allscheudle list */
    public void setScheduledList(ArrayList<Integer> list) {
        allSchedule.clear();
        // Add schedulable activity to allSchedule
        int numOfSchedulableAct = schedulableActivity.size();
        for (int i = 0; i < numOfSchedulableAct; i++) {
            // For each actNode, we get start time/duration
            ActivityNode actNode = schedulableActivity.get(i);
            int startTime = list.get(i);
            int duration = actNode.getDuration();
            // Add to allSchedule
            for (int j = 0; j < duration; j++) {
                int currentTime = startTime + j;
                // If this is the first activity in currentTime slot
                if (!allSchedule.containsKey(currentTime)) {
                    ArrayList<ActivityNode> tmp = new ArrayList<ActivityNode>();
                    tmp.add(actNode);
                    allSchedule.put(currentTime, tmp);
                } else {
                    ArrayList<ActivityNode> tmp = allSchedule.get(currentTime);
                    tmp.add(actNode);
                    allSchedule.put(currentTime, tmp);
                }
            }
        }

        // Add Nonschedulable activity to allSchedule
        int numOfNonSchedulableAct = nonSchedulableActivity.size();
        for (int i = 0; i < numOfNonSchedulableAct; i++) {
            // For each actNode, we get start time/duration
            ActivityNode actNode = nonSchedulableActivity.get(i);
            int startTime = actNode.getStartTime();
            int duration = actNode.getDuration();

            // Add to allSchedule
            for (int j = 0; j < duration; j++) {
                int currentTime = startTime + j;
                // If this is the first activity in currentTime slot
                if (!allSchedule.containsKey(currentTime)) {
                    ArrayList<ActivityNode> tmp = new ArrayList<ActivityNode>();
                    tmp.add(actNode);
                    allSchedule.put(currentTime, tmp);
                } else {
                    ArrayList<ActivityNode> tmp = allSchedule.get(currentTime);
                    tmp.add(actNode);
                    allSchedule.put(currentTime, tmp);
                }
            }
        }
    }
}
