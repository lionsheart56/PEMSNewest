package ems.MultipleHome;

/**
 * Created by LionKuo on 2016/1/23.
 */

import java.util.*;

import ems.datastructure.ActivityNode;


public class MultipleScheduler {
    public ArrayList<SingleScheduler> allHome = new ArrayList<SingleScheduler>();
    private int numOfHome = 0;
    public List<HashMap<Integer, ArrayList<ActivityNode>>> lastStratgy = new ArrayList<HashMap<Integer, ArrayList<ActivityNode>>>();
    ArrayList<SingleScheduler> finalSol = new ArrayList<SingleScheduler>();
    public List<HashMap<Integer, ArrayList<ActivityNode>>> finalStratgy = new ArrayList<HashMap<Integer, ArrayList<ActivityNode>>>();
    //Use to store the strategy of each house.
    public List<ArrayList<Double>> allPowerUsage = new ArrayList<ArrayList<Double>>();
    public List<ArrayList<Double>> finalPowerUsage = new ArrayList<ArrayList<Double>>();
    public ArrayList<Double> lastCost = new ArrayList<Double>();
    public double lastPAR = 0.0;


    public MultipleScheduler(String[] args){

        this.numOfHome = args.length;
        for(int i = 0; i < this.numOfHome ; i++){
            SingleScheduler temp = new SingleScheduler(args[i]);
            allHome.add(temp);
            finalSol.add(temp);
        }
    }

    public void tryIt(){
        for(int i=0;i<numOfHome;i++){
            SingleScheduler temp = allHome.get(i);
            temp.PSOAlgorithm();
            allPowerUsage.add(i, temp.getPowerUsage());
            lastCost.add(i, temp.xCost);
        }
        System.out.println("Cost " + lastCost);
        System.out.println("PAR " + getPar(allPowerUsage));
        System.out.println("One: " + allHome.get(0).getPowerUsage());
        System.out.println("NExt: " + calOthers(1,allPowerUsage));

        for(int i=0;i<numOfHome;i++) {
            SingleScheduler temp = allHome.get(i);
            temp.PSOAlgorithm(calOthers(i, allPowerUsage));
            allPowerUsage.set(i, temp.getPowerUsage());
        }


    }

    public void exec(){


        for(int i=0;i<numOfHome;i++){
            SingleScheduler temp = allHome.get(i);
            temp.PSOAlgorithm();
            allPowerUsage.add(temp.getPowerUsage());
            lastCost.add(temp.xCost);
        }
        System.out.println("\n===============");
        System.out.println("PAR is  " + getPar(allPowerUsage));
        System.out.println("===============\n");
        lastPAR = getPar(allPowerUsage);

        int steps = 0;
        double minPAR = lastPAR;
        double curPAR = 0;
        ArrayList<Double> finalCost = new ArrayList<Double>();

        while(steps++ < 50){
            ArrayList<Double> curCost = new ArrayList<Double>();
            List<HashMap<Integer, ArrayList<ActivityNode>>> curStratgy = new ArrayList<HashMap<Integer, ArrayList<ActivityNode>>>();
            for(int i=0;i<numOfHome;i++){
                SingleScheduler temp = allHome.get(i);
                temp.PSOAlgorithm(calOthers(i, allPowerUsage));
               // temp.printSolution(temp.gBest);
                allPowerUsage.set(i, temp.getPowerUsage());
                finalPowerUsage.add(temp.getPowerUsage());

                curCost.add(temp.getCost());
                finalCost.add(temp.getCost());
                curStratgy.add(temp.getAllSchedule());
                finalStratgy.add(temp.getAllSchedule());
            }
            curPAR = getPar(allPowerUsage);
            //System.out.println("======= " + steps + " ==========");
            if(curPAR < minPAR && checkCost(lastCost, curCost, 1.5) ){
            //if(curPAR < minPAR){
                //System.out.println("ddd");
                minPAR = curPAR;
                for(int i=0;i<numOfHome;i++) {
                    finalSol.set(i,allHome.get(i));
                    finalCost.set(i, curCost.get(i));
                    finalStratgy.set(i, curStratgy.get(i));
                    finalPowerUsage.set(i, allPowerUsage.get(i));
                   // System.out.println("PAR is  " + curPAR);
                    //System.out.println("minPAR is  " + getPar(finalPowerUsage));
                }
            }
            finalPowerUsage.clear();
        }
        System.out.println("--------------END---------------");

        for(int i = 0;i<numOfHome;i++){
            //allPowerUsage.set(i, finalSol.get(i).getPowerUsage());
            //finalSol.get(i).printSchedule(finalSol.get(i).gBest);
            //finalSol.get(i).printSolution(finalSol.get(i).gBest);
            //finalSol.get(i).printBestResult(finalSol.get(i).gBest);
            System.out.println("Strategy:  ");
            Set<Integer> allTime = finalStratgy.get(i).keySet();
            ArrayList<Integer> allTimeList = new ArrayList<Integer>();
            allTimeList.addAll(allTime);
            Collections.sort(allTimeList);

            for (int time : allTimeList) {
                ArrayList<ActivityNode> allActivity = finalStratgy.get(i).get(time);
                System.out.print(time + ":00~" + (time+1) + ":00");
                System.out.print("	");
                List<String> tempList = new ArrayList<>();
                for (ActivityNode actNode : allActivity) {
                    System.out.print(actNode.getName() + ",");
                    tempList.add(actNode.getName());
                }
                System.out.println();
            }

            System.out.println("\nCost is " + finalCost.get(i));
            System.out.println("==================");
        }
        System.out.println("\n===============");
        System.out.println("PAR is  " + minPAR);
        //System.out.println("allPAR is  " + getPar(finalPowerUsage));
        System.out.println("===============\n");


    }

