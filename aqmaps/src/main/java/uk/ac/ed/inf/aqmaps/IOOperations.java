package uk.ac.ed.inf.aqmaps;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.util.List;

import com.mapbox.geojson.FeatureCollection;

public class IOOperations {

	public static void writeFileToGEOJSON (String fileName, FeatureCollection fc) throws Exception {
		FileWriter file = new FileWriter(fileName);    	
    	file.write(fc.toJson());
    	System.out.println("Successfully Created File");
    	file.close();
	}
	
	public static void writeMovesToFile(List<Move> list, String fileName) throws Exception {
		FileWriter fw = new FileWriter(fileName);
		for (Move move : list) {
			fw.write(String.format("%d,%f,%f,%d,%f,%f,%s\n", move.moveId, move.beforeMove.lng, move.beforeMove.lat,  move.direction, move.afterMove.lng, move.afterMove.lat, move.location));
		}
		fw.close();
	}
	

}
