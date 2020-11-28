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
	
	public static boolean isLineInPoly(Detail detail, Feature feature) {
		for (int to = 1; to < 100; to++) {
			double move1 = 0.000003*to;
			
			if (detail.direction >= 0 && detail.direction <= 90) {
				var yolo = Point.fromLngLat(detail.startingPoint.longitude() + move1*Math.sin(detail.direction), detail.startingPoint.latitude() + move1*Math.cos(detail.direction));
				if (TurfJoins.inside(yolo, (Polygon)feature.geometry())){
					return true;					
				}
			}
			if (detail.direction >= 100 && detail.direction <= 180) {
				var yolo = Point.fromLngLat(detail.startingPoint.longitude() - move1*Math.sin(detail.direction), detail.startingPoint.latitude() + move1*Math.cos(detail.direction));
				if (TurfJoins.inside(yolo, (Polygon)feature.geometry())){
					return true;				
				}
			}
			if (detail.direction >= 190 && detail.direction <= 270) {
				var yolo = Point.fromLngLat(detail.startingPoint.longitude() - move1*Math.sin(detail.direction), detail.startingPoint.latitude() - move1*Math.cos(detail.direction));
				if (TurfJoins.inside(yolo, (Polygon)feature.geometry())){
					return true;					
				}
			}
			if (detail.direction >= 280 && detail.direction <= 350) {
				var yolo = Point.fromLngLat(detail.startingPoint.longitude() + move1*Math.sin(detail.direction), detail.startingPoint.latitude() - move1*Math.cos(detail.direction));
				if (TurfJoins.inside(yolo, (Polygon)feature.geometry())){
					return true;					
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
	
	public static final Point northWest = Point.fromLngLat(-3.192473, 55.946233);
	public static final Point northEast = Point.fromLngLat(-3.184319, 55.946233);
	public static final Point southEast = Point.fromLngLat(-3.184319, 55.942617);
	public static final Point southWest = Point.fromLngLat(-3.192473, 55.942617);
	public static final double move = 0.0003;
	
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
    	
    	final Point startingPoint = Point.fromLngLat(Double.parseDouble(args[5]), Double.parseDouble(args[4]));
    			
    			
    	String urlStringForPoints = "http://localhost:" + args[3] + "/maps/" + args[2] + "/" + args[0] + "/" + args[1] + "/air-quality-data.json";    	
      
        Type listType = new TypeToken<ArrayList<AirQualityEntry>>() {}.getType();
        ArrayList<AirQualityEntry> listOfEntries = new Gson().fromJson(NetworkRead.readNetworkToString(urlStringForPoints), listType);
        
        
        
        
        String urlStringNoFly = "http://localhost:" + args[3] + "/buildings/no-fly-zones.geojson";
        var noFlyAreas = FeatureCollection.fromJson(NetworkRead.readNetworkToString(urlStringNoFly)); 
       
        
        var testing = Point.fromLngLat(-3.186300000, 55.944500000);
    	
    	for (Feature feature : noFlyAreas.features()) {
    		System.out.println("Feature " + feature.properties());
    		if (TurfJoins.inside(testing, (Polygon)feature.geometry())) {
    			System.out.println("It is inside Poly");
    			System.out.println();
    			System.out.println();
    		}
    		
    	}
        
        
        
        for (AirQualityEntry entry : listOfEntries) {
        	String str[] = entry.location.split("\\.");
        	var Words = new Gson().fromJson(NetworkRead.readNetworkToString("http://localhost:" + args[3] + "/words/" + str[0] + "/" + str[1] + "/" + str[2] + "/details.json"), Words.class);
        	entry.lat = Words.coordinates.lat;
        	entry.lng = Words.coordinates.lng;        	
        }
        
       var last4Moves = new ArrayList<Move>();
        
       boolean reachedBack = false;
       var visitedPoints = new ArrayList<AirQualityEntry>();
       var moves = new ArrayList<Move>();
       int jotaro = 1;
       var currentPoint = startingPoint;
       var tempoo = new ArrayList<Point>();
       
       var features = new ArrayList<Feature>();
       features.add(Feature.fromGeometry(startingPoint));
       features.get(0).addBooleanProperty("Starting Point", true);
       
       for (AirQualityEntry aqee : listOfEntries) {
    	   features.add(Feature.fromGeometry((Point.fromLngLat(Double.parseDouble(aqee.lng), Double.parseDouble(aqee.lat)))));
       }
       
       var features1 = new ArrayList<Feature>();
       
       while (reachedBack == false) {
    	   
    	   List<Detail> listOfPoints = new LinkedList<Detail>();
    	   
    	   for (int i = 0; i <= 90; i += 10) {
    		   var yolo = Point.fromLngLat(currentPoint.longitude() + move*Math.sin(i), currentPoint.latitude() + move*Math.cos(i));
    		   if (TurfJoins.inside(yolo, workingArea) == false ) {
    			   continue;
    		   }    		   
    		       		       			       		      		   
    		   listOfPoints.add(new Detail(i, yolo, currentPoint));   
    	   }
    	   for (int i = 100; i <= 180; i += 10) {
    		   var yolo = Point.fromLngLat(currentPoint.longitude() - move*Math.sin(i), currentPoint.latitude() + move*Math.cos(i));
    		   if (TurfJoins.inside(yolo, workingArea) == false ) {
    			   continue;
    		   }    		   
    		      		       			       		     	
    		   listOfPoints.add(new Detail(i, yolo, currentPoint));   
    	   }
    	   for (int i = 190; i <= 270; i += 10) {
    		   var yolo = Point.fromLngLat(currentPoint.longitude() - move*Math.sin(i), currentPoint.latitude() - move*Math.cos(i));
    		   if (TurfJoins.inside(yolo, workingArea) == false ) {
    			   continue;
    		   }    		   
    		       		       			       		     	
    		   listOfPoints.add(new Detail(i, yolo, currentPoint));    
    	   }
    	   for (int i = 280; i <= 350; i += 10) {
    		   var yolo = Point.fromLngLat(currentPoint.longitude() + move*Math.sin(i), currentPoint.latitude() - move*Math.cos(i));
    		   if (TurfJoins.inside(yolo, workingArea) == false ) {
    			   continue;
    		   }    		   
    		      		       			       		     	
    		   listOfPoints.add(new Detail(i, yolo, currentPoint));    
    	   }
    	   
    	   var toRemove = new ArrayList<Detail>();
    	   
    	   
    	   for (Feature feature1 : noFlyAreas.features()) {
    		   for (Detail detail1 : listOfPoints) {
    			   if (isLineInPoly(detail1, feature1)) {
    				   toRemove.add(detail1);
    			   }
    		   }
    	   }
    	   
    	   
    	   
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
    	   
    	   System.out.println("Done with Points for " + jotaro);
    	   
    	   
    	   List<List<Difference>> listOfLists = new ArrayList<List<Difference>>();
    	   for (AirQualityEntry aqe : listOfEntries) {
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
    	   
    	   
    	   moves.add(new Move(jotaro, new Coordinates(currentPoint.longitude(), currentPoint.latitude()), new Coordinates(newDifference.source.estimatedPoint.longitude(), newDifference.source.estimatedPoint.latitude())));
    	   moves.get(jotaro - 1).addDirection(newDifference.source.direction);
    	   System.out.println(moves.get(jotaro - 1).toString());
    	   
    	   moves.get(jotaro - 1).addDirection(newDifference.source.direction);
    	   last4Moves.add(((jotaro - 1) % 4), new Move(jotaro, new Coordinates(currentPoint.longitude(), currentPoint.latitude()), new Coordinates(newDifference.source.estimatedPoint.longitude(), newDifference.source.estimatedPoint.latitude())));
    	   
    	   
    	   tempoo.add(Point.fromLngLat(moves.get(jotaro - 1).beforeMove.lng, moves.get(jotaro - 1).beforeMove.lat));
    	   tempoo.add(Point.fromLngLat(moves.get(jotaro - 1).afterMove.lng, moves.get(jotaro - 1).afterMove.lat));
    	   
    	   
    	   currentPoint = newDifference.source.estimatedPoint;
    	   
    	   
    	   if (last4Moves.size() == 4 ) {
    	   if (isEqual(last4Moves.get(0).afterMove, last4Moves.get(2).afterMove)) {
    		   if (isEqual(last4Moves.get(1).afterMove, last4Moves.get(3).afterMove)) {
    			   var tempPoint = Point.fromLngLat(currentPoint.longitude() + move*Math.sin(50), currentPoint.latitude() - move * Math.cos(50));
    			   moves.add(new Move(jotaro + 1, new Coordinates(currentPoint.longitude(), currentPoint.latitude()), new Coordinates(tempPoint.longitude(), tempPoint.latitude())));
    			   currentPoint = tempPoint;
    		   }
    	   }
    	   }
    	   
    	   if (distance(currentPoint,Point.fromLngLat(Double.parseDouble(newDifference.dest.lng), Double.parseDouble(newDifference.dest.lat))) < 0.0002) {
    		   System.out.println(newDifference.dest.location + " " + newDifference.dest.reading);
    		   moves.get(jotaro - 1).addLocation(newDifference.dest.location);
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
    		   listOfEntries.add(new AirQualityEntry(startingPoint));
    	   }

    	   jotaro ++;
       
           
        	
           if (jotaro == 300) {
        	   System.out.println("More than 300 moves!");
        	   features1.add(Feature.fromGeometry(LineString.fromLngLats(tempoo)));
               var fcaa = FeatureCollection.fromFeatures(features1);
               try {
            	   writeFileToGEOJSON("resultWithError.geojson", fcaa); 
               } catch (Exception e) {
        		e.printStackTrace();
        	}
        	   
        	   System.exit(69);
        	   
           }
           
              	   
       }
       
       
       
       features1.add(Feature.fromGeometry(LineString.fromLngLats(tempoo)));
       var fcaa = FeatureCollection.fromFeatures(features1);
       try {
    	   writeFileToGEOJSON("result.geojson", fcaa); 
       } catch (Exception e) {
		e.printStackTrace();
	}
       
       
    
      try {
    	  IOOperations.writeFile(moves);
      } catch (Exception e) {
		e.printStackTrace();
	}
       
   
    }
}
