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
        }
        System.out.println(calOthers(0,allPowerUsage));
        System.out.println("===============");
        System.out.println(allPowerUsage.get(1));
    }

    public boolean isDiffer(HashMap<Integer, ArrayList<ActivityNode>> k, HashMap<Integer, ArrayList<ActivityNode>> v){
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
