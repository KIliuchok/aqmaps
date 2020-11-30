package uk.ac.ed.inf.aqmaps;

public class Words {
	private String country;
	private Square square;
	private String nearestPlace;
	private Coordinates coordinates;
	private String words;
	private String language;
	private String webMap;

	public static class Coordinates {
		private String lat;
		private String lng;

		public double getLatitude() {
			return Double.parseDouble(lat);
		}

		public double getLongitude() {
			return Double.parseDouble(lng);
		}
	}

	public Coordinates getCoordinates() {
		return coordinates;
	}

	public static class Square {
		String squareName;
		Coordinates squareCoordinates;
	}
}
