package com.vcherkassky.easybtgps.dev;

import java.util.Collection;

import com.vcherkassky.easybtgps.nmea.Message;



/**
 * @author victor
 *
 * Created Sep 28, 2010
 *
 */
public class FakeLogger implements FileLogger {

	/**
	 * @see android.util.Printer#println(java.lang.String)
	 */
	@Override
	public void println(String x) {

	}

	/**
	 * @see com.vcherkassky.easybtgps.dev.FileLogger#startLogging()
	 */
	@Override
	public void startLogging() {

	}

	/**
	 * @see com.vcherkassky.easybtgps.dev.FileLogger#stopLogging()
	 */
	@Override
	public void stopLogging() {

	}

	/**
	 * @see com.vcherkassky.easybtgps.dev.FileLogger#startNewLog(java.lang.String)
	 */
	@Override
	public void startNewLog(String fileName) {

	}

	/**
	 * @see com.vcherkassky.easybtgps.dev.FileLogger#log(java.lang.String)
	 */
	@Override
	public void log(String message) {

	}

	/**
	 * @see com.vcherkassky.easybtgps.dev.FileLogger#log(java.util.Collection, boolean)
	 */
	@Override
	public void log(Collection<Message> messages, boolean count) {

	}

}
