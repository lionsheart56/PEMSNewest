package ems.MultipleHome;

/**
 * Created by LionKuo on 2016/1/23.
 */

import java.util.*;
import java.text.*;

import ems.datastructure.ActivityNode;


public class MultipleScheduler {
    public ArrayList<SingleScheduler> allHome = new ArrayList<SingleScheduler>();
    public ArrayList<SingleScheduler> finalSol = new ArrayList<SingleScheduler>();
    public List<HashMap<Integer, ArrayList<ActivityNode>>> finalStratgy = new ArrayList<HashMap<Integer, ArrayList<ActivityNode>>>();  //Use to store the strategy of each house.
    public List<ArrayList<Double>> allPowerUsage = new ArrayList<ArrayList<Double>>();
    public List<ArrayList<Double>> finalPowerUsage = new ArrayList<ArrayList<Double>>();
    public ArrayList<Double> lastCost = new ArrayList<Double>();
    public double lastPAR = 0.0;
    public double maxPower = 0.0;

    private int numOfHome = 0;
    private double acceptDiff = 1.0;

    public MultipleScheduler(String[] args){

        this.numOfHome = args.length;
        for(int i = 0; i < this.numOfHome ; i++){
            SingleScheduler temp = new SingleScheduler(args[i]);
            allHome.add(temp);
            finalSol.add(temp);
        }
    }

    public void exec(){

        NumberFormat nf = NumberFormat.getInstance();
        nf.setMaximumFractionDigits(3);

        System.out.println(" ===== Wait for Individual Optimization ======");
        ArrayList<Double> curCost = new ArrayList<Double>();
        ArrayList<Double> finalCost = new ArrayList<Double>();
        List<HashMap<Integer, ArrayList<ActivityNode>>> curStratgy = new ArrayList<HashMap<Integer, ArrayList<ActivityNode>>>();
        for(int i=0;i<numOfHome;i++){
            SingleScheduler temp = allHome.get(i);
            temp.PSOAlgorithm();
            allPowerUsage.add(temp.getPowerUsage());
            finalPowerUsage.add(temp.getPowerUsage());
            lastCost.add(temp.xCost);
            curCost.add(temp.getCost());
            finalCost.add(temp.xCost);
            curStratgy.add(temp.getAllSchedule());
            finalStratgy.add(temp.getAllSchedule());
        }

        lastPAR = getPar(allPowerUsage);
        System.out.println("\n===== Individual Optimization Finish ======");
        System.out.println("Now Peak-to-Average Ratio : " + nf.format(lastPAR));
        System.out.println("Now Peak Power : " + nf.format(maxPower));
        System.out.println("\n===== Cooperative Game Start ======");
        long startTime = Calendar.getInstance().getTimeInMillis();
        int steps = 0;
        double minPAR = lastPAR;
        double curPAR = 0.0;
        double newPAR = 0.0;
        double lastPower = maxPower;
        boolean change = true;
        int maxEpoch = 40;
        int iteration = 1;
        double rate = 1.4;
        double oldCost = calCost(lastCost, lastPower);

        while(change && iteration < 4) {
            System.out.println("Iteration : " + iteration++);
            while (steps++ < maxEpoch) {

                for (int i = 0; i < numOfHome; i++) {
                    SingleScheduler temp = allHome.get(i);
                    temp.PSOAlgorithm(calOthers(i, allPowerUsage));
                    allPowerUsage.set(i, temp.getPowerUsage());
                    curCost.set(i, temp.getCost());
                    curStratgy.set(i, temp.getAllSchedule());
                }
                curPAR = getPar(allPowerUsage);
                if (curPAR < minPAR && checkCost(lastCost, curCost, rate)) {
                    minPAR = curPAR;
                    for (int i = 0; i < numOfHome; i++) {
                        finalSol.set(i, allHome.get(i));
                        finalCost.set(i, curCost.get(i));
                        finalStratgy.set(i, curStratgy.get(i));
                        finalPowerUsage.set(i, allPowerUsage.get(i));
                    }
                }
            }
            newPAR = getPar(finalPowerUsage);
            double newCost = calCost(finalCost, maxPower);
            if(lastPAR < 3.0){
                acceptDiff = 0.3;
            }
            if(!isAccept(minPAR, lastPAR)){//|| !checkTotalCost(oldCost, newCost)){
                steps = 0;
                //maxEpoch +=10;
                rate += 0.2;
                System.out.println("Not Accept: [ "+ minPAR+ " ] " + "[ " + lastPAR +" ]");
                System.out.println("Now Rate : " + rate);
            }else{
                change = false;
            }
        }
        long endTime = Calendar.getInstance().getTimeInMillis();
        long duration = endTime - startTime;

        System.out.println("===== Cooperative Game Finish ======");

        for(int i = 0;i<numOfHome;i++){
            System.out.println("Strategy:  ");
            Set<Integer> allTime = finalStratgy.get(i).keySet();
            ArrayList<Integer> allTimeList = new ArrayList<Integer>();
            allTimeList.addAll(allTime);
            Collections.sort(allTimeList);

            for (int time : allTimeList) {
                ArrayList<ActivityNode> allActivity = finalStratgy.get(i).get(time);
                System.out.print(time + ":00~" + (time+1) + ":00");
                System.out.print("	");
               // List<String> tempList = new ArrayList<>();
                for (ActivityNode actNode : allActivity) {
                    System.out.print(actNode.getName() + ",");
                    //tempList.add(actNode.getName());
                }
                System.out.println();
            }
            System.out.println("\nElectricity Cost : " + finalCost.get(i));
            System.out.println("==================");
        }


        System.out.println("\n========== Final Results ========");
        System.out.println("Old Peak-to-Average Ratio : " + nf.format(lastPAR));
        System.out.println("New Peak-to-Average Ratio : " + nf.format(getPar(finalPowerUsage)));
        System.out.println("Old Peak Power : " + nf.format(lastPower));
        System.out.println("New Peak Power : " + nf.format(maxPower));
        System.out.println("Old Total Cost : " + nf.format(calCost(lastCost, lastPower) + giveYouInteger(lastPower)*236));
        System.out.println("New Total Cost : " + nf.format(calCost(finalCost, maxPower) + giveYouInteger(maxPower)*236));
        System.out.println("\n======= Computation Time ========");
        System.out.println("Second:" + nf.format(duration/1000.0));
        System.out.println("Minute:" + nf.format(duration/60000.0));
    }

