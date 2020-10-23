package uk.ac.ed.inf.aqmaps;

import com.mapbox.geojson.*;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;

import java.util.*;

public class App 
{
	private static final String urlString = "https:/localhost:80/maps/2020/01/03/air-quality-data.json";
	
    public static void main( String[] args )
    {
        String string = NetworkRead.readNetworkToString(urlString);
        
        Type listType = new TypeToken<ArrayList<AirQualityEntry>>() {}.getType();
        ArrayList<AirQualityEntry> listOfEntries = new Gson().fromJson(string, listType);
        
        System.out.println(listOfEntries.size());
    }
}
