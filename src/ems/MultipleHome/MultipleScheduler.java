package ems.MultipleHome;

/**
 * Created by LionKuo on 2016/1/23.
 */


import java.util.ArrayList;

import ems.singleHome.HybridScheduler;


public class MultipleScheduler {
    public ArrayList<SingleScheduler> allHome = new ArrayList<SingleScheduler>();
    private int numOfHome = 0;

    public MultipleScheduler(String[] args){

        this.numOfHome = args.length;

        for(int i = 0; i < this.numOfHome ; i++){
            SingleScheduler temp = new SingleScheduler(args[i]);
            allHome.add(temp);
        }
    }

    // The entrance of all program.
    public void run(){

        // 1. Do single Optimization



    }

}
