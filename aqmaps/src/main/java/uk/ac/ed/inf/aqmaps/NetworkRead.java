package uk.ac.ed.inf.aqmaps;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse.BodyHandlers;

public class NetworkRead {

	
	public static String readNetworkToString(String urlString) {
		var client = HttpClient.newHttpClient();
		var request = HttpRequest.newBuilder().uri(URI.create(urlString)).build();
		String output = "";
		try {
			var response = client.send(request, BodyHandlers.ofString());
			int responseCode = response.statusCode();
			if (responseCode != 200) {
				System.out.println("Network Error: " + response.statusCode());
				System.exit(69);
			} else {
				output = response.toString();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return output;
	}
	
	public static void main(String[] args) {
	
	
	}	
}
