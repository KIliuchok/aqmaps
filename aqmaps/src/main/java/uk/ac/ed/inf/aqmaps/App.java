package uk.ac.ed.inf.aqmaps;

import com.mapbox.geojson.*;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;

import java.util.*;

public class App 
{
	
    public static void main( String[] args )
    {
    	
    	final String urlString = "http://localhost:" + args[3] + "/maps/" + args[2] + "/" + args[0] + "/" + args[1] + "/air-quality-data.json";
    	
        String string = NetworkRead.readNetworkToString(urlString);
        System.out.println(string);
        Type listType = new TypeToken<ArrayList<AirQualityEntry>>() {}.getType();
        ArrayList<AirQualityEntry> listOfEntries = new Gson().fromJson(string, listType);
        
        System.out.println(listOfEntries.size());
        System.out.print(listOfEntries.get(1).location);
    }
}
