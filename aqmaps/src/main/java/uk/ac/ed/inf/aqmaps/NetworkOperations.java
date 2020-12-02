package uk.ac.ed.inf.aqmaps;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse.BodyHandlers;

public class NetworkOperations {
	public static String readFileToString(String urlString) {
		var client = HttpClient.newHttpClient();
		var request = HttpRequest.newBuilder().uri(URI.create(urlString)).build();
		String output = "";
		try {
			var response = client.send(request, BodyHandlers.ofString());
			int responseCode = response.statusCode();
			if (responseCode != 200) {
				System.out.println("Network Error: " + response.statusCode());
				System.exit(60);
			} else {
				output = response.body();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return output;
	}
}