    private boolean checkTotalCost(double oldC, double newC){
        if(newC >= oldC) return false;
        else return true;
    }

    public double giveYouInteger(double power){
        return Math.ceil(power);
    }

    public double calCost(ArrayList<Double> eCost, double peak){
        double basicCharge = giveYouInteger(peak) * 236;
        double sum = 0.0;
        for(int i = 0; i < numOfHome ; i++){
            sum += eCost.get(i);
        }
        return sum;
        //return sum+basicCharge;
    }

    public boolean checkCost(ArrayList<Double> lastCost, ArrayList<Double> curCost, double interest){
        boolean flag = true;
        for(int i=0;i<numOfHome;i++){

            double lC = lastCost.get(i) * interest;
            double cC = curCost.get(i);
//            System.out.println(lC + " | " + cC);
            if(cC > lC){
                flag = false;
                return flag;
            }else continue;
        }

        return flag;
    }

    public boolean isAccept(Double e, Double k){
       // System.out.println(e + " | " + k);
        if( k.equals(e)) return false;
        else if (k - e > acceptDiff) return true;
        else return false;
    }

    public double getPar(List<ArrayList<Double>> allPowerUsage){
        double PAR = 0.0;
        double MAX = 0.0;
        double inTime = 0.0;
        double Avg = 0.0;
        int hour = 0;
        for(int i=0;i<MultiScheduler.TIME_SLOTS; i++){
            inTime = 0.0;
            for(int j=0; j<allPowerUsage.size();j++) {
                double temp = allPowerUsage.get(j).get(i);
                inTime = inTime + temp;
            }  // Calculate all power in i hour
            Avg = Avg + inTime;
            if(inTime > MAX) {
                MAX = inTime;
                hour = i;
            }
        }
        Avg = Avg / 24.0;
        PAR = MAX / Avg;
        this.maxPower = MAX;
       // System.out.println("Max Hour: " + hour);
        return PAR;

    }

    public boolean isSame(HashMap<Integer, ArrayList<ActivityNode>> k, HashMap<Integer, ArrayList<ActivityNode>> v){
        return k.equals(v);
    }  //Done

    public ArrayList<Double> calOthers(int index, List<ArrayList<Double>> allPowerUsage){
        ArrayList<Double> result = new ArrayList<Double>();
        for(int i=0;i<MultiScheduler.TIME_SLOTS;i++){
            double total = 0.0;
            for(int j=0;j<allPowerUsage.size();j++){
                if( j == index) continue;
                else{
                    ArrayList<Double> temp = allPowerUsage.get(j);
                    double curPower = temp.get(i);
                    total += curPower;
                }
            }
            result.add(i,total);
        }
        return result;
    }//Done
}