    public boolean checkCost(ArrayList<Double> lastCost, ArrayList<Double> curCost, double interest){
        boolean flag = true;
        for(int i=0;i<numOfHome;i++){

            double lC = lastCost.get(i) * interest;
            double cC = curCost.get(i);
            System.out.println(lC + " | " + cC);
            if(cC > lC){
                flag = false;
                return flag;
            }else continue;
        }

        return flag;
    }

    // The entrance of all program.
    public void run(){

        //System.out.println(lastStratgy.size());
        // 1. Do single Optimization
        for(int i=0;i<numOfHome;i++){
            SingleScheduler temp = allHome.get(i);
            temp.PSOAlgorithm();
            lastStratgy.add(i, temp.getAllSchedule());
            allPowerUsage.add(i, temp.getPowerUsage());
            lastCost.add(i, temp.getCost());
        }
        //System.out.println(calOthers(0,allPowerUsage));
        System.out.println("\n===============");
        System.out.println("PAR is  " + getPar(allPowerUsage));
        System.out.println("===============\n");
        lastPAR = getPar(allPowerUsage);
        // System.out.println(allPowerUsage.get(1));

        boolean flag = false;
        int count = 0;
        int acceptCount = 0;
        while(acceptCount < 10){
            List<HashMap<Integer, ArrayList<ActivityNode>>> curStratgy = new ArrayList<HashMap<Integer, ArrayList<ActivityNode>>>();
            ArrayList<Double> curCost = new ArrayList<Double>();
            for(int i=0;i<numOfHome;i++){
                SingleScheduler temp = allHome.get(i);
                temp.PSOAlgorithm(calOthers(i, allPowerUsage));
                allPowerUsage.set(i, temp.getPowerUsage());
                curStratgy.add(i, temp.getAllSchedule());
                curCost.add(i, temp.getCost());
            }
            double curPar = getPar(allPowerUsage);
            if(Math.abs(curPar - lastPAR) < 0.7 || Math.abs(lastPAR - curPar) < 0.53 ){
                System.out.println(curPar + " | " + lastPAR);
                acceptCount++;
            }else{
              acceptCount = 0;
            }
            lastPAR = curPar;
            /*
            for(int i=0;i<lastCost.size();i++){
                boolean temp = isAccept(lastCost.get(i), curCost.get(i));
                if(temp == false){
                    flag = false;
                    acceptCount = 0;
                    break;
                }
                flag = true;
            }
            if(flag == true){ System.out.println("Hi"); acceptCount++;}
            */



            for(int i=0;i<lastStratgy.size();i++){
                lastStratgy.set(i,curStratgy.get(i));
                lastCost.set(i,curCost.get(i));
           //     allHome.get(i).printSchedule(allHome.get(i).gBest);
             //   allHome.get(i).print();
            }

            System.out.println("======= " + count + " ==========");
            System.out.println("PAR is  " + getPar(allPowerUsage));
            count ++;

        }
        for(int i = 0;i<numOfHome;i++){
            allHome.get(i).printSchedule(allHome.get(i).gBest);
            allHome.get(i).print();
            System.out.println("==================");
        }
        System.out.println("\n===============");
        System.out.println("PAR is  " + getPar(allPowerUsage));
        System.out.println("===============\n");



    }

    public boolean isAccept(Double e, Double k){
        System.out.println(e + " | " + k);
        if (k - e < 0.5) return true;
        else return false;
    }

    public double getPar(List<ArrayList<Double>> allPowerUsage){
        double PAR = 0.0;
        double MAX = 0.0;
        double inTime = 0.0;
        double Avg = 0.0;
        for(int i=0;i<MultiScheduler.TIME_SLOTS; i++){
            inTime = 0.0;
            for(int j=0; j<allPowerUsage.size();j++) {
                double temp = allPowerUsage.get(j).get(i);
                inTime = inTime + temp;
            }  // Calculate all power in i hour
            Avg = Avg + inTime;
            if(inTime > MAX) MAX = inTime;
        }
        Avg = Avg / 24.0;
        PAR = MAX / Avg;
        return PAR;
    }

    public boolean isSame(HashMap<Integer, ArrayList<ActivityNode>> k, HashMap<Integer, ArrayList<ActivityNode>> v){
        return k.equals(v);
    }  //Done
    //Hi
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
