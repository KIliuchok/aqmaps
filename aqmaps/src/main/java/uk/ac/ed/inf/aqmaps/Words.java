package uk.ac.ed.inf.aqmaps;

public class Words {
		String country;
		Square square;
		public static class Square{
			String squareName;
			Coordinates squareCoordinates;
		}
		String nearestPlace;
		Coordinates coordinates;		
		public static class Coordinates{
			String lat;
			String lng;
		}
		String words;
		String language;
		String webMap;		
}
