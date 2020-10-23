package uk.ac.ed.inf.aqmaps;

import com.mapbox.geojson.*;
import com.google.*;
/**
 * Hello world!
 *
 */
public class App 
{
	private static final String urlString = "localhost:80";
	
    public static void main( String[] args )
    {
        String string = NetworkRead.readNetworkToString(urlString);
        
        
    }
}
