package uk.ac.ed.inf.aqmaps;

import java.util.Comparator;

public class SortByDistance implements Comparator<VectorAndGoalSensor> {

	
	public int compare(VectorAndGoalSensor arg0, VectorAndGoalSensor arg1) {
		if (arg0.getDistance() > arg1.getDistance()) {
			return 1;
		}
		return -1;		
	}

	

}
