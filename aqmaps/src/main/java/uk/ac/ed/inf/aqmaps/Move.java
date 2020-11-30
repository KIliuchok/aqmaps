package uk.ac.ed.inf.aqmaps;

public class Move {
	private int moveId;
	private Coordinates beforeMove;
	private Coordinates afterMove;
	public Coordinates getBeforeMove() {
		return beforeMove;
	}

	public Coordinates getAfterMove() {
		return afterMove;
	}

	public int getMoveId() {
		return moveId;
	}

	public String getLocation() {
		return location;
	}

	public int getDirection() {
		return direction;
	}

	private String location;
	private int direction;
	
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
