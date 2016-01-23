package ems.singleHome;

import java.util.Scanner;

public class Scheduler {
	public static final int TIME_SLOTS = 24;
	public boolean follow = true;
	public Scheduler(){
	}
	public static void main(String[] args) {
		String schedulePath = args[0];
		HybridScheduler hs = new HybridScheduler(schedulePath);

		hs.PSOAlgorithm();

		Scanner scanner = new Scanner(System.in);
		System.out.print("Which time you do not obey the schedule: ");
		int interruptTime = scanner.nextInt();
		System.out.print("Which activity you do instead: ");
		String interruptAct = scanner.next();
		hs.setRenew(interruptTime, interruptAct);
		hs.PSOAlgorithm();
	}
}
