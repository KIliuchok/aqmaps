package uk.ac.ed.inf.aqmaps;

import com.mapbox.geojson.*;
import com.mapbox.geojson.LineString;
import com.mapbox.geojson.Point;
import com.mapbox.geojson.Polygon;

import org.wololo.jts2geojson.GeoJSONReader;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;

import java.util.*;

public class App {

	// Define Points for the Confinement Area
	private static final Point NORTH_WEST = Point.fromLngLat(-3.192473, 55.946233);
	private static final Point NORTH_EAST = Point.fromLngLat(-3.184319, 55.946233);
	private static final Point SOUTH_EAST = Point.fromLngLat(-3.184319, 55.942617);
	private static final Point SOUTH_WEST = Point.fromLngLat(-3.192473, 55.942617);

	// Define the move length of the drone
	private static final double MOVE_LENGTH = 0.0003;

	// Initialize control variables
	private static FeatureCollection errorRegion;
	private static Polygon confinementArea;
	private static FeatureCollection noFlyAreas;
	// Control bias is essentially a direction where to move the starting point as a first move
	private static int startingBias = 0;
	// Flag which signifies if a valid path has been found
	private static boolean isFinished = false;

	/*
	 * Using JTS Library to check if provided geometry object intersects any of the
	 * geometry objects in provided FeatureCollection
	 */
	private static boolean isTouches(LineString line, FeatureCollection fc) {
		GeoJSONReader reader = new GeoJSONReader();
		org.locationtech.jts.geom.Geometry jtsLine = reader.read(line.toJson());
		for (Feature feature : fc.features()) {
			org.locationtech.jts.geom.Geometry jtwPoly = reader.read(feature.geometry().toJson());
			if (jtwPoly.intersects(jtsLine)) {
				return true;
			}
		}
		return false;
	}

	private static boolean isTouches(Point point, FeatureCollection fc) {
		GeoJSONReader reader = new GeoJSONReader();
		org.locationtech.jts.geom.Geometry jtsPoint = reader.read(point.toJson());
		for (Feature feature : fc.features()) {
			org.locationtech.jts.geom.Geometry jtwPoly = reader.read(feature.geometry().toJson());
			if (jtwPoly.intersects(jtsPoint)) {
				return true;
			}
		}
		return false;
	}

	private static void addAttributes(Feature feature, float reading, String location) {
		feature.addStringProperty("marker-size", "medium");
		feature.addStringProperty("location", location);
		if ((reading >= 0) && (reading < 32)) {
			addColourAttribute(feature, "#00ff00");
			feature.addStringProperty("marker-symbol", "lighthouse");
		} else if ((reading >= 32) && (reading < 64)) {
			addColourAttribute(feature, "#40ff00");
			feature.addStringProperty("marker-symbol", "lighthouse");
		} else if ((reading >= 64) && (reading < 96)) {
			addColourAttribute(feature, "#80ff00");
			feature.addStringProperty("marker-symbol", "lighthouse");
		} else if ((reading >= 96) && (reading < 128)) {
			addColourAttribute(feature, "#c0ff00");
			feature.addStringProperty("marker-symbol", "lighthouse");
		} else if ((reading >= 128) && (reading < 160)) {
			addColourAttribute(feature, "#ffc000");
			feature.addStringProperty("marker-symbol", "danger");
		} else if ((reading >= 160) && (reading < 192)) {
			addColourAttribute(feature, "#ff8000");
			feature.addStringProperty("marker-symbol", "danger");
		} else if ((reading >= 192) && (reading < 224)) {
			addColourAttribute(feature, "#ff4000");
			feature.addStringProperty("marker-symbol", "danger");
		} else if ((reading >= 224) && (reading < 256)) {
			addColourAttribute(feature, "#ff0000");
			feature.addStringProperty("marker-symbol", "danger");
		} else {
			addColourAttribute(feature, "#aaaaaa");
		}
		
	}

	private static void addColourAttribute(Feature feature, String colour) {
		feature.addStringProperty("rgb-string", colour);
		feature.addStringProperty("marker-color", colour);
	}

	// Convert sensor coordinates to a Point object
	private static Point getPointFromSensor(Sensor sensor) {
		return Point.fromLngLat(sensor.getCoordinates().getLng(), sensor.getCoordinates().getLat());
	}

	// Distance between two points
	private static double distance(Point a, Point b) {
		return Math.sqrt(Math.pow(a.longitude() - b.longitude(), 2) + Math.pow(a.latitude() - b.latitude(), 2));
	}

