package ems.MultipleHome;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Random;
import java.util.Set;
import java.lang.Math;

import ems.datastructure.ActivityNode;
import ems.datastructure.Environment;
import ems.singleHome.HybridScheduler;

/**
 * Created by LionKuo on 2016/1/23.
 */
public class MultiScheduler {

    public static final int TIME_SLOTS = 24;

    public static void main(String[] args){

        MultipleScheduler instance = new MultipleScheduler(args);
        //System.out.println(args.length);
        instance.run();
    }
}
