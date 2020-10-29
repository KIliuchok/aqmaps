package uk.ac.ed.inf.aqmaps;

import java.util.Comparator;

public class SortByDistance implements Comparator<Difference> {

	
	public int compare(Difference arg0, Difference arg1) {
		if (arg0.distance < arg1.distance) {
			return -1;
		}
		return 1;		
	}

	

}
