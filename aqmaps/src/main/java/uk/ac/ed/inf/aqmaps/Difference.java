package uk.ac.ed.inf.aqmaps;

public class Difference {
	public DirectionalLine source;
	public SensorData dest;  
	public double distance;
	
	public Difference (DirectionalLine line, SensorData sensor, double dist) {
		this.source = line;
		this.dest = sensor;
		this.distance = dist;
	}
}
