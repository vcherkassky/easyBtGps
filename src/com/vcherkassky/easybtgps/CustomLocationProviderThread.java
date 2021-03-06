package com.vcherkassky.easybtgps;

import java.util.Collection;
import java.util.Date;

import android.content.Context;
import android.location.LocationManager;

import com.vcherkassky.easybtgps.dev.DefaultFileLogger;
import com.vcherkassky.easybtgps.dev.FileLogger;
import com.vcherkassky.easybtgps.nmea.Message;

/**
 * @author victor
 *
 * Created Sep 2, 2010
 *
 */
public abstract class CustomLocationProviderThread extends Thread {
	
//	private static final String TAG = CustomLocationProviderThread.class.getSimpleName();

	protected LocationManager mLocationManager;

	protected String mProviderName;

	protected Context mContext;
	
	private FileLogger mFileLogger; 

	public CustomLocationProviderThread(LocationManager locationManager, String providerName, Context context, boolean loggingEnabled) {

		this.mLocationManager = locationManager;
		this.mProviderName = providerName;
		this.mContext = context;
		
		if(loggingEnabled) {
			this.startLogging();
		}
	}


	public CustomLocationProviderThread(LocationManager locationManager, String providerName, Context context) {

		this.mLocationManager = locationManager;
		this.mProviderName = providerName;
		this.mContext = context;
	}
	/**
	 * @see java.lang.Thread#destroy()
	 */
	@Override
	public void destroy() {

		if(mFileLogger != null) {
			
			mFileLogger.stopLogging();
		}
	}
	
	public void changeProviderName(String providerName) {
		
		mProviderName = providerName;

		if(mFileLogger != null) {
		
			mFileLogger.startNewLog(getFreshLogFileName());
		}
	}
	
	public void startLogging() {
		
		if(mFileLogger == null) {
			
			mFileLogger = new DefaultFileLogger(mContext, true, getFreshLogFileName());
		}

		mFileLogger.startLogging();
	}
	
	public void stopLogging() {
		
		if(mFileLogger != null) {
			
			mFileLogger.stopLogging();
		}
	}
	
	protected void log(Collection<Message> messages) {
		
		if(mFileLogger != null) {
			
			mFileLogger.log(messages, false);
		}
	}
	
	private String getFreshLogFileName() {
		
		return mProviderName + "_" + DefaultFileLogger.formatDate(new Date()) + ".log";
	}

}
