package com.vcherkassky.easybtgps.nmea;

import java.util.List;

import android.location.Location;
import android.os.Bundle;

import com.vcherkassky.easybtgps.nmea.Message.Type;


/**
 * @author victor
 *
 * Created Aug 31, 2010
 *
 */
public class LocationDataExtractor {
	
	private static final String DELIMITER = ",";

	public static void updateLocation(Location location, List<Message> messagesBatch) {
		
		for(Message message : messagesBatch) {
			
			if(message.getType() == Type.UNKNOWN || !message.validate()) {
				continue;
			}
			switch(message.getType()) {
				case GPGGA:
					LocationDataExtractor.updateFromGPGGA(location, message);
					break;
				case GPGSV:
					break;
				case GPVTG:
					LocationDataExtractor.updateFromGPVTG(location, message);
					break;
			}
		}
	}
	
	public static void updateFromGPGGA(Location location, Message message) {
		// No fix:
		// $GPGGA,173336.146,4956.7314,N,03616.7202,E,0,0,,222.0,M,17.0,M,,*4D
		// With fix:
		// $GPGGA,173557.000,4956.7028,N,03616.8173,E,1,8,1.09,139.5,M,17.0,M,,*5A
//		StringTokenizer st = new StringTokenizer(message.getMessage(), DELIMITER);
		String[] tokens = message.getMessage().split(DELIMITER);
//		tokens[0]; // Should be $GPGGA
//		tokens[1]; // Time - .146 appears to be shown when there is no fix
		String lat = tokens[2]; // Latitude
		String latDir = tokens[3]; // North/South
		String lon = tokens[4]; // Longitude
		String lonDir = tokens[5]; // West/East
		int sats = parseInt(tokens[6]); //Number of satellites
		float acc = parseFloat(tokens[7]); // Horizontal Dilution of Precision (HDOP) - relative accuracy of horizontal position
		double alt = parseDouble(tokens[8]); //Altitude

		
		location.setLatitude(LocationDataExtractor.convertLat(lat, latDir)); 
		location.setLongitude(LocationDataExtractor.convertLon(lon, lonDir));
		location.setAccuracy(acc);
		location.setAltitude(alt);
		
		Bundle locationExtras = location.getExtras();
		if(locationExtras == null) {
			locationExtras = new Bundle();
	        location.setExtras(locationExtras);
		}
        locationExtras.putInt("satellites", sats);

	}
	
	public static void updateFromGPVTG(Location location, Message message) {
		// No fix:
		// $GPVTG,0.00,T,,M,0.00,N,0.00,K,N*32
		// Fix:
		// $GPVTG,88.77,T,,M,0.54,N,1.00,K,A*0D
//		StringTokenizer st = new StringTokenizer(message.getMessage(), DELIMITER);
		String[] tokens = message.getMessage().split(DELIMITER);
//		tokens[0]; // Should be $GPVTG
//		tokens[1]; // True course mode good over ground, degrees
//		tokens[2]; // T
//		tokens[3]; // Magnetic course mode good over ground, degrees
//		tokens[4]; // M
//		tokens[5]; // Ground speed
//		tokens[6]; // N=Knots
		float speed = parseFloat(tokens[7]); // Ground speed
//		tokens[8]; // K=Kilometers per hour
		String mode = tokens[9]; // Mode indicator (A=Autonomous, D=Differential, E=Estimated, N=Data not valid)
		
		if(mode.equalsIgnoreCase("N")) {
			return;
		}
		
		location.setSpeed(convertSpeedToMps(speed));
	}
	
	/**
	 * Converts from 4956.7314 where 49 is degrees and 56.7314 is minutes to double form 
	 * 
	 * @param lat latitude in form of 4956.7314 where 49 is degrees and 56.7314 is minutes
	 * @param latDir N or S meaning North and South
	 * @return
	 */
	public static double convertLat(String lat, String latDir) {
		
		final int pointIndex = lat.indexOf(".");
		final int minutesStartIndex = pointIndex - 2; 
		// Putting degrees in latitude - first chars, i.e. 49
		double latitude = parseDouble(lat.substring(0, minutesStartIndex));
		// This is minutes, i.e. 56.7314
		double minutes = parseDouble(lat.substring(minutesStartIndex));
		// Adding minutes converted to degrees to latitude
		latitude += minutes / 60d;
		// Assign a minus if it is south
		if("S".equalsIgnoreCase(latDir)) {
			latitude = -latitude;
		}
		
		return latitude;
	}
	
	/**
	 * Converts from 03616.7202 where 36 is degrees and 16.7202 is minutes to double form 
	 * 
	 * @param lon longitude in form of 03616.7202 where 36 is degrees and 16.7202 is minutes
	 * @param lonDir W or E meaning West and East
	 * @return
	 */
	public static double convertLon(String lon, String lonDir) {
		
		final int pointIndex = lon.indexOf(".");
		final int minutesStartIndex = pointIndex - 2; 
		// Putting degrees in latitude - first chars, i.e. 036
		double longitude = parseDouble(lon.substring(0, minutesStartIndex));
		// This is minutes, i.e. 16.7202
		double minutes = parseDouble(lon.substring(minutesStartIndex));
		// Adding minutes converted to degrees to latitude
		longitude += minutes / 60d;
		// Assign a minus if it is south
		if("W".equalsIgnoreCase(lonDir)) {
			longitude = -longitude;
		}
		
		return longitude;
	}
	
	/**
	 * Converts speed from kilometers per hour to meters per second
	 * 
	 * @param speedMetersSec
	 * @return
	 */
	public static float convertSpeedToMps(float speedKmph) {
		
		return speedKmph / 3.6f;
	}
	
	public static int parseInt(String string) {
		
		if(string == null || "".equals(string.trim())) {
			return 0;
		}

		return Integer.parseInt(string);
	}
	
	public static double parseDouble(String string) {
		
		if(string == null || "".equals(string.trim())) {
			return 0d;
		}

		return Double.parseDouble(string);
	}
	
	public static float parseFloat(String string) {
		
		if(string == null || "".equals(string.trim())) {
			return 0f;
		}

		return Float.parseFloat(string);
	}
}
