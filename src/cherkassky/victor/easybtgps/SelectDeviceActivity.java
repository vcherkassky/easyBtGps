package cherkassky.victor.easybtgps;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Spinner;
import android.widget.Toast;

public class SelectDeviceActivity extends Activity {
	
	private static final String TAG = SelectDeviceActivity.class.getSimpleName();

    private static final int REQUEST_ENABLE_BT = 1;
    
    private static final String BT_GPS_SERVICE_NAME = "cherkassky.victor.easybtgps.BT_GPS_SERVICE";
    
	private BluetoothAdapter mBluetoothAdapter;
	
	private BluetoothDevice mSelectedDevice;
	
	private ILocationProviderService mService;
	
	private CheckBox cbOverrideSystemData;
	
	private boolean mLoggingEnabled;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        
    	super.onCreate(savedInstanceState);
        
    	setContentView(R.layout.select_device);
    	
    	mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    	if (mBluetoothAdapter == null) {
    	    // Device does not support Bluetooth
            Toast.makeText(this, R.string.bt_not_supported_leaving, Toast.LENGTH_SHORT).show();
            finish();
    	}
    	
    	if (!mBluetoothAdapter.isEnabled()) {
    	    Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
    	    startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
    	}
        
        Spinner deviceSelector = (Spinner) this.findViewById(R.id.device_selector);
        
        List<BluetoothDeviceDecorator> adapters = this.queryPairedBtDevices();
        ArrayAdapter<BluetoothDeviceDecorator> adapter = new ArrayAdapter<BluetoothDeviceDecorator>(this,
                android.R.layout.simple_spinner_item, adapters);
        
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        
        deviceSelector.setAdapter(adapter);
        
        deviceSelector.setOnItemSelectedListener(new OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> paramAdapterView, View targetView, int position, long id) {

				BluetoothDeviceDecorator adapter = (BluetoothDeviceDecorator)paramAdapterView.getItemAtPosition(position);
				mSelectedDevice = adapter.getDevice();
			}

