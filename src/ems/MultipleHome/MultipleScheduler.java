package ems.MultipleHome;

/**
 * Created by LionKuo on 2016/1/23.
 */

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import ems.datastructure.ActivityNode;


public class MultipleScheduler {
    public ArrayList<SingleScheduler> allHome = new ArrayList<SingleScheduler>();
    private int numOfHome = 0;
    public List<HashMap<Integer, ArrayList<ActivityNode>>> lastStratgy = new ArrayList<HashMap<Integer, ArrayList<ActivityNode>>>();
    //Use to store the strategy of each house.
    public List<ArrayList<Double>> allPowerUsage = new ArrayList<ArrayList<Double>>();
    public ArrayList<Double> lastCost = new ArrayList<Double>();

    public MultipleScheduler(String[] args){

        this.numOfHome = args.length;
        for(int i = 0; i < this.numOfHome ; i++){
            SingleScheduler temp = new SingleScheduler(args[i]);
            allHome.add(temp);
        }
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
        // System.out.println(allPowerUsage.get(1));

        boolean flag = false;
        int count = 0;
        int acceptCount = 0;
        while(acceptCount < 20){
            List<HashMap<Integer, ArrayList<ActivityNode>>> curStratgy = new ArrayList<HashMap<Integer, ArrayList<ActivityNode>>>();
            ArrayList<Double> curCost = new ArrayList<Double>();
            for(int i=0;i<numOfHome;i++){
                SingleScheduler temp = allHome.get(i);
                temp.PSOAlgorithm(calOthers(i, allPowerUsage));
                allPowerUsage.set(i, temp.getPowerUsage());
                curStratgy.add(i, temp.getAllSchedule());
                curCost.add(i, temp.getCost());
            }

            for(int i=0;i<lastCost.size();i++){
                boolean temp = isAccept(lastCost.get(i), curCost.get(i));
                if(temp == false){
                    flag = false;
                    acceptCount = 0;
                    break;
                }
                flag = true;
            }
            if(flag == true) acceptCount++;

            for(int i=0;i<lastStratgy.size();i++){
                lastStratgy.set(i,curStratgy.get(i));
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
        if (k - e < 1) return true;
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
