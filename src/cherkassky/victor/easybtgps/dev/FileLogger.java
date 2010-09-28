package cherkassky.victor.easybtgps.dev;

import java.util.Collection;

import cherkassky.victor.easybtgps.nmea.Message;
import android.util.Printer;


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
