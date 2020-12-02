package uk.ac.ed.inf.aqmaps;

import com.mapbox.geojson.*;
import com.mapbox.geojson.LineString;
import com.mapbox.geojson.Point;
import com.mapbox.geojson.Polygon;

import org.wololo.jts2geojson.GeoJSONReader;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;

import com.mapbox.turf.*;

import java.util.*;

public class App {

	// Constant Points for the Confinement Area
	private static final Point NORTH_WEST = Point.fromLngLat(-3.192473, 55.946233);
	private static final Point NORTH_EAST = Point.fromLngLat(-3.184319, 55.946233);
	private static final Point SOUTH_EAST = Point.fromLngLat(-3.184319, 55.942617);
	private static final Point SOUTH_WEST = Point.fromLngLat(-3.192473, 55.942617);

	private static final double MOVE_LENGTH = 0.0003;

	private static int controlBit = 0;
	private static int variableForCorrection = 10;
	private static boolean isFinished = false;
	private static FeatureCollection errorRegion;

	// Checks if for the last 4 moves moves exhibited a loop pattern
	private static boolean isLoop(Move[] moves) {
		int direction0 = moves[0].getDirection();
		int direction1 = moves[1].getDirection();
		int direction2 = moves[2].getDirection();
		int direction3 = moves[3].getDirection();
		if ((direction0 == direction2) && (direction1 == direction3) && (direction0 != direction1)) {
			return true;
		}
		return false;
	}

