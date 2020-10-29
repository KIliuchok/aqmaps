package uk.ac.ed.inf.aqmaps;

public class Move {
	int moveId;
	Coordinates beforeMove;
	Coordinates afterMove;
	public Move (int a, Coordinates b, Coordinates c) {
		this.moveId = a;
		this.beforeMove = b;
		this.afterMove = c;
	}
	
	@Override
	public String toString() {
		return beforeMove.toString() + " " + afterMove.toString();
	}

}
