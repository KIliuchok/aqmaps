package uk.ac.ed.inf.aqmaps;

public class Move {
	private int moveNumber;
	private Coordinates beforeMove;
	private Coordinates afterMove;
	private String location;
	private int direction;

	public Coordinates getBeforeMove() {
		return beforeMove;
	}

	public Coordinates getAfterMove() {
		return afterMove;
	}

	public int getMoveNum() {
		return moveNumber;
	}

	public String getLocation() {
		return location;
	}

	public int getDirection() {
		return direction;
	}

	public Move(int moveNumber, Coordinates b, Coordinates c) {
		this.moveNumber = moveNumber;
		this.beforeMove = b;
		this.afterMove = c;
	}

	public void addLocation(String loc) {
		this.location = loc;
	}

	public void addDirection(int direction) {
		this.direction = direction;
	}

}
