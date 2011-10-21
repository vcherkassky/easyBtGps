package com.vcherkassky.easybtgps;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.UUID;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.location.Location;
import android.location.LocationManager;
import android.util.Log;
import android.widget.Toast;

import com.vcherkassky.easybtgps.nmea.LocationDataExtractor;
import com.vcherkassky.easybtgps.nmea.Message;
import com.vcherkassky.easybtgps.nmea.Message.Type;


/**
 * @author victor
 *
 * Created Aug 31, 2010
 *
 */
public class BluetoothGpsLocationProvider extends CustomLocationProviderThread {
	
	private static final String TAG = BluetoothGpsLocationProvider.class.getSimpleName();
	
	private static final UUID DEFAULT_SPP_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
	
	private static final Message.Type DEFAULT_FIRST_MESSAGE_TYPE = Type.GPGGA; 
	
    private final BluetoothDevice mDevice;
    
    private BluetoothSocket mBluetoothSocket;
    
    private Message.Type mFirstMessageType;
    
	public BluetoothGpsLocationProvider(LocationManager locationManager, String providerName, Context context, BluetoothDevice device, boolean loggingEnabled) {

		super(locationManager, providerName, context, loggingEnabled);
		
		this.mDevice = device;

		try {
			this.mBluetoothSocket = this.mDevice.createRfcommSocketToServiceRecord(DEFAULT_SPP_UUID);
		} 
		catch(IOException e) {
			Log.e(TAG, "Could not connect to bluetooth device", e);
			Toast.makeText(context, "Could not connect to bluetooth device", Toast.LENGTH_LONG).show();
		}
	}

	/**
	 * @see java.lang.Thread#run()
	 */
	@Override
	public void run() {
        Log.d(TAG, "STARTING TO CONNECT THE SOCKET");

        try {
        	mBluetoothSocket.connect();
        } catch (IOException e) {
            Log.e(TAG, "Could not connect to device", e);
            Toast.makeText(this.mContext, "Could not connect bluetooth to device", Toast.LENGTH_LONG).show();
            this.destroy();
        }

    	final ArrayList<Message> messagesBatch = new ArrayList<Message>();
        InputStream in = null;
        try {
            in = mBluetoothSocket.getInputStream();
            InputStreamReader isr = new InputStreamReader(in);
            BufferedReader br = new BufferedReader(isr);
            
        	String firstLine = br.readLine();
            Log.d("NMEA", "First line:");
            Log.d("NMEA", firstLine);
            
        	Message message = new Message(firstLine);
        	this.mFirstMessageType = message.getType();
        	if(this.mFirstMessageType == Type.GPGSV) {
        		this.mFirstMessageType = DEFAULT_FIRST_MESSAGE_TYPE;
        	}
        	
            try {
            	
	            while (true) {
	            	String retrievedLine = br.readLine();
	                Log.d("NMEA", retrievedLine);
	                
	                message = new Message(retrievedLine);
	                
	                if(message.getType() == this.mFirstMessageType) {
	                	
	                	Location location = new Location(mProviderName);
	                	LocationDataExtractor.updateLocation(location, messagesBatch);
	                	
	                	location.setTime(System.currentTimeMillis());
	                	
	                	this.mLocationManager.setTestProviderLocation(mProviderName, location);
	                	
                		this.log(messagesBatch);
	                	
	                	messagesBatch.clear();
	                }
	                
	                messagesBatch.add(message);
	            }
            } catch (IOException e) {
                Log.e(TAG, "Error occured while retrieving info from device", e);
            }
        } catch (IOException e) {
        	Log.e(TAG, "Could not initiate info retrieval from device", e);
        } finally {
        	try {
        		if(in != null) {
        			in.close();
        		}
			}
			catch(IOException e) {
				Log.e(TAG, "Unknown IO error occured", e);
			}
        }
    }

	@Override
    public void destroy() {
		
		super.destroy();
		
        try {
        	mBluetoothSocket.close();
        } catch (IOException e) {
            Log.e(TAG, this.getName() + " SOCKET NOT CLOSED");
        }
    }
    
}
