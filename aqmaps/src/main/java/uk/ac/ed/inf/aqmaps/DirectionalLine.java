package uk.ac.ed.inf.aqmaps;

import com.mapbox.geojson.Point;

public class DirectionalLine {
	private int direction;
	private Point estimatedPoint;
	private Point startingPoint;
	private SensorData sensorGoal;
	private double distanceToGoal;

	public DirectionalLine (int direction, Point estimated, Point original) {
		this.direction = direction;
		this.estimatedPoint = estimated;
		this.startingPoint = original;
	}
	
	public int getDirection() {
		return direction;
	}

	public Point getEstimatedPoint() {
		return estimatedPoint;
	}

	public Point getStartingPoint() {
		return startingPoint;
	}

	public SensorData getGoal() {
		return sensorGoal;
	}

	public void setGoal(SensorData goal) {
		this.sensorGoal = goal;
	}

	public double getDistanceToGoal() {
		return distanceToGoal;
	}

	public void setDistanceToGoal(double distanceToGoal) {
		this.distanceToGoal = distanceToGoal;
	}
	
	

}
