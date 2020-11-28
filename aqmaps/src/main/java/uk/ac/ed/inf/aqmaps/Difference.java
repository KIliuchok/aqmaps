package uk.ac.ed.inf.aqmaps;

public class Difference {
	public Detail source;
	public SensorData dest;  
	public double distance;
	
	public Difference (Detail duh, SensorData duh2, double dist) {
		this.source = duh;
		this.dest = duh2;
		this.distance = dist;
	}
		

}
