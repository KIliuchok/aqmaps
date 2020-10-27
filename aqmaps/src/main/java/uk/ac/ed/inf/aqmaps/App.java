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
        
        for (double i = northWest.latitude(); i < northEast.latitude(); i += 0.000001 ) {
        	for (double j = northWest.longitude(); j < southEast.longitude(); j += 0.000001) {
        		var vertex = new Vertex(i,j);
        		vertices.add(vertex);
        	}
        }
        
        
        var point = Point.fromLngLat(50, 8);
        
        
        // USE TURF TO CHECK IF IT IS INSIDE THE POLYGON
        
        turf.inside(point, noFlyAreas.features().get(0));
        
    }
}