	// Using JTS Library to check if provided geometry object intersects any of the
	// geometry objects in provided FeatureCollection
	public static boolean isTouches(LineString line, FeatureCollection fc) {
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
	public static boolean isTouches(Point point, FeatureCollection fc) {
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
	
	public static Point fromSensorToPoint(SensorData sensor) {
		return Point.fromLngLat(sensor.getCoordinates().getLng(), sensor.getCoordinates().getLat());
	}
	
	public static void addColourAttribute(Feature feature, String colour) {
		feature.addStringProperty("rgb-string", colour);
		feature.addStringProperty("marker-color", colour);
	}

	public static Feature addAttributes(Feature feature, double reading) {
		if ((reading >= 0) && (reading < 32)) {
			addColourAttribute(feature, "00ff00");
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
		feature.addStringProperty("marker-size", "medium");
		return feature;
	}


	public static double distance(Point a, Point b) {
		return Math.sqrt(Math.pow(a.longitude() - b.longitude(), 2) + Math.pow(a.latitude() - b.latitude(), 2));
	}
	
	// Push test

	@SuppressWarnings("unchecked")
	private static MovesAndPoints algorithm(Point startingPoint1, Polygon workingArea,
			ArrayList<SensorData> listOfEntries1, FeatureCollection noFlyAreas) {
		int movesCounter = 1;
		Point currentPoint;
		boolean reachedBack = false;
		var moves = new ArrayList<Move>();
		var points = new ArrayList<Point>();
		var features1 = new ArrayList<Feature>();
		var visitedPoints = new ArrayList<SensorData>();
		var listOfEntries = new ArrayList<SensorData>();
		listOfEntries = (ArrayList<SensorData>) listOfEntries1.clone();
		var last4Moves = new Move[4];
		boolean empty = false;

		if (controlBit > 0) {
			currentPoint = Point.fromLngLat(
					startingPoint1.longitude() + MOVE_LENGTH * Math.cos(Math.toRadians(controlBit)),
					startingPoint1.latitude() + MOVE_LENGTH * Math.sin(Math.toRadians(controlBit)));
			if (isTouches(currentPoint, noFlyAreas)) {
				var result = new MovesAndPoints(moves, features1);
				isFinished = false;
				return result;
			} else {
				moves.add(new Move(movesCounter, new Coordinates(startingPoint1.longitude(), startingPoint1.latitude()),
						new Coordinates(currentPoint.longitude(), currentPoint.latitude())));
				last4Moves[1] = new Move(movesCounter,
						new Coordinates(startingPoint1.longitude(), startingPoint1.latitude()),
						new Coordinates(currentPoint.longitude(), currentPoint.latitude()));
				movesCounter += 1;
			}

		} else {
			currentPoint = startingPoint1;
		}

		while (reachedBack == false) {
			List<DirectionalLine> listOfLines = new LinkedList<DirectionalLine>();
			/*
			 * for (Feature feature : noFlyAreas.features()) {
			 * System.out.println("Point is not in " + feature.properties().toString());
			 * System.out.println(TurfJoins.inside(Point.fromLngLat(-3.185459,
			 * 55.945020),(Polygon)feature.geometry()));
			 * 
			 * }
			 */
			for (int i = 0; i <= 90; i += 10) {
				var yolo = Point.fromLngLat(currentPoint.longitude() + MOVE_LENGTH * Math.cos(Math.toRadians(i)),
						currentPoint.latitude() + MOVE_LENGTH * Math.sin(Math.toRadians(i)));
				if (TurfJoins.inside(yolo, workingArea) == false) {
					continue;
				}
				var lineStringPoints = new ArrayList<Point>();
				lineStringPoints.add(currentPoint);
				lineStringPoints.add(yolo);
				if (isTouches(LineString.fromLngLats(lineStringPoints), noFlyAreas)) {
					continue;
				}
				if (isTouches(LineString.fromLngLats(lineStringPoints), errorRegion)) {
					continue;
				}
				listOfLines.add(new DirectionalLine(i, yolo, currentPoint));
			}
			for (int i = 100; i <= 180; i += 10) {
				var yolo = Point.fromLngLat(currentPoint.longitude() + MOVE_LENGTH * Math.cos(Math.toRadians(i)),
						currentPoint.latitude() + MOVE_LENGTH * Math.sin(Math.toRadians(i)));
				if (TurfJoins.inside(yolo, workingArea) == false) {
					continue;
				}
				var lineStringPoints = new ArrayList<Point>();
				lineStringPoints.add(currentPoint);
				lineStringPoints.add(yolo);
				if (isTouches(LineString.fromLngLats(lineStringPoints), noFlyAreas)) {
					continue;
				}
				if (isTouches(LineString.fromLngLats(lineStringPoints), errorRegion)) {
					continue;
				}
				listOfLines.add(new DirectionalLine(i, yolo, currentPoint));
			}
			for (int i = 190; i < 280; i += 10) {
				var yolo = Point.fromLngLat(currentPoint.longitude() + MOVE_LENGTH * Math.cos(Math.toRadians(i)),
						currentPoint.latitude() + MOVE_LENGTH * Math.sin(Math.toRadians(i)));
				if (TurfJoins.inside(yolo, workingArea) == false) {
					continue;
				}
				var lineStringPoints = new ArrayList<Point>();
				lineStringPoints.add(currentPoint);
				lineStringPoints.add(yolo);
				if (isTouches(LineString.fromLngLats(lineStringPoints), noFlyAreas)) {
					continue;
				}
				if (isTouches(LineString.fromLngLats(lineStringPoints), errorRegion)) {
					continue;
				}
				listOfLines.add(new DirectionalLine(i, yolo, currentPoint));
			}
			for (int i = 280; i <= 350; i += 10) {
				var yolo = Point.fromLngLat(currentPoint.longitude() + MOVE_LENGTH * Math.cos(Math.toRadians(i)),
						currentPoint.latitude() + MOVE_LENGTH * Math.sin(Math.toRadians(i)));
				if (TurfJoins.inside(yolo, workingArea) == false) {
					continue;
				}
				var lineStringPoints = new ArrayList<Point>();
				lineStringPoints.add(currentPoint);
				lineStringPoints.add(yolo);
				if (isTouches(LineString.fromLngLats(lineStringPoints), noFlyAreas)) {
					continue;
				}
				if (isTouches(LineString.fromLngLats(lineStringPoints), errorRegion)) {
					continue;
				}
				listOfLines.add(new DirectionalLine(i, yolo, currentPoint));
			}

			List<List<DirectionalLine>> listOfLists = new ArrayList<List<DirectionalLine>>();
			for (SensorData aqe : listOfEntries) {
				List<DirectionalLine> temporaryList = new ArrayList<DirectionalLine>();
				for (DirectionalLine line : listOfLines) {
					line.setDistanceToGoal(distance(fromSensorToPoint(aqe), line.getEstimatedPoint()));
					line.setGoal(aqe);
					temporaryList.add(line);
				}
				listOfLists.add(temporaryList);
			}

			var resultingList = new ArrayList<DirectionalLine>();

			for (List<DirectionalLine> list : listOfLists) {
				Collections.sort(list, new SortByDistance());
				resultingList.add(list.get(0));
			}
			Collections.sort(resultingList, new SortByDistance());

			var newDifference = resultingList.get(0);

			moves.add(new Move(movesCounter, new Coordinates(currentPoint.longitude(), currentPoint.latitude()),
					new Coordinates(newDifference.getEstimatedPoint().longitude(),
							newDifference.getEstimatedPoint().latitude())));
			moves.get(movesCounter - 1).addDirection(newDifference.getDirection());
			// System.out.println(moves.get(movesCounter - 1).toString());

			last4Moves[movesCounter % 4] = (new Move((movesCounter),
					new Coordinates(currentPoint.longitude(), currentPoint.latitude()),
					new Coordinates(newDifference.getEstimatedPoint().longitude(),
							newDifference.getEstimatedPoint().latitude())));
			if (movesCounter % 4 == 0) {
				last4Moves[3].addDirection(newDifference.getDirection());
			} else {
				last4Moves[movesCounter % 4].addDirection(newDifference.getDirection());
			}
			points.add(Point.fromLngLat(moves.get(movesCounter - 1).getBeforeMove().getLng(),
					moves.get(movesCounter - 1).getBeforeMove().getLat()));
			points.add(Point.fromLngLat(moves.get(movesCounter - 1).getAfterMove().getLng(),
					moves.get(movesCounter - 1).getAfterMove().getLat()));

			currentPoint = newDifference.getEstimatedPoint();

			if (distance(currentPoint, fromSensorToPoint(newDifference.getGoal())) < 0.0002) {
				// System.out.println(newDifference.dest.location + " " +
				// newDifference.dest.reading);
				moves.get(movesCounter - 1).addLocation(newDifference.getGoal().getLocation());
				var temporary = Feature.fromGeometry(Point.fromLngLat(newDifference.getGoal().getCoordinates().getLng(),
						newDifference.getGoal().getCoordinates().getLat()));
				temporary.addStringProperty("location", newDifference.getGoal().getLocation());
				if (newDifference.getGoal().getReading() != null) {
					if (newDifference.getGoal().getBattery() < 10.0) {
						temporary.addStringProperty("rgb-string", "#000000");
						temporary.addStringProperty("marker-color", "#000000");
						temporary.addStringProperty("marker-symbol", "cross");
					} else if (newDifference.getGoal().getReading().contains("n") == false) {
						addAttributes(temporary, Double.parseDouble(newDifference.getGoal().getReading()));
					}
				}
				if (empty == false) {
					features1.add(temporary);
				}
				listOfEntries.remove(newDifference.getGoal());
				visitedPoints.add(newDifference.getGoal());
			}

			if (distance(currentPoint, startingPoint1) < 0.0003 && (listOfEntries.isEmpty() == true)) {
				System.out.println("Finished and reached to the starting point");
				break;
			}

			if (listOfEntries.isEmpty()) {
				listOfEntries.add(new SensorData(startingPoint1));
				empty = true;
			}

			if (movesCounter > 3) {
				if (isLoop(last4Moves)) {
					while (variableForCorrection < 180) {
						boolean flagg = true;
						var tempPoint = Point.fromLngLat(
								currentPoint.longitude()
								+ MOVE_LENGTH * Math.cos(Math.toRadians(variableForCorrection)),
								currentPoint.latitude()
								+ MOVE_LENGTH * Math.sin(Math.toRadians(variableForCorrection)));
						for (Feature feature : noFlyAreas.features()) {
							if (TurfJoins.inside(tempPoint, (Polygon) feature.geometry())) {
								flagg = false;
								break;
							}
						}

						variableForCorrection += 10;
						if (flagg == false) {
							continue;
						}
						if (flagg = true) {
							moves.add(new Move(movesCounter,
									new Coordinates(currentPoint.longitude(), currentPoint.latitude()),
									new Coordinates(tempPoint.longitude(), tempPoint.latitude())));
							currentPoint = tempPoint;
							break;
						}

					}
				}
			}
			movesCounter++;

			if (movesCounter == 150) {
				System.out.println("More than 150 moves!");
				System.out.println("Calculating different route");
				controlBit += 10;
				isFinished = false;

				features1.add(Feature.fromGeometry(LineString.fromLngLats(points)));
				var fcaa = FeatureCollection.fromFeatures(features1);
				String fileName = String.format("errorMovesFOR %d.txt", controlBit);
				try {
					System.out.println(String.format("Written error path to result with error %d", controlBit));
					IOOperations.writeFileToGEOJSON(String.format("resultWithErrorfor %d.geojson", controlBit), fcaa);
					IOOperations.writeMovesToFile(moves, fileName);
				} catch (Exception e) {
					e.printStackTrace();
				}

				break;

			} else {
				isFinished = true;
			}
			if (controlBit > 350) {
				System.out.println("Cannot find valid route");
				System.exit(69);
			}

		}

		features1.add(Feature.fromGeometry(LineString.fromLngLats(points)));

		var result = new MovesAndPoints(moves, features1);

		return result;
	}

	public static void main(String[] args) {
		// Geometry of the confinement area
		List<Point> confinementAreaPoints = new LinkedList<Point>();
		confinementAreaPoints.add(NORTH_WEST);
		confinementAreaPoints.add(NORTH_EAST);
		confinementAreaPoints.add(SOUTH_EAST);
		confinementAreaPoints.add(SOUTH_WEST);
		confinementAreaPoints.add(NORTH_WEST);
		Polygon confinementArea = Polygon.fromLngLats(List.of(confinementAreaPoints));

		// Name passed arguments
		Point startingPoint = Point.fromLngLat(Double.parseDouble(args[4]), Double.parseDouble(args[3]));
		String dateDay = args[0];
		String dateMonth = args[1];
		String dateYear = args[2];
		String port = args[6];

		// Problematic region, moving where is avoided as is with noFlyAreas, however
		// the sensors that are in such region are fully accessible and are able to be
		// read and analyzed
		Point p1 = Point.fromLngLat(-3.186995, 55.945421);
		Point p2 = Point.fromLngLat(-3.186753, 55.945096);
		Point p3 = Point.fromLngLat(-3.186646, 55.945108);
		Point p4 = Point.fromLngLat(-3.186753, 55.945228);
		Point p5 = Point.fromLngLat(-3.186753, 55.945228);
		Point p6 = Point.fromLngLat(-3.186689, 55.945303);
		Point p7 = Point.fromLngLat(-3.186791, 55.945294);
		Point p8 = Point.fromLngLat( -3.186898, 55.945436);
		Point p9 = Point.fromLngLat(-3.186995, 55.945421);
		var listOfP = new ArrayList<Point>();
		listOfP.add(p1);
		listOfP.add(p2);
		listOfP.add(p3);
		listOfP.add(p4);
		listOfP.add(p5);
		listOfP.add(p6);
		listOfP.add(p7);
		listOfP.add(p8);
		listOfP.add(p9);
		var feature = Feature.fromGeometry(Polygon.fromLngLats(List.of(listOfP)));
		errorRegion = FeatureCollection.fromFeature(feature);

		// Check if the starting position is within the confinement area
		if (TurfJoins.inside(startingPoint, confinementArea) == false) {
			System.out.println("Error: Starting Point is defined not in the working area. Exiting program now");
			System.exit(69);
		}

		// Read and store the sensors for the specific day in an array listOfEntries
		String urlStringForPoints = "http://localhost:" + port + "/maps/" + dateYear + "/" + dateMonth + "/" + dateDay
				+ "/air-quality-data.json";
		System.out.println("Sensor Data was read from the Network");
		Type listType = new TypeToken<ArrayList<SensorData>>() {
		}.getType();
		ArrayList<SensorData> listOfEntries = new Gson().fromJson(NetworkRead.readNetworkToString(urlStringForPoints),
				listType);

		// Read the no fly areas and store in collection noFlyAreas
		String urlStringNoFly = "http://localhost:" + port + "/buildings/no-fly-zones.geojson";
		System.out.println("No-fly zones were read from the Network");
		var noFlyAreas = FeatureCollection.fromJson(NetworkRead.readNetworkToString(urlStringNoFly));

		// Check if the starting point is inside the no Fly Area
		if (isTouches(startingPoint, noFlyAreas)) {
			System.out.println("ERROR: Starting Point is in a forbidden zone. Exiting");
			System.exit(69);
		}

		// Set the coordinates for each sensor from its w3w location
		for (SensorData entry : listOfEntries) {
			String str[] = entry.getLocation().split("\\.");
			var Words = new Gson().fromJson(NetworkRead.readNetworkToString(
					"http://localhost:" + port + "/words/" + str[0] + "/" + str[1] + "/" + str[2] + "/details.json"),
					Words.class);
			entry.setLat(Words.getCoordinates().getLatitude());
			entry.setLng(Words.getCoordinates().getLongitude());
		}

		var moves = new ArrayList<Move>();
		var features = new ArrayList<Feature>();
		var result = new MovesAndPoints();
		
		// Execute algorithm
		// var result = algorithm(startingPoint, confinementArea, listOfEntries, noFlyAreas);

		// Trying for find alternative route if no route with less then 150 moves is found
		while (isFinished == false && controlBit < 360) {
			result = algorithm(startingPoint, confinementArea, listOfEntries, noFlyAreas);
		}
		if (controlBit == 360) {
			System.out.println("Could not find a valid path. Exiting");
			System.exit(404);
		}

		features = result.getFeatures();
		var fcaa = FeatureCollection.fromFeatures(features);
		try {
			String fileName = String.format("readings-%s-%s-%s.geojson", dateDay, dateMonth, dateYear);
			IOOperations.writeFileToGEOJSON(fileName, fcaa);
		} catch (Exception e) {
			e.printStackTrace();
		}

		moves = result.getMoves();
		try {
			String fileName = String.format("flightpath-%s-%s-%s.txt", dateDay, dateMonth, dateYear);
			IOOperations.writeMovesToFile(moves, fileName);
		} catch (Exception e) {
			e.printStackTrace();
		}

	}
}