package uk.ac.ed.inf.aqmaps;

import java.util.ArrayList;

import com.mapbox.geojson.Feature;

public class MovesAndPoints {
	
	private ArrayList<Move> moves;
	private ArrayList<Feature> features;
	
	public MovesAndPoints(ArrayList<Move> moves, ArrayList<Feature> features) {
		this.moves = moves;
		this.features = features;
	}
	
	public MovesAndPoints() {

	}
	
	public ArrayList<Move> getMoves(){
		return this.moves;
	}
	
	public ArrayList<Feature> getFeatures(){
		return this.features;
	}

}
