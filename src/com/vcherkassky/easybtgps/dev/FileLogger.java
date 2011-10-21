package com.vcherkassky.easybtgps.dev;

import java.util.Collection;

import android.util.Printer;

import com.vcherkassky.easybtgps.nmea.Message;


/**
 * @author victor
 *
 * Created Sep 28, 2010
 *
 */
public interface FileLogger extends Printer {
	
	void startLogging();
	
	void stopLogging();
	
	void startNewLog(String fileName);
	
	void log(String message);
	
	void log(Collection<Message> messages, boolean count);

}
