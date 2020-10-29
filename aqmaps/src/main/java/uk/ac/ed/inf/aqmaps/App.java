package uk.ac.ed.inf.aqmaps;

import com.mapbox.geojson.*;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.math.RoundingMode;

import com.mapbox.turf.*;


import java.util.*;

public class App 
{
	
	public static final Point northWest = Point.fromLngLat(-3.192473, 55.946233);
	public static final Point northEast = Point.fromLngLat(-3.184319, 55.946233);
	public static final Point southEast = Point.fromLngLat(-3.184319, 55.942617);
	public static final Point southWest = Point.fromLngLat(-3.192473, 55.942617);
	
	public static double round(double value, int places) {
	    if (places < 0) throw new IllegalArgumentException();

	    BigDecimal bd = BigDecimal.valueOf(value);
	    bd = bd.setScale(places, RoundingMode.HALF_UP);
	    return bd.doubleValue();
	}
	
    public static void main( String[] args )
    {
    	List<Point> coo = new LinkedList<Point>();
    	coo.add(northWest);
    	coo.add(northEast);
    	coo.add(southEast);
    	coo.add(southWest);
    	coo.add(northWest);
    	
    	Polygon workingArea = Polygon.fromLngLats(List.of(coo));
    	
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
        
       boolean reachedBack = false;
       while (reachedBack == false) {
    	   var currentPoint = startingPoint;
    	   List<Detail> listOfPoints = new LinkedList<Detail>();
    	   
    	   for (int i = 0; i <= 90; i += 10) {
    		   var yolo = Point.fromLngLat(currentPoint.longitude() + move*Math.sin(i), currentPoint.latitude() + move*Math.cos(i));
    		   double distancee = Math.sqrt(Math.pow((startingPoint.longitude() - yolo.longitude()),2) + Math.pow(startingPoint.latitude() - yolo.latitude(),2)); 
    		   listOfPoints.add(new Detail(distancee, yolo));   
    	   }
    	   for (int i = 100; i <= 180; i += 10) {
    		   var yolo = Point.fromLngLat(currentPoint.longitude() + move*Math.sin(i), currentPoint.latitude() - move*Math.cos(i));
    		   double distancee = Math.sqrt(Math.pow((startingPoint.longitude() - yolo.longitude()),2) + Math.pow(startingPoint.latitude() - yolo.latitude(),2)); 
    		   listOfPoints.add(new Detail(distancee, yolo));   
    	   }
    	   for (int i = 190; i <= 270; i += 10) {
    		   var yolo = Point.fromLngLat(currentPoint.longitude() - move*Math.sin(i), currentPoint.latitude() - move*Math.cos(i));
    		   double distancee = Math.sqrt(Math.pow((startingPoint.longitude() - yolo.longitude()),2) + Math.pow(startingPoint.latitude() - yolo.latitude(),2)); 
    		   listOfPoints.add(new Detail(distancee, yolo));   
    	   }
    	   for (int i = 280; i <= 350; i += 10) {
    		   var yolo = Point.fromLngLat(currentPoint.longitude() - move*Math.sin(i), currentPoint.latitude() + move*Math.cos(i));
    		   double distancee = Math.sqrt(Math.pow((startingPoint.longitude() - yolo.longitude()),2) + Math.pow(startingPoint.latitude() - yolo.latitude(),2)); 
    		   listOfPoints.add(new Detail(distancee, yolo));   
    	   }
    	   
    	   
    	   
       }
        
       List<Vertex> vertices = new LinkedList<Vertex>();

       int k = 0;
        for (double i = 55.94267; i < 55.94623; i += 0.00001 ) {
        	for (double j = 3.18431; j < 3.19247; j += 0.00001) {
        		
        		var temp1 = new Vertex(-j, i);
        		vertices.add(temp1);        		
        	} 
        	
        	System.out.println(" Row " + k);
        	k ++;
        }
        
        List<Vertex> toRemove = new ArrayList<Vertex>();
        
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
        
        
        
        
        
        
        /* List<Edge> edges = new ArrayList<Edge>();
        int iter = 0;
        for (Vertex vertex : vertices) {
        	for (int i = 0; i <=  90; i += 10) {
        		var temp4 = new Vertex(vertex.longitude + round(move*Math.sin(i),5), vertex.latitude + round(move*Math.cos(i),5));
        		if (TurfJoins.inside(Point.fromLngLat(temp4.longitude, temp4.latitude), workingArea)) {
        			for (Feature feature : noFlyAreas.features()) {
        				if(TurfJoins.inside(Point.fromLngLat(temp4.longitude, temp4.latitude), (Polygon)feature.geometry()) == false){
        					var edge = new Edge(String.valueOf(iter), vertex, temp4, 1);
        					edges.add(edge);
        					iter ++;
        				}
        			}
        		}        		
        	}
        	for (int i = 100; i <=  180; i += 10) {
        		var temp4 = new Vertex(vertex.longitude + round(move*Math.sin(i),5), vertex.latitude - round(move*Math.cos(i),5));
        		if (TurfJoins.inside(Point.fromLngLat(temp4.longitude, temp4.latitude), workingArea)) {
        			for (Feature feature : noFlyAreas.features()) {
        				if(TurfJoins.inside(Point.fromLngLat(temp4.longitude, temp4.latitude), (Polygon)feature.geometry()) == false){
        					var edge = new Edge(String.valueOf(iter), vertex, temp4, 1);
        					edges.add(edge);
        					iter ++;
        				}
        			}
        		}        		
        	}
        	for (int i = 190; i <=  270; i += 10) {
        		var temp4 = new Vertex(vertex.longitude - round(move*Math.sin(i),5), vertex.latitude - round(move*Math.cos(i),5));
        		if (TurfJoins.inside(Point.fromLngLat(temp4.longitude, temp4.latitude), workingArea)) {
        			for (Feature feature : noFlyAreas.features()) {
        				if(TurfJoins.inside(Point.fromLngLat(temp4.longitude, temp4.latitude), (Polygon)feature.geometry()) == false){
        					var edge = new Edge(String.valueOf(iter), vertex, temp4, 1);
        					edges.add(edge);
        					iter ++;
        				}
        			}
        		}        		
        	}
        	for (int i = 280; i <=  350; i += 10) {
        		var temp4 = new Vertex(vertex.longitude - round(move*Math.sin(i),5), vertex.latitude + round(move*Math.cos(i),5));
        		if (TurfJoins.inside(Point.fromLngLat(temp4.longitude, temp4.latitude), workingArea)) {
        			for (Feature feature : noFlyAreas.features()) {
        				if(TurfJoins.inside(Point.fromLngLat(temp4.longitude, temp4.latitude), (Polygon)feature.geometry()) == false){
        					var edge = new Edge(String.valueOf(iter), vertex, temp4, 1);
        					edges.add(edge);
        					iter ++;
        				}
        			}
        		}        		
        	}
        	System.out.print("Done: " + vertex.longitude + "," + vertex.latitude);
        } */
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        // USE TURF TO CHECK IF IT IS INSIDE THE POLYGON
        for (Feature feature : noFlyAreas.features()) {
        	System.out.println("It is inside (1): " + feature.getProperty("name") + TurfJoins.inside(point, (Polygon)feature.geometry()));
            System.out.println("It is inside (2): " + feature.getProperty("name") + TurfJoins.inside(point2, (Polygon)feature.geometry()));
        }
        
    }
}
