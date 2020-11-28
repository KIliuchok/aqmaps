package uk.ac.ed.inf.aqmaps;

public class Move {
	int moveId;
	Coordinates beforeMove;
	Coordinates afterMove;
	String location;
	int direction;
	
	public Move (int a, Coordinates b, Coordinates c) {
		this.moveId = a;
		this.beforeMove = b;
		this.afterMove = c;
	}
	
	public void addLocation(String loc) {
		this.location = loc;
	}
	public void addDirection(int direction) {
		this.direction = direction;
	}
	
	@Override
	public String toString() {
		return beforeMove.toString() + " " + afterMove.toString();
	}

}
