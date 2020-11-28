package uk.ac.ed.inf.aqmaps;

import com.mapbox.geojson.*;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.File;
import java.io.FileWriter;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.math.RoundingMode;

import com.mapbox.turf.*;


import java.util.*;

public class App 
{
	public static void writeFileToGEOJSON (String fileName, FeatureCollection fc) throws Exception {
		FileWriter file = new FileWriter(fileName);    	
    	file.write(fc.toJson());
    	System.out.println("Successfully Created File");
    	file.close();
	}
	
	public static boolean isEqual(Coordinates a, Coordinates b) {
		if (a.lat == b.lat && a.lng == b.lng) {
			return true;
		}
		return false;
	}
	
	
	// Check if the line from startingPoint towards a certain direction is inside any of the polygons of the Feature Collection fc
	public static boolean isLineInFC(Point startingPoint, FeatureCollection fc, int direction) {
		for (int j = 1; j < 100; j++) {
			double move1 = 0.000003*j;	
			for (Feature feature: fc.features()) {
					if (direction >= 0 && direction <= 90) {
						var yolo = Point.fromLngLat(startingPoint.longitude() + move1*Math.cos(direction), startingPoint.latitude() + move1*Math.sin(direction));
						if (TurfJoins.inside(yolo, (Polygon)feature.geometry())){
							return true;					
						}
					}
					if (direction >= 100 && direction <= 180) {
						var yolo = Point.fromLngLat(startingPoint.longitude() - move1*Math.cos(direction), startingPoint.latitude() + move1*Math.sin(direction));
						if (TurfJoins.inside(yolo, (Polygon)feature.geometry())){
							return true;					
						}
					}
					if (direction >= 190 && direction <= 270) {
						var yolo = Point.fromLngLat(startingPoint.longitude() - move1*Math.cos(direction), startingPoint.latitude() - move1*Math.sin(direction));
						if (TurfJoins.inside(yolo, (Polygon)feature.geometry())){
							return true;					
						}
					}
					if (direction >= 280 && direction <= 350) {
						var yolo = Point.fromLngLat(startingPoint.longitude() + move1*Math.cos(direction), startingPoint.latitude() - move1*Math.sin(direction));
						if (TurfJoins.inside(yolo, (Polygon)feature.geometry())){
							return true;					
						}
					}
				}
			}	
		return false;
	}
	
	public static Feature addAttributes(Feature feature, double reading) {
		if ((reading >= 0 ) && (reading < 32)) {
			feature.addStringProperty("rgb-string", "#00ff00");
			feature.addStringProperty("marker-color", "#00ff00");
			feature.addStringProperty("marker-symbol", "lighthouse");
		} else if ((reading >= 32 ) && (reading < 64)) {
			feature.addStringProperty("rgb-string", "#40ff00");
			feature.addStringProperty("marker-color", "#40ff00");
			feature.addStringProperty("marker-symbol", "lighthouse");
		} else if ((reading >= 64 ) && (reading < 96)) {
			feature.addStringProperty("rgb-string", "#80ff00");
			feature.addStringProperty("marker-color", "#80ff00");
			feature.addStringProperty("marker-symbol", "lighthouse");
		} else if ((reading >= 96 ) && (reading < 128)) {
			feature.addStringProperty("rgb-string", "#c0ff00");
			feature.addStringProperty("marker-color", "#c0ff00");
			feature.addStringProperty("marker-symbol", "lighthouse");
		} else if ((reading >= 128 ) && (reading < 160)) {
			feature.addStringProperty("rgb-string", "#ffc000");
			feature.addStringProperty("marker-color", "#ffc000");
			feature.addStringProperty("marker-symbol", "danger");
		} else if ((reading >= 160 ) && (reading < 192)) {
			feature.addStringProperty("rgb-string", "#ff8000");
			feature.addStringProperty("marker-color", "#ff8000");
			feature.addStringProperty("marker-symbol", "danger");
		} else if ((reading >= 192 ) && (reading < 224)) {
			feature.addStringProperty("rgb-string", "#ff4000");
			feature.addStringProperty("marker-color", "#ff4000");
			feature.addStringProperty("marker-symbol", "danger");
		} else if ((reading >= 224 ) && (reading < 256)) {
			feature.addStringProperty("rgb-string", "#ff0000");
			feature.addStringProperty("marker-color", "#ff0000");
			feature.addStringProperty("marker-symbol", "danger");
		} else {
			feature.addStringProperty("rgb-string", "#aaaaaa");
			feature.addStringProperty("marker-color", "#aaaaaa");
			feature.addStringProperty("marker-symbol", "cross");
		}
		feature.addStringProperty("marker-size", "medium");    					
		return feature;
	}
	