	// Estimate a point given a direction and origin
	private static Point estimatePoint(Point currentPoint, int direction) {
		var point = Point.fromLngLat(currentPoint.longitude() + MOVE_LENGTH * Math.cos(Math.toRadians(direction)),
				currentPoint.latitude() + MOVE_LENGTH * Math.sin(Math.toRadians(direction)));
		return point;
	}

	// Creates a valid directional vector
	private static Vector createValidVector(Point origin, int direction) {
		var estimatedPoint = estimatePoint(origin, direction);
		// Check if the estimated point is in the confinement area
		if (isTouches(estimatedPoint, FeatureCollection.fromFeature(Feature.fromGeometry(confinementArea))) == false) {
			return null;
		}
		// Create a LineString and check if it touches any of the forbidden areas
		var pointsForLineString = new ArrayList<Point>();
		pointsForLineString.add(origin);
		pointsForLineString.add(estimatedPoint);
		if (isTouches(LineString.fromLngLats(pointsForLineString), noFlyAreas)) {
			return null;
		}
		if (isTouches(estimatedPoint, errorRegion)) {
			return null;
		}
		var vector = new Vector(direction, origin, estimatedPoint);
		return vector;
	}

	// Check if the sensor is close to a point
	private static boolean isSensorInRange(Point currentPosition, Sensor sensor) {
		if (distance(currentPosition, getPointFromSensor(sensor)) < 0.0002) {
			return true;
		}
		return false;
	}

	private static Feature analyzeSensor(Sensor sensor) {
		var analyzedSensor = Feature.fromGeometry(getPointFromSensor(sensor));
		if (sensor.getReading() != null) {
			if (sensor.getBattery() < 10.0) {
				addColourAttribute(analyzedSensor, "#000000");
				analyzedSensor.addStringProperty("marker-symbol", "cross");
			} else {
				addAttributes(analyzedSensor, Float.parseFloat(sensor.getReading()), sensor.getLocation());
			}
		}
		return analyzedSensor;
	}

	private static Feature createPath(ArrayList<Move> moves) {
		var tempListOfVisitedPoints = new ArrayList<Point>();
		var startPoint = Point.fromLngLat(moves.get(0).getBeforeMove().getLng(), moves.get(0).getBeforeMove().getLat());
		tempListOfVisitedPoints.add(startPoint);
		for (Move move : moves) {
			var point = Point.fromLngLat(move.getBeforeMove().getLng(), move.getBeforeMove().getLat());
			tempListOfVisitedPoints.add(point);
		}
		var lineString = Feature.fromGeometry(LineString.fromLngLats(tempListOfVisitedPoints));
		return lineString;
	}

