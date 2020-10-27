package uk.ac.ed.inf.aqmaps;

public class Words {
		String country;
		Square square;		
		String nearestPlace;
		Coordinates coordinates;				
		String words;
		String language;
		String webMap;	
		
		public static class Coordinates{
			String lat;
			String lng;
		}
		public static class Square{
			String squareName;
			Coordinates squareCoordinates;
		}
}
