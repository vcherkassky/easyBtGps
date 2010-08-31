package cherkassky.victor.easybtgps;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;


/**
 * @author victor
 *
 * Created Aug 30, 2010
 *
 */
public class MockLocationProviderTestActivity extends Activity implements LocationListener {

	private final String LOG_TAG = "locationActivity";
	
	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		setContentView(R.layout.test_location);

		((TextView) this.findViewById(R.id.textView)).setText("Something else");

		// LocationManager locationManager = (LocationManager)
		// getSystemService(Context.LOCATION_SERVICE);
		// locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
		// 0, 0, this);

		LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

		String mockLocationProvider = LocationManager.GPS_PROVIDER;
		locationManager.addTestProvider(mockLocationProvider, false, false,
				false, false, true, true, true, 0, 5);
		locationManager.setTestProviderEnabled(mockLocationProvider, true);
		locationManager.requestLocationUpdates(mockLocationProvider, 0L, 0F, this);

		try {

			List<String> data = new ArrayList<String>();
			InputStream is = this.getAssets().open("locationData.csv");
			BufferedReader reader = new BufferedReader(new InputStreamReader(is));
			String line = null;
			while ((line = reader.readLine()) != null) {

				data.add(line);
			}
			Log.e(LOG_TAG, data.size() + " lines");

			new MockLocationProvider(locationManager, mockLocationProvider, data).start();

		} catch (IOException e) {

			e.printStackTrace();
		}
	}

	/**
	 * @see android.location.LocationListener#onLocationChanged(android.location.Location)
	 */
	@Override
	public void onLocationChanged(Location location) {

		((TextView) this.findViewById(R.id.textView)).setText(
				"LAT:"+location.getLatitude()
				+"\nLON:"+location.getLongitude()
				+"\nALT:"+location.getAltitude()
				+"\nSPEED:"+location.getSpeed());
	}

	/**
	 * @see android.location.LocationListener#onProviderDisabled(java.lang.String)
	 */
	@Override
	public void onProviderDisabled(String provider) {

		// TODO Auto-generated method stub
		
	}

	/**
	 * @see android.location.LocationListener#onProviderEnabled(java.lang.String)
	 */
	@Override
	public void onProviderEnabled(String provider) {

		// TODO Auto-generated method stub
		
	}

	/**
	 * @see android.location.LocationListener#onStatusChanged(java.lang.String, int, android.os.Bundle)
	 */
	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {

		// TODO Auto-generated method stub
		
	}
	
	
}
