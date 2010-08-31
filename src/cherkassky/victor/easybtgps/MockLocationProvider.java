package cherkassky.victor.easybtgps;

import java.io.IOException;
import java.util.List;

import android.location.Location;
import android.location.LocationManager;
import android.util.Log;


/**
 * @author victor
 *
 * Created Aug 30, 2010
 *
 */
public class MockLocationProvider extends Thread {

    private List<String> data;

    private LocationManager locationManager;

    private String mockLocationProvider;

    private String LOG_TAG = "MockLocationProvider";

    public MockLocationProvider(LocationManager locationManager,
            String mocLocationProvider, List<String> data) throws IOException {

        this.locationManager = locationManager;
        this.mockLocationProvider = mocLocationProvider;
        this.data = data;
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
            Location location = new Location(mockLocationProvider);
            location.setLatitude(latitude);
            location.setLongitude(longitude);
            location.setAltitude(altitude);

            Log.e(LOG_TAG, location.toString());

            // set the time in the location. If the time on this location
            // matches the time on the one in the previous set call, it will be
            // ignored
            location.setTime(System.currentTimeMillis());

            locationManager.setTestProviderLocation(mockLocationProvider,
                    location);
        }
    }
}