package uk.ac.ed.inf.aqmaps;

import java.io.FileWriter;

import com.mapbox.geojson.FeatureCollection;

public class IOOperations {

	public static void writeFileToGEOJSON (String fileName, FeatureCollection fc) throws Exception {
		FileWriter file = new FileWriter(fileName);    	
    	file.write(fc.toJson());
    	System.out.println("Successfully Created File");
    	file.close();
	}

}
