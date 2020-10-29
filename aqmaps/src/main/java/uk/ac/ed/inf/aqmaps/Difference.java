package uk.ac.ed.inf.aqmaps;

public class Difference {
	public Detail source;
	public AirQualityEntry dest;  
	public double distance;
	
	public Difference (Detail duh, AirQualityEntry duh2, double dist) {
		this.source = duh;
		this.dest = duh2;
		this.distance = dist;
	}
		

}