			@Override
			public void onNothingSelected(AdapterView<?> paramAdapterView) {

				//TODO: implement this method if it is needed
			}
        	
		});
        
        cbOverrideSystemData = (CheckBox)findViewById(R.id.override_system_data);
        
        // Watch for button clicks.
        Button button = (Button)findViewById(R.id.start);
        button.setOnClickListener(mStartListener);
        button = (Button)findViewById(R.id.stop);
        button.setOnClickListener(mStopListener);
        button = (Button)findViewById(R.id.start_logging);
        button.setOnClickListener(mStartLoggingListener);
        button = (Button)findViewById(R.id.stop_logging);
        button.setOnClickListener(mStopLoggingListener);
    }
    
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
        case REQUEST_ENABLE_BT:
            // When the request to enable Bluetooth returns
            // User did not enable Bluetooth or an error occured
        	if (!mBluetoothAdapter.isEnabled()) {
                Toast.makeText(this, R.string.bt_not_enabled_leaving, Toast.LENGTH_SHORT).show();
                finish();
        	}
        }
    }

    protected List<BluetoothDeviceDecorator> queryPairedBtDevices() {
    	
    	Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
    	List<BluetoothDeviceDecorator> adapters = new ArrayList<SelectDeviceActivity.BluetoothDeviceDecorator>(pairedDevices.size());
    	// If there are paired devices
    	if (pairedDevices.size() > 0) {
    	    // Loop through paired devices
    	    for (BluetoothDevice device : pairedDevices) {
    	        // Add the name to an array adapter to show in a ListView
    	    	adapters.add(new BluetoothDeviceDecorator(device));
    	    }
    	}
    	
    	return adapters;
    }


    /**
     * Class for interacting with the main interface of the service.
     */
    private ServiceConnection mConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className,
                IBinder service) {
            // This is called when the connection with the service has been
            // established, giving us the service object we can use to
            // interact with the service.  We are communicating with our
            // service through an IDL interface, so get a client-side
            // representation of that from the raw service object.
            mService = ILocationProviderService.Stub.asInterface(service);

            if(mLoggingEnabled) {
	    		try {
					mService.startLogging();
				}
				catch(RemoteException e) {
					Log.e(TAG, "Could not call start logging on the service", e);
					Toast.makeText(SelectDeviceActivity.this, "Could not start logging", Toast.LENGTH_LONG);
				}
            } else {
				try {
					mService.stopLogging();
				}
				catch(RemoteException e) {
					Log.e(TAG, "Could not call stop logging on the service", e);
					Toast.makeText(SelectDeviceActivity.this, "Could not start logging", Toast.LENGTH_LONG);
				}
			}
        }

        public void onServiceDisconnected(ComponentName className) {
            // This is called when the connection with the service has been
            // unexpectedly disconnected -- that is, its process crashed.
            mService = null;
        }
    };

    private OnClickListener mStartListener = new OnClickListener() {
        public void onClick(View v) {
            // Make sure the service is started.  It will continue running
            // until someone calls stopService().
            // We use an action code here, instead of explicitly supplying
            // the component name, so that other packages can replace
            // the service.
        	
        	Intent serviceStartIntent = new Intent(BT_GPS_SERVICE_NAME);
        	serviceStartIntent.putExtra(LocationProviderService.INTENT_EXTRA_ADDRESS,	SelectDeviceActivity.this.mSelectedDevice.getAddress());
    		serviceStartIntent.putExtra(LocationProviderService.INTENT_EXTRA_OVERRIDE_SYSTEM_PROVIDER, cbOverrideSystemData.isChecked());
        	
			startService(serviceStartIntent);
        }
    };

    private OnClickListener mStopListener = new OnClickListener() {
        public void onClick(View v) {
            // Cancel a previous call to startService().  Note that the
            // service will not actually stop at this point if there are
            // still bound clients.
            stopService(new Intent(BT_GPS_SERVICE_NAME));
        }
    };

    private OnClickListener mStartLoggingListener = new OnClickListener() {
        public void onClick(View v) {
        	
        	mLoggingEnabled = true;
        	
        	if(mService == null) {
        		
                bindService(new Intent(ILocationProviderService.class.getName()), mConnection, Context.BIND_AUTO_CREATE);
                
                return;
        	}
        	
        	if(mService != null) {
        		
        		try {
					mService.startLogging();
				}
				catch(RemoteException e) {
					Log.e(TAG, "Could not call start logging on the service", e);
					Toast.makeText(SelectDeviceActivity.this, "Could not start logging", Toast.LENGTH_LONG);
				}
        	}
        }
    };

    private OnClickListener mStopLoggingListener = new OnClickListener() {
        public void onClick(View v) {
        	
        	mLoggingEnabled = false;
        	
        	if(mService == null) {
        		
                bindService(new Intent(ILocationProviderService.class.getName()), mConnection, Context.BIND_AUTO_CREATE);
                
                return;
        	}
        	
        	if(mService != null) {
        		
        		try {
					mService.stopLogging();
				}
				catch(RemoteException e) {
					Log.e(TAG, "Could not call stop logging on the service", e);
					Toast.makeText(SelectDeviceActivity.this, "Could not start logging", Toast.LENGTH_LONG);
				}
        	}
        }
    };
    
    private static class BluetoothDeviceDecorator {
    	
    	private BluetoothDevice mBluetoothDevice;

		public BluetoothDeviceDecorator(BluetoothDevice mBluetoothDevice) {

			this.mBluetoothDevice = mBluetoothDevice;
		}

		/**
		 * Gets the mBluetoothDevice 
		 *
		 * @return the mBluetoothDevice
		 */
		public BluetoothDevice getDevice() {
		
			return this.mBluetoothDevice;
		}

		/**
		 * @see java.lang.Object#toString()
		 */
		@Override
		public String toString() {

			return this.mBluetoothDevice.getName();
		}
    	
    }
}