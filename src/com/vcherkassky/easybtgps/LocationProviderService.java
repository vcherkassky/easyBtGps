package com.vcherkassky.easybtgps;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.location.LocationManager;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;
import android.widget.Toast;

import com.vcherkassky.easybtgps.dev.FileLogger;


/**
 * @author victor
 *
 * Created Aug 30, 2010
 *
 */
public class LocationProviderService extends Service {
	
	private static final String TAG = LocationProviderService.class.getSimpleName();
	
	public static final String INTENT_EXTRA_ADDRESS = "address";
	
	public static final String INTENT_EXTRA_OVERRIDE_SYSTEM_PROVIDER = "overrideSystemProvider";
	
	public static final int SERVICE_STATUS_OK = 0;
	
	public static final int SERVICE_STATUS_ERR = 1;
	
	public static final String BT_GPS_PROVIDER_NAME = "easy_bt_gps";
	
	private String mLocationProviderName;
	
    private NotificationManager mNM;

    private CustomLocationProviderThread mLocationProvider;
    
    private FileLogger mNmeaFileLogger;
    
    private final ILocationProviderService.Stub mBinder = new ILocationProviderService.Stub() {
		
		@Override
		public int getServiceStatus() throws RemoteException {
			
			if(mLocationProvider == null && mLocationProvider.isAlive()) {
				return SERVICE_STATUS_ERR;
			}
			
			return SERVICE_STATUS_OK;
		}

		/**
		 * @see cherkassky.victor.easybtgps.ILocationProviderService#setBluetoothDevice(java.lang.String)
		 */
		@Override
		public void setBluetoothDevice(String deviceAddress) throws RemoteException {

			for(BluetoothDevice device : BluetoothAdapter.getDefaultAdapter().getBondedDevices()) {
				if(device.getAddress().equalsIgnoreCase(deviceAddress)) {
					if(mLocationProvider != null) {
						mLocationProvider.destroy();
					}
					useBtGpsLocationProvider(device, false);
					break;
				}
			}
		}

		/**
		 * @see cherkassky.victor.easybtgps.ILocationProviderService#setLocationProviderName(java.lang.String)
		 */
		@Override
		public void setLocationProviderName(String name) throws RemoteException {

			if(mLocationProviderName == null || mLocationProvider == null || !mLocationProvider.isAlive()) {

				mLocationProviderName = name;
			} else if(!mLocationProviderName.equals(name)) {

				mLocationProvider.suspend();
				
				removeProvider();
				
				mLocationProvider.changeProviderName(name);
				
				addProvider();
				
				mLocationProvider.resume();
			}
		}

		/**
		 * @see cherkassky.victor.easybtgps.ILocationProviderService#getLocationProviderName()
		 */
		@Override
		public String getLocationProviderName() throws RemoteException {

			return mLocationProviderName;
		}

		/**
		 * @see cherkassky.victor.easybtgps.ILocationProviderService#startLogging()
		 */
		@Override
		public void startLogging() throws RemoteException {

			mLocationProvider.startLogging();
			
			if(mNmeaFileLogger == null) {
				
//				mNmeaFileLogger = new DefaultFileLogger(LocationProviderService.this, true, getFreshNmeaLogFileName());
				return;
			}

			mNmeaFileLogger.startLogging();
		}

		/**
		 * @see cherkassky.victor.easybtgps.ILocationProviderService#stopLogging()
		 */
		@Override
		public void stopLogging() throws RemoteException {

			mLocationProvider.stopLogging();
			
			if(mNmeaFileLogger != null) {
				
				mNmeaFileLogger.stopLogging();
			}
		}
	};
    
    @Override
    public void onCreate() {
        mNM = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);

        // Display a notification about us starting.  We put an icon in the status bar.
        showNotification();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i("LocalService", "Received start id " + startId + ": " + intent);
        
        if(intent.hasExtra(INTENT_EXTRA_OVERRIDE_SYSTEM_PROVIDER)
        		&& intent.getBooleanExtra(INTENT_EXTRA_OVERRIDE_SYSTEM_PROVIDER, false)) {
        	
        	mLocationProviderName = LocationManager.GPS_PROVIDER;
        } else {
        	
        	mLocationProviderName = BT_GPS_PROVIDER_NAME;
        }
        
        if(intent.hasExtra(INTENT_EXTRA_ADDRESS)) {
            String deviceAddress = intent.getStringExtra(INTENT_EXTRA_ADDRESS);
			for(BluetoothDevice device : BluetoothAdapter.getDefaultAdapter().getBondedDevices()) {
				if(device.getAddress().equalsIgnoreCase(deviceAddress)) {
					if(mLocationProvider != null) {
						mLocationProvider.destroy();
					}
					useBtGpsLocationProvider(device, false);
					break;
				}
			}
        } else {
        	if(mLocationProvider != null) {
        		mLocationProvider.start();
        	} else {
        		Log.e(TAG, "Location provider was null and no device address passed to service start command");
        		Toast.makeText(this, "Could not start Bluetooth GPS service", Toast.LENGTH_LONG).show();
        		
        		this.stopSelf();
        	}
        }
        
        // We want this service to continue running until it is explicitly
        // stopped, so return sticky.
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        // Cancel the persistent notification.
        mNM.cancel(R.string.local_service_started);

        mLocationProvider.stop();
        
        removeProvider();
        
        // Tell the user we stopped.
        Toast.makeText(this, R.string.local_service_stopped, Toast.LENGTH_SHORT).show();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    /**
     * Show a notification while this service is running.
     */
    private void showNotification() {
        // In this sample, we'll use the same text for the ticker and the expanded notification
        CharSequence text = getText(R.string.local_service_started);

        // Set the icon, scrolling text and timestamp
        Notification notification = new Notification(android.R.drawable.btn_radio, text,
                System.currentTimeMillis());

        // The PendingIntent to launch our activity if the user selects this notification
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
                new Intent(this, SelectDeviceActivity.class), 0);

        // Set the info for the views that show in the notification panel.
        notification.setLatestEventInfo(this, getText(R.string.local_service_label),
                       text, contentIntent);

        // Send the notification.
        // We use a layout id because it is a unique number.  We use it later to cancel.
        mNM.notify(R.string.local_service_started, notification);
    }
    
//	private void useMockLocationProvider() {
//    	
//		LocationManager locationManager = addProvider();
//
//		try {
//
//			mLocationProvider = new MockLocationProvider(locationManager, mLocationProviderName, this);
//			mLocationProvider.start();
//
//		} catch (IOException e) {
//			Log.e(TAG, "Error occured while reading locationData file", e);
//    		Toast.makeText(this, "Error occured while reading locationData file", Toast.LENGTH_LONG).show();
//    		
//    		this.stopSelf();
//		}
//    }
    
    private void useBtGpsLocationProvider(BluetoothDevice bluetoothDevice, boolean loggingEnabled) {
    	
		LocationManager locationManager = addProvider();
		
		mLocationProvider =	new BluetoothGpsLocationProvider(locationManager, mLocationProviderName, this, bluetoothDevice, loggingEnabled);
		mLocationProvider.start();
    }
    
    private LocationManager addProvider() {
		LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

		locationManager.addTestProvider(mLocationProviderName, false, true, false, false, true, true, true, 0, 3);
		locationManager.setTestProviderEnabled(mLocationProviderName, true);

		// These 2 lines are adding new NmeaListener
//		locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 300, 0, mNmeaLocationListener);
//		locationManager.addNmeaListener(mNmeaListener);
		
		return locationManager;
    }
    
    private void removeProvider() {
		LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
    	
		locationManager.removeTestProvider(mLocationProviderName);
		
		// These 2 lines are removing NmeaListener
//		locationManager.removeUpdates(mNmeaLocationListener);
//		locationManager.removeNmeaListener(mNmeaListener);
    }
    
//    private final NmeaListener mNmeaListener = new NmeaListener() {
//		
//		@Override
//		public void onNmeaReceived(long timestamp, String nmea) {
//
//			if(mNmeaFileLogger != null) {
//				
//				mNmeaFileLogger.log(nmea);
//			}
//		}
//	}; 
	
//	private final LocationListener mNmeaLocationListener = new LocationListener() {
//
//		/**
//		 * @see android.location.LocationListener#onLocationChanged(android.location.Location)
//		 */
//		@Override
//		public void onLocationChanged(Location paramLocation) {
//			
//			if(mNmeaFileLogger != null) {
//				
//				paramLocation.dump(mNmeaFileLogger, "");
//			}
//		}
//
//		/**
//		 * @see android.location.LocationListener#onStatusChanged(java.lang.String, int, android.os.Bundle)
//		 */
//		@Override
//		public void onStatusChanged(String paramString, int paramInt, Bundle paramBundle) {
//		}
//
//		/**
//		 * @see android.location.LocationListener#onProviderEnabled(java.lang.String)
//		 */
//		@Override
//		public void onProviderEnabled(String paramString) {
//		}
//
//		/**
//		 * @see android.location.LocationListener#onProviderDisabled(java.lang.String)
//		 */
//		@Override
//		public void onProviderDisabled(String paramString) {
//		}
//		
//	};
	
//	private String getFreshNmeaLogFileName() {
//		
//		return "nmea_" + DefaultFileLogger.formatDate(new Date()) + ".log";
//	}

}