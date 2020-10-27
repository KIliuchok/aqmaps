package uk.ac.ed.inf.aqmaps;

import com.mapbox.geojson.*;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import com.mapbox.turf.*;


import java.util.*;

public class App 
{
	
	public static final Point northWest = Point.fromLngLat(-3.192473, 55.946233);
	public static final Point northEast = Point.fromLngLat(-3.184319, 55.946233);
	public static final Point southEast = Point.fromLngLat(-3.184319, 55.942617);
	public static final Point southWest = Point.fromLngLat(-3.192473, 55.942617);
	
    public static void main( String[] args )
    {
    	final Point startingPoint = Point.fromLngLat(Double.parseDouble(args[5]), Double.parseDouble(args[4]));
    	double move = 0.0003;		
    			
    	String urlStringForPoints = "http://localhost:" + args[3] + "/maps/" + args[2] + "/" + args[0] + "/" + args[1] + "/air-quality-data.json";    	
      
        Type listType = new TypeToken<ArrayList<AirQualityEntry>>() {}.getType();
        ArrayList<AirQualityEntry> listOfEntries = new Gson().fromJson(NetworkRead.readNetworkToString(urlStringForPoints), listType);
        
        String urlStringNoFly = "http://localhost:" + args[3] + "/buildings/no-fly-zones.geojson";
        var noFlyAreas = FeatureCollection.fromJson(NetworkRead.readNetworkToString(urlStringNoFly)); 
       
        for (AirQualityEntry entry : listOfEntries) {
        	String str[] = entry.location.split("\\.");
        	var Words = new Gson().fromJson(NetworkRead.readNetworkToString("http://localhost:" + args[3] + "/words/" + str[0] + "/" + str[1] + "/" + str[2] + "/details.json"), Words.class);
        	entry.lat = Words.coordinates.lat;
        	entry.lng = Words.coordinates.lng;        	
        }
        
       List<Vertex> vertices = new LinkedList<Vertex>();
       List<Doubles> test = new ArrayList<Doubles>();
       int k = 0;
        for (double i = 55.942617; i < 55.946233; i += 0.0001 ) {
        	for (double j = 3.184319; j < 3.192473; j += 0.0001) {
        		
        		var temp1 = new Vertex(-j, i);
        		vertices.add(temp1);        		
        	} 
        	
        	System.out.println(" Row " + k);
        	k ++;
        }
        
        for (Doubles double1 : test) {
        	for (Feature feature : noFlyAreas.features()) {
        		if (TurfJoins.inside(Point.fromLngLat(double1.secondTerm, double1.firstTerm), (Polygon)feature.geometry())) {
        			System.out.println("Removed " + double1.secondTerm + " " + double1.firstTerm );
        			test.remove(double1);
        		}
        	}
        }
        
        List<Vertex> toRemove = new ArrayList();
        
        for (Vertex vertex : vertices) {
        	for (Feature feature : noFlyAreas.features()) {
        		if (TurfJoins.inside(Point.fromLngLat(vertex.longitude, vertex.latitude), (Polygon)feature.geometry())) {
        			System.out.println("Removed " + vertex.latitude + "," + vertex.longitude);
        			toRemove.add(vertex);
        		}
        	}
        }
        
        vertices.removeAll(toRemove);
        
        var point = Point.fromLngLat(-3.1866, 55.9445);
        var point2 = Point.fromLngLat(-3.1874, 55.9444);
        
        
        // USE TURF TO CHECK IF IT IS INSIDE THE POLYGON
        for (Feature feature : noFlyAreas.features()) {
        	System.out.println("It is inside (1): " + feature.getProperty("name") + TurfJoins.inside(point, (Polygon)feature.geometry()));
            System.out.println("It is inside (2): " + feature.getProperty("name") + TurfJoins.inside(point2, (Polygon)feature.geometry()));
        }
        
    }
}