	private static final Point northWest = Point.fromLngLat(-3.192473, 55.946233);
	private static final Point northEast = Point.fromLngLat(-3.184319, 55.946233);
	private static final Point southEast = Point.fromLngLat(-3.184319, 55.942617);
	private static final Point southWest = Point.fromLngLat(-3.192473, 55.942617);
	private static final double move = 0.0003;
	
	public static double round(double value, int places) {
	    if (places < 0) throw new IllegalArgumentException();

	    BigDecimal bd = BigDecimal.valueOf(value);
	    bd = bd.setScale(places, RoundingMode.HALF_UP);
	    return bd.doubleValue();
	}
	
	public static double distance(Point a, Point b) {
		return Math.sqrt(Math.pow(a.longitude() - b.longitude(),2) + Math.pow(a.latitude() - b.latitude(), 2));
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
    	
    	final Point startingPoint = Point.fromLngLat(Double.parseDouble(args[4]), Double.parseDouble(args[3]));
    			
    	// Read and store the sensors for the specific day in an array listOfEntries	
    	String urlStringForPoints = "http://localhost:" + args[6] + "/maps/" + args[2] + "/" + args[1] + "/" + args[0] + "/air-quality-data.json";    	
        Type listType = new TypeToken<ArrayList<SensorData>>() {}.getType();
        ArrayList<SensorData> listOfEntries = new Gson().fromJson(NetworkRead.readNetworkToString(urlStringForPoints), listType);
        
        // Read the no fly areas and store in collection noFlyAreas
        String urlStringNoFly = "http://localhost:" + args[6] + "/buildings/no-fly-zones.geojson";
        var noFlyAreas = FeatureCollection.fromJson(NetworkRead.readNetworkToString(urlStringNoFly)); 
        
        for (SensorData entry : listOfEntries) {
        	String str[] = entry.location.split("\\.");
        	var Words = new Gson().fromJson(NetworkRead.readNetworkToString("http://localhost:" + args[6] + "/words/" + str[0] + "/" + str[1] + "/" + str[2] + "/details.json"), Words.class);
        	entry.lat = Words.coordinates.lat;
        	entry.lng = Words.coordinates.lng;        	
        }
        
        // Define locally used variables
        var last4Moves = new ArrayList<Move>(); 
        var visitedPoints = new ArrayList<SensorData>();
        var moves = new ArrayList<Move>();
        int movesCounter = 1;
        var currentPoint = startingPoint;
        // Array of points
        var points = new ArrayList<Point>();
        // Flag to check if the drone reached its initial position
        boolean reachedBack = false;
        
       
       var features = new ArrayList<Feature>();
       features.add(Feature.fromGeometry(startingPoint));
       features.get(0).addBooleanProperty("Starting Point", true);
       
       for (SensorData sensor : listOfEntries) {
    	   features.add(Feature.fromGeometry((Point.fromLngLat(Double.parseDouble(sensor.lng), Double.parseDouble(sensor.lat)))));
       }
       
       var features1 = new ArrayList<Feature>();
       
       while (reachedBack == false) {
    	   
    	   List<Detail> listOfPoints = new LinkedList<Detail>();
    	   for (Feature feature : noFlyAreas.features()) {
    		   System.out.println("Point is not in " +  feature.properties().toString());
    		   System.out.println(TurfJoins.inside(Point.fromLngLat(-3.185459, 55.945020),(Polygon)feature.geometry()));
    		   
    	   }
    	   for (int i = 0; i <= 90; i += 10) {
    		   var yolo = Point.fromLngLat(currentPoint.longitude() + move*Math.cos(i), currentPoint.latitude() + move*Math.sin(i));
    		   if (TurfJoins.inside(yolo, workingArea) == false ) {
    			   continue;
    		   }
    		   if (isLineInFC(currentPoint, noFlyAreas, i)) {
    			   continue;
    		   }  		       		       			       		      		   
    		   listOfPoints.add(new Detail(i, yolo, currentPoint));   
    	   }
    	   for (int i = 100; i <= 180; i += 10) {
    		   var yolo = Point.fromLngLat(currentPoint.longitude() - move*Math.cos(i), currentPoint.latitude() + move*Math.sin(i));
    		   if (TurfJoins.inside(yolo, workingArea) == false ) {
    			   continue;
    		   }
    		   if (isLineInFC(currentPoint, noFlyAreas, i)) {
    			   continue;
    		   }   		      		       			       		     	
    		   listOfPoints.add(new Detail(i, yolo, currentPoint));   
    	   }
    	   for (int i = 190; i <= 270; i += 10) {
    		   var yolo = Point.fromLngLat(currentPoint.longitude() - move*Math.cos(i), currentPoint.latitude() - move*Math.sin(i));
    		   if (TurfJoins.inside(yolo, workingArea) == false ) {
    			   continue;
    		   }
    		   if (isLineInFC(currentPoint, noFlyAreas, i)) {
    			   continue;
    		   }    		       		       			       		     	
    		   listOfPoints.add(new Detail(i, yolo, currentPoint));    
    	   }
    	   for (int i = 280; i <= 350; i += 10) {
    		   var yolo = Point.fromLngLat(currentPoint.longitude() + move*Math.cos(i), currentPoint.latitude() - move*Math.sin(i));
    		   if (TurfJoins.inside(yolo, workingArea) == false ) {
    			   continue;
    		   }
    		   if (isLineInFC(currentPoint, noFlyAreas, i)) {
    			   continue;
    		   }      		       			       		     	
    		   listOfPoints.add(new Detail(i, yolo, currentPoint));    
    	   }
    	   /*
    	   var toRemove = new ArrayList<Detail>();   
    	   for (Feature feature1 : noFlyAreas.features()) {
    		   for (Detail detail1 : listOfPoints) {
    			   if (isLineInPoly(detail1, feature1)) {
    				   toRemove.add(detail1);
    			   }
    		   }
    	   }
    	   
    	   // Check if any points are in the forbidden zone and delete them if they are
    	    for (Detail detail : listOfPoints) {
    		   for (Feature feature : noFlyAreas.features()) {
    			   if (isLineInPoly(detail, feature)) {
    				   toRemove.add(detail);
    			   }
    		   }
    	   }  	   
    	   listOfPoints.removeAll(toRemove);
    	   
    	   for (Feature feature : noFlyAreas.features()) {
    		   for (Detail detail : listOfPoints) {
    			   if (TurfJoins.inside(detail.estimatedPoint, (Polygon)feature.geometry())) {
    				   toRemove.add(detail);
    			   }
    		   }
    	   }
    	   
    	  
    	   
    	   
    	   listOfPoints.removeAll(toRemove);
    	   */
    	   System.out.println("Done with Points for " + movesCounter);
    	   
    	   
    	   List<List<Difference>> listOfLists = new ArrayList<List<Difference>>();
    	   for (SensorData aqe : listOfEntries) {
    		   List<Difference> temporaryList = new ArrayList<Difference>();
    		   for (Detail detail1 : listOfPoints) {
    			   temporaryList.add(new Difference(detail1, aqe, distance(Point.fromLngLat(Double.parseDouble(aqe.lng), Double.parseDouble(aqe.lat)), detail1.estimatedPoint)));
    		   }
    		   
    		   listOfLists.add(temporaryList);
    	   }
    	   
    	   var yes = new ArrayList<Difference>();
    	   
    	   for (List<Difference> list : listOfLists) {
    		   Collections.sort(list, new SortByDistance());
    		   yes.add(list.get(0));
    	   }
    	   Collections.sort(yes, new SortByDistance());
    	   
    	   var newDifference = yes.get(0);
    	   
    	   
    	   moves.add(new Move(movesCounter, new Coordinates(currentPoint.longitude(), currentPoint.latitude()), new Coordinates(newDifference.source.estimatedPoint.longitude(), newDifference.source.estimatedPoint.latitude())));
    	   moves.get(movesCounter - 1).addDirection(newDifference.source.direction);
    	   System.out.println(moves.get(movesCounter - 1).toString());
    	   
    	   moves.get(movesCounter - 1).addDirection(newDifference.source.direction);
    	   last4Moves.add(((movesCounter - 1) % 4), new Move(movesCounter, new Coordinates(currentPoint.longitude(), currentPoint.latitude()), new Coordinates(newDifference.source.estimatedPoint.longitude(), newDifference.source.estimatedPoint.latitude())));
    	   
    	   
    	   points.add(Point.fromLngLat(moves.get(movesCounter - 1).beforeMove.lng, moves.get(movesCounter - 1).beforeMove.lat));
    	   points.add(Point.fromLngLat(moves.get(movesCounter - 1).afterMove.lng, moves.get(movesCounter - 1).afterMove.lat));
    	   
    	   
    	   currentPoint = newDifference.source.estimatedPoint;
    	   
    	   
    	   if (last4Moves.size() == 4 ) {
    	   if (isEqual(last4Moves.get(0).afterMove, last4Moves.get(2).afterMove)) {
    		   if (isEqual(last4Moves.get(1).afterMove, last4Moves.get(3).afterMove)) {
    			   var tempPoint = Point.fromLngLat(currentPoint.longitude() + move*Math.sin(50), currentPoint.latitude() - move * Math.cos(50));
    			   moves.add(new Move(movesCounter + 1, new Coordinates(currentPoint.longitude(), currentPoint.latitude()), new Coordinates(tempPoint.longitude(), tempPoint.latitude())));
    			   currentPoint = tempPoint;
    		   }
    	   }
    	   }
    	   
    	   if (distance(currentPoint,Point.fromLngLat(Double.parseDouble(newDifference.dest.lng), Double.parseDouble(newDifference.dest.lat))) < 0.0002) {
    		   System.out.println(newDifference.dest.location + " " + newDifference.dest.reading);
    		   moves.get(movesCounter - 1).addLocation(newDifference.dest.location);
    		   var temporary = Feature.fromGeometry(Point.fromLngLat(Double.parseDouble(newDifference.dest.lng), Double.parseDouble(newDifference.dest.lat)));
    		   temporary.addStringProperty("location", newDifference.dest.location);
    		   
    		   if (newDifference.dest.reading != null) {
    		   if (newDifference.dest.reading.contains("n") == false) {
    		   addAttributes(temporary, Double.parseDouble(newDifference.dest.reading));
    		   
    		   		}
    		   	}
    		   features1.add(temporary);
    		   listOfEntries.remove(newDifference.dest);
        	   visitedPoints.add(newDifference.dest);
    	   }
    	   
    	   if (distance(currentPoint, startingPoint) < 0.0003 && (listOfEntries.isEmpty() == true)) {
    		   break;
    	   }
    	   
    	   if (listOfEntries.isEmpty()) {
    		   listOfEntries.add(new SensorData(startingPoint));
    	   }

    	   movesCounter ++;
       
           if (movesCounter == 150) {
        	   System.out.println("More than 150 moves!");
        	   features1.add(Feature.fromGeometry(LineString.fromLngLats(points)));
               var fcaa = FeatureCollection.fromFeatures(features1);
               try {
            	   writeFileToGEOJSON("resultWithError.geojson", fcaa); 
               } catch (Exception e) {
        		e.printStackTrace();
        	}
        	   
        	   System.exit(69);
        	   
           }
           
              	   
       }
       
       
       
       features1.add(Feature.fromGeometry(LineString.fromLngLats(points)));
       var fcaa = FeatureCollection.fromFeatures(features1);
       try {
    	   String fileName = String.format("readings-%s-%s-%s.geojson", args[0], args[1], args[2]);
    	   writeFileToGEOJSON(fileName, fcaa); 
       } catch (Exception e) {
		e.printStackTrace();
	}
       
       
    
      try {
    	  String fileName = String.format("flightpath-%s-%s-%s.txt", args[0], args[1], args[2]);
    	  IOOperations.writeMovesToFile(moves, fileName);
      } catch (Exception e) {
		e.printStackTrace();
	}
       
   
    }
}