	@SuppressWarnings("unchecked")
	private static MovesAndPoints algorithm(Point originalStartingPoint, ArrayList<Sensor> originallistOfEntries) {
		// 
		int movesCounter = 1;
		Point currentPoint;
		boolean reachedBack = false;
		var moves = new ArrayList<Move>();
		var features = new ArrayList<Feature>();
		var listOfEntries = new ArrayList<Sensor>();
		
		// Clone the original list of entries to prevent other rising issues if the
		// first pass of the algorithm didn't manage to give result
		listOfEntries = (ArrayList<Sensor>) originallistOfEntries.clone();
		// Signifies that the initial list with sensors to visit is empty
		boolean emptyFlag = false;

		if (startingBias > 0) {
			currentPoint = Point.fromLngLat(
					originalStartingPoint.longitude() + MOVE_LENGTH * Math.cos(Math.toRadians(startingBias)),
					originalStartingPoint.latitude() + MOVE_LENGTH * Math.sin(Math.toRadians(startingBias)));
			if (isTouches(currentPoint, noFlyAreas)) {
				var result = new MovesAndPoints(moves, features);
				isFinished = false;
				return result;
			}
		} else {
			currentPoint = originalStartingPoint;
		}

		while (reachedBack == false) {
			// Create list of directional lines from current point towards each direction
			// possible
			List<Vector> listOfLines = new LinkedList<Vector>();
			for (int i = 0; i <= 90; i += 10) {
				var vector = createValidVector(currentPoint, i);
				if (vector == null) {
					continue;
				}
				listOfLines.add(vector);
			}
			for (int i = 100; i <= 180; i += 10) {
				var vector = createValidVector(currentPoint, i);
				if (vector == null) {
					continue;
				}
				listOfLines.add(vector);
			}
			for (int i = 190; i < 280; i += 10) {
				var vector = createValidVector(currentPoint, i);
				if (vector == null) {
					continue;
				}
				listOfLines.add(vector);
			}
			for (int i = 280; i <= 350; i += 10) {
				var vector = createValidVector(currentPoint, i);
				if (vector == null) {
					continue;
				}
				listOfLines.add(vector);
			}

			// For each directional line and for each unused sensor create a "line with goal
			// sensor" object
			List<List<VectorAndGoalSensor>> listOfLists = new ArrayList<List<VectorAndGoalSensor>>();
			for (Sensor sensorData : listOfEntries) {
				List<VectorAndGoalSensor> temporaryList = new ArrayList<VectorAndGoalSensor>();
				for (Vector vector : listOfLines) {
					var distance = distance(getPointFromSensor(sensorData), vector.getTargetPoint());
					var lineWithGoalSensor = new VectorAndGoalSensor(vector, sensorData, distance);
					temporaryList.add(lineWithGoalSensor);
				}
				listOfLists.add(temporaryList);
			}

			// Sort the list of lists for output to contain results with smallest distance of each list
			var sorting = new ArrayList<VectorAndGoalSensor>();
			for (List<VectorAndGoalSensor> list : listOfLists) {
				Collections.sort(list, new SortByDistance());
				sorting.add(list.get(0));
			}

			// Sort the remaining list to get the directional line object with the smallest
			// distance from current point to the closest sensor
			Collections.sort(sorting, new SortByDistance());
			var lineWithSmallestDistance = sorting.get(0);

			// Get the position values of current and estimated point as Coordinates object
			var coordinatesForCurrentPoint = new Coordinates(currentPoint.longitude(), currentPoint.latitude());
			var coordinatesForEstimatedPoint = new Coordinates(
					lineWithSmallestDistance.getSourceVector().getTargetPoint().longitude(),
					lineWithSmallestDistance.getSourceVector().getTargetPoint().latitude());

			// Create a move entry
			var move = new Move(movesCounter, coordinatesForCurrentPoint, coordinatesForEstimatedPoint);
			move.addDirection(lineWithSmallestDistance.getSourceVector().getDirection());
			moves.add(move);

			// Update the current point for the next loop
			currentPoint = lineWithSmallestDistance.getSourceVector().getTargetPoint();

			// If sensor is in range, analyze it and remove it from the initial list of entries
			if (isSensorInRange(currentPoint, lineWithSmallestDistance.getDestinationSensor())) {
				// Add location field to the move
				moves.get(movesCounter - 1).addLocation(lineWithSmallestDistance.getDestinationSensor().getLocation());
				var analyzedSensor = analyzeSensor(lineWithSmallestDistance.getDestinationSensor());
				// To prevent the initial starting position from appearing in the final readings
				// file
				if (emptyFlag == false) {
					features.add(analyzedSensor);
				}
				listOfEntries.remove(lineWithSmallestDistance.getDestinationSensor());
			}

			/*
			 * If the drone is close to the initial starting point and there is no more
			 * sensors to visit, end the main loop
			 */
			if (distance(currentPoint, originalStartingPoint) < 0.0003 && (listOfEntries.isEmpty() == true)) {
				System.out.println("Finished and reached to the starting point");
				break;
			}

			// If there is no more sensors to visit, add initial starting point as a sensor object
			if (listOfEntries.isEmpty()) {
				listOfEntries.add(new Sensor(originalStartingPoint));
				emptyFlag = true;
			}
			movesCounter++;
			
			/*
			 * If there is more then 150 moves, try calculating the alternative route
			 * with having altered starting position
			 */
			if (movesCounter > 150) {
				System.out.println("More than 150 moves!");
				System.out.println("Calculating different route");
				startingBias += 10;
				isFinished = false;
				break;
			} else {
				isFinished = true;
			}
			if (startingBias > 350) {
				System.out.println("Cannot find valid route");
				System.exit(404);
			}

		}
		return new MovesAndPoints(moves, features);	
	}

