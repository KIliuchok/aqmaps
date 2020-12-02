package uk.ac.ed.inf.aqmaps;

import java.util.Comparator;

public class SortByDistance implements Comparator<DirectionalLine> {

	
	public int compare(DirectionalLine arg0, DirectionalLine arg1) {
		if (arg0.getDistanceToGoal() < arg1.getDistanceToGoal()) {
			return -1;
		}
		return 1;		
	}

	

}
