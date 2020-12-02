package uk.ac.ed.inf.aqmaps;

public class VectorAndGoalSensor {
	private Vector sourceLine;
	private Sensor destinationSensor;
	private double distance;

	public VectorAndGoalSensor(Vector line, Sensor sensor, double dist) {
		this.sourceLine = line;
		this.destinationSensor = sensor;
		this.distance = dist;
	}

	public Vector getSourceLine() {
		return sourceLine;
	}

	public Sensor getDestinationSensor() {
		return destinationSensor;
	}

	public double getDistance() {
		return distance;
	}

}
