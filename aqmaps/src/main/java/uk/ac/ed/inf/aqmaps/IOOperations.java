package uk.ac.ed.inf.aqmaps;

import java.io.FileWriter;
import java.util.List;

import com.mapbox.geojson.FeatureCollection;

public class IOOperations {

	public static void writeFeaturesToGEOJSON(String fileName, FeatureCollection fc) throws Exception {
		try {
			FileWriter file = new FileWriter(fileName);
			file.write(fc.toJson());
			System.out.println("Successfully Created File " + fileName);
			file.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void writeMovesToFile(List<Move> list, String fileName) throws Exception {
		try {
			FileWriter fw = new FileWriter(fileName);
			for (Move move : list) {
				fw.write(String.format("%d,%f,%f,%d,%f,%f,%s\n", move.getMoveNum(), move.getBeforeMove().getLng(),
						move.getBeforeMove().getLat(), move.getDirection(), move.getAfterMove().getLng(),
						move.getAfterMove().getLat(), move.getLocation()));
			}
			System.out.println("Successfully Created File " + fileName);
			fw.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