	public static void main(String[] args) {
		// Geometry of the confinement area
		List<Point> confinementAreaPoints = new LinkedList<Point>();
		confinementAreaPoints.add(NORTH_WEST);
		confinementAreaPoints.add(NORTH_EAST);
		confinementAreaPoints.add(SOUTH_EAST);
		confinementAreaPoints.add(SOUTH_WEST);
		confinementAreaPoints.add(NORTH_WEST);
		// Update the class variable
		confinementArea = Polygon.fromLngLats(List.of(confinementAreaPoints));

		// Name passed arguments
		Point startingPoint = Point.fromLngLat(Double.parseDouble(args[4]), Double.parseDouble(args[3]));
		String dateDay = args[0];
		String dateMonth = args[1];
		String dateYear = args[2];
		String port = args[6];

		/*
		 * Problematic region, estimating points where is avoided, however
		 * the sensors that are in such region are fully accessible and are able to be
		 * read and analyzed
		 */
		var listOfP = new ArrayList<Point>();
		listOfP.add(Point.fromLngLat(-3.186995, 55.945421));
		listOfP.add(Point.fromLngLat(-3.186753, 55.945096));
		listOfP.add(Point.fromLngLat(-3.186646, 55.945108));
		listOfP.add(Point.fromLngLat(-3.186753, 55.945228));
		listOfP.add(Point.fromLngLat(-3.186753, 55.945228));
		listOfP.add(Point.fromLngLat(-3.186689, 55.945303));
		listOfP.add(Point.fromLngLat(-3.186791, 55.945294));
		listOfP.add(Point.fromLngLat( -3.186898, 55.945436));
		listOfP.add(Point.fromLngLat(-3.186995, 55.945421));
		var feature = Feature.fromGeometry(Polygon.fromLngLats(List.of(listOfP)));
		errorRegion = FeatureCollection.fromFeature(feature);

		// Check if the starting position is within the confinement area
		if (isTouches(startingPoint, FeatureCollection.fromFeature(Feature.fromGeometry(confinementArea))) == false) {
			System.out.println("Error: Starting Point is defined not in the working area. Exiting program now");
			System.exit(69);
		}

		// Read and store the sensors for the specific day in an array listOfEntries
		String urlStringForPoints = "http://localhost:" + port + "/maps/" + dateYear + "/" + dateMonth + "/" + dateDay
				+ "/air-quality-data.json";
		System.out.println("Sensor Data was read from the Network");
		Type listType = new TypeToken<ArrayList<Sensor>>() {
		}.getType();
		ArrayList<Sensor> listOfEntries = new Gson().fromJson(NetworkOperations.readFileToString(urlStringForPoints),
				listType);

		// Read the no fly areas and update them to class variable
		String urlStringNoFly = "http://localhost:" + port + "/buildings/no-fly-zones.geojson";
		System.out.println("No-fly zones were read from the Network");
		noFlyAreas = FeatureCollection.fromJson(NetworkOperations.readFileToString(urlStringNoFly));

		// Check if the starting point is inside the no Fly Area
		if (isTouches(startingPoint, noFlyAreas)) {
			System.out.println("ERROR: Starting Point is in a forbidden zone. Exiting");
			System.exit(69);
		}

		// Set the coordinates for each sensor from its w3w location
		for (Sensor entry : listOfEntries) {
			String str[] = entry.getLocation().split("\\.");
			var Words = new Gson().fromJson(NetworkOperations.readFileToString(
					"http://localhost:" + port + "/words/" + str[0] + "/" + str[1] + "/" + str[2] + "/details.json"),
					Words.class);
			entry.setLat(Words.getCoordinates().getLatitude());
			entry.setLng(Words.getCoordinates().getLongitude());
		}

		// Initialize the lists of the results from the algorithm
		var moves = new ArrayList<Move>();
		var features = new ArrayList<Feature>();

		// Execute algorithm
		var result = algorithm(startingPoint, listOfEntries);

		// Trying for find alternative route if the first run was not successful
		while (isFinished == false) {
			result = algorithm(startingPoint, listOfEntries);
		}
		
		// retrieve the features (Points) and the moves from the algorithm result
		features = result.getFeatures();
		moves = result.getMoves();
		features.add(createPath(moves));
		var analyzedPointsAndPath = FeatureCollection.fromFeatures(features);
		// Save the GEOJSON file with readings and a path
		try {
			String fileName = String.format("readings-%s-%s-%s.geojson", dateDay, dateMonth, dateYear);
			IOOperations.writeFeaturesToGEOJSON(fileName, analyzedPointsAndPath);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		// Save the flight path
		try {
			String fileName = String.format("flightpath-%s-%s-%s.txt", dateDay, dateMonth, dateYear);
			IOOperations.writeMovesToFile(moves, fileName);
		} catch (Exception e) {
			e.printStackTrace();
		}

	}
}
