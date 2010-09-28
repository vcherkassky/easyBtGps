package cherkassky.victor.easybtgps.dev;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import cherkassky.victor.easybtgps.CustomLocationProviderThread;
import cherkassky.victor.easybtgps.nmea.LocationDataExtractor;


/**
 * @author victor
 *
 * Created Aug 30, 2010
 *
 */
public class MockLocationProvider extends CustomLocationProviderThread {

    private List<String> data;

    private LocationManager locationManager;

    private String LOG_TAG = "MockLocationProvider";

    public MockLocationProvider(LocationManager locationManager, String providerName, Context context) throws IOException {

		super(locationManager, providerName, context);
		
		List<String> data = new ArrayList<String>();
		InputStream is = mContext.getAssets().open("locationData.csv");
		BufferedReader reader = new BufferedReader(new InputStreamReader(is));
		String line = null;
		while ((line = reader.readLine()) != null) {

			data.add(line);
		}
	}

	@Override
    public void run() {

    	int i=0;
    	while(i < data.size()) {

    		String str = data.get(i);
    		i++;
    		if(i >= data.size() - 1) {
    			i = 0;
    		}
    		
            try {

                Thread.sleep(1000);

            } catch (InterruptedException e) {

                e.printStackTrace();
            }

            // Set one position
            String[] parts = str.split(",");
            Double latitude = Double.valueOf(parts[0]);
            Double longitude = Double.valueOf(parts[1]);
            Double altitude = Double.valueOf(parts[2]);
            Float speed = Float.valueOf(parts[3]);
            Location location = new Location(mProviderName);
            location.setLatitude(latitude);
            location.setLongitude(longitude);
            location.setAltitude(altitude);
            location.setSpeed(LocationDataExtractor.convertSpeedToMps(speed));
            
            Bundle locationExtras = new Bundle();
            locationExtras.putInt("satellites", (int) Math.round(15*Math.random()));
            
            location.setExtras(locationExtras);

            Log.e(LOG_TAG, location.toString());

            // set the time in the location. If the time on this location
            // matches the time on the one in the previous set call, it will be
            // ignored
            location.setTime(System.currentTimeMillis());

            locationManager.setTestProviderLocation(mProviderName, location);
        }
    }
}