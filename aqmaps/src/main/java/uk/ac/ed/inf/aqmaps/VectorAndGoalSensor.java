package uk.ac.ed.inf.aqmaps;

public class VectorAndGoalSensor {
	private Vector sourceVector;
	private Sensor destinationSensor;
	private double distance;

	public VectorAndGoalSensor(Vector vector, Sensor sensor, double distance) {
		this.sourceVector = vector;
		this.destinationSensor = sensor;
		this.distance = distance;
	}

	public Vector getSourceVector() {
		return sourceVector;
	}

	public Sensor getDestinationSensor() {
		return destinationSensor;
	}

	public double getDistance() {
		return distance;
	}

}
