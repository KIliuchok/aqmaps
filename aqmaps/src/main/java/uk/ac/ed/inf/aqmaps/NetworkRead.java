package uk.ac.ed.inf.aqmaps;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse.BodyHandlers;

public class NetworkRead {

	var client = HttpClient.newHttpClient();
	var request = HttpRequest.newBuilder().uri(URI.create(urlString)).build();
	var response = client.send(request, BodyHandlers.ofString());
	
}
