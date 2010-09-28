package cherkassky.victor.easybtgps.dev;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Environment;
import android.util.Log;
import cherkassky.victor.easybtgps.nmea.Message;


/**
 * @author victor
 *
 * Created Sep 3, 2010
 *
 */
public class DefaultFileLogger implements FileLogger {
	
	private static final String TAG = DefaultFileLogger.class.getSimpleName();

	private static final String BASE_DIR_NAME = File.separator + "easyBtGps";
	
	private static final Format DATE_FORMAT = new SimpleDateFormat("yyyy.MM.dd_HH.mm.ss.SSS");
	
	private static final int DEFAULT_LOGGING_FREQUENCY = 3;

	private BroadcastReceiver mExternalStorageReceiver;

	private boolean mExternalStorageAvailable = false;

	private boolean mExternalStorageWriteable = false;

	private String mLoggingDirName;
	
	private String mLoggingFileName;

	private boolean mLoggingEnabled;

	private boolean mLoggingPaused;

	private long mLoggingPauseTime;
	
	private int mLoggingCounter;
	
	private int mLoggingFrequency = DEFAULT_LOGGING_FREQUENCY;
	
	private File mLogFile;
	
	private PrintStream mPrintStream;

	protected Context mContext;

	public DefaultFileLogger(Context context, boolean loggingEnabled, String fileName) {

		this.mContext = context;
		this.mLoggingEnabled = loggingEnabled;
		this.mLoggingFileName = fileName;
	}
	
	public DefaultFileLogger(Context context, boolean loggingEnabled, String fileName, int loggingFrequency) {

		this.mContext = context;
		this.mLoggingEnabled = loggingEnabled;
		this.mLoggingFileName = fileName;
		this.mLoggingFrequency = loggingFrequency;
		
		assert(loggingFrequency != 0);
	}
	
	/**
	 * @see android.util.Printer#println(java.lang.String)
	 */
	@Override
	public void println(String message) {

		this.log(message);
	}

	@Override
	public void startLogging() {
		
		mLoggingEnabled = true;
		
		this.startWatchingExternalStorage();
		
		try {
			this.openLogFile();
		}
		catch(IOException e) {
			Log.e(TAG, "Failed to open log file", e);
			try {
				this.closeLogFile();
			}
			catch(IOException e1) {
				Log.e(TAG, "Could not close log file after fail", e1);
			}
		}
	}
	
	@Override
	public void stopLogging() {
		
		mLoggingEnabled = false;
		
		this.stopWatchingExternalStorage();
		
		try {
			this.closeLogFile();
		}
		catch(IOException e) {
			Log.e(TAG, "Failed to close log file", e);
		}
	}

	@Override
	public void startNewLog(String fileName) {
		
		if(mLoggingEnabled && !mLoggingPaused) {
			
			stopLogging();
			
			mLoggingFileName = fileName;
			
			startLogging();
		} else {
			
			mLoggingFileName = fileName;
		}

	}
	
	@Override
	public void log(String message) {

		if(mLoggingEnabled && !mLoggingPaused) {
			
			mPrintStream.println(formatDate(new Date()) + ": " + message);
		}
	}
	
	@Override
	public void log(Collection<Message> messages, boolean count) {

		if(mLoggingEnabled && !mLoggingPaused) {

			if(count) {

            	mLoggingCounter = (mLoggingCounter + 1) % mLoggingFrequency;

            	if(mLoggingCounter != 1) {
            		
            		return;
            	}
			}
			for(Message message : messages) {
				mPrintStream.println(formatDate(new Date()) + ": " + message.getMessage());
			}
		}
	}

	private void updateExternalStorageState() {

		String state = Environment.getExternalStorageState();
		if(Environment.MEDIA_MOUNTED.equals(state)) {
			mExternalStorageAvailable = mExternalStorageWriteable = true;
		}
		else if(Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
			mExternalStorageAvailable = true;
			mExternalStorageWriteable = false;
		}
		else {
			mExternalStorageAvailable = mExternalStorageWriteable = false;
		}
		this.handleExternalStorageState(mExternalStorageAvailable, mExternalStorageWriteable);
	}

	private void startWatchingExternalStorage() {

		mExternalStorageReceiver = new BroadcastReceiver() {

			@Override
			public void onReceive(Context context, Intent intent) {

				Log.i("test", "Storage: " + intent.getData());
				updateExternalStorageState();
			}
		};
		IntentFilter filter = new IntentFilter();
		filter.addAction(Intent.ACTION_MEDIA_MOUNTED);
		filter.addAction(Intent.ACTION_MEDIA_REMOVED);
		mContext.registerReceiver(mExternalStorageReceiver, filter);
		updateExternalStorageState();
	}

	private void stopWatchingExternalStorage() {

		mContext.unregisterReceiver(mExternalStorageReceiver);
	}

	private void handleExternalStorageState(boolean externalStorageAvailable, boolean externalStorageWriteable) {

		if(externalStorageAvailable && externalStorageWriteable) {

			mLoggingDirName = Environment.getExternalStorageDirectory().getAbsolutePath() + BASE_DIR_NAME;

			if(mLoggingPaused) {

				mLoggingPaused = false;
				this.startLogging();
				
				this.log("Logging paused at " + formatDate(new Date(this.mLoggingPauseTime)));
				this.log("Logging resumed at " + formatDate(new Date()));
			}
		}
		else {

			mLoggingPaused = true;
			mLoggingPauseTime = System.currentTimeMillis();
		}
	}
	
	
	private void openLogFile() throws IOException {

		File fileDir = new File(mLoggingDirName);
		mLogFile = new File(mLoggingDirName, mLoggingFileName);
		
		if(!mLogFile.exists()) {
			
			fileDir.mkdirs();
			mLogFile.createNewFile();
		}
		
		mPrintStream = new PrintStream(mLogFile); //Opening appending stream
	}
	
	private void closeLogFile() throws IOException {
		
		if(mPrintStream != null) {
			
			mPrintStream.flush();
			mPrintStream.close();
		}
	}
	
	public static String formatDate(Date date) {
		
		return DATE_FORMAT.format(date);
	}

}
