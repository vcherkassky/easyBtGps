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
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

public class SelectDeviceActivity extends Activity {

    private static final int REQUEST_ENABLE_BT = 1;

	private BluetoothAdapter mBluetoothAdapter;
	
	private BluetoothDevice mSelectedDevice;
	
    private boolean mIsBound;
    
    private LocationProviderService mBoundService;
	
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
//        ArrayAdapter<BluetoothDevice> adapter = new ArrayAdapter<BluetoothDevice>(this, 
//        		android.R.layout.simple_spinner_item, new ArrayList<BluetoothDevice>(mBluetoothAdapter.getBondedDevices()));
        
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        
        deviceSelector.setAdapter(adapter);
        
        deviceSelector.setOnItemSelectedListener(new OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> paramAdapterView, View targetView, int position, long id) {

				BluetoothDeviceDecorator adapter = (BluetoothDeviceDecorator)paramAdapterView.getItemAtPosition(position);
				mSelectedDevice = adapter.getDevice();
//				testLocationService();
			}

			@Override
			public void onNothingSelected(AdapterView<?> paramAdapterView) {

				//TODO: implement this method if it is needed
			}
        	
		});
        
        // Watch for button clicks.
        Button button = (Button)findViewById(R.id.bind);
        button.setOnClickListener(mBindListener);
        button = (Button)findViewById(R.id.unbind);
        button.setOnClickListener(mUnbindListener);

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
    
    private ServiceConnection mConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder service) {
            // This is called when the connection with the service has been
            // established, giving us the service object we can use to
            // interact with the service.  Because we have bound to a explicit
            // service that we know is running in our own process, we can
            // cast its IBinder to a concrete class and directly access it.
            mBoundService = ((LocationProviderService.LocalBinder)service).getService();
            
            // Tell the user about this for our demo.
            Toast.makeText(SelectDeviceActivity.this, R.string.local_service_connected,
                    Toast.LENGTH_SHORT).show();
        }

        public void onServiceDisconnected(ComponentName className) {
            // This is called when the connection with the service has been
            // unexpectedly disconnected -- that is, its process crashed.
            // Because it is running in our same process, we should never
            // see this happen.
            mBoundService = null;
            Toast.makeText(SelectDeviceActivity.this, R.string.local_service_disconnected,
                    Toast.LENGTH_SHORT).show();
        }
    };

    private OnClickListener mBindListener = new OnClickListener() {
        public void onClick(View v) {
            // Establish a connection with the service.  We use an explicit
            // class name because we want a specific service implementation that
            // we know will be running in our own process (and thus won't be
            // supporting component replacement by other applications).
            bindService(new Intent(SelectDeviceActivity.this, 
            		LocationProviderService.class), mConnection, Context.BIND_AUTO_CREATE);
            mIsBound = true;
        }
    };

    private OnClickListener mUnbindListener = new OnClickListener() {
        public void onClick(View v) {
            if (mIsBound) {
                // Detach our existing connection.
                unbindService(mConnection);
                mIsBound = false;
            }
        }
    };
    
    protected void testLocationService() {
    	
//    	LocationManager locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
//    	Toast.makeText(this, locationManager.getProviders(false).toString(), Toast.LENGTH_LONG).show();
    	
    	this.startActivity(new Intent()
    		.setComponent(new ComponentName(this, MockLocationProviderTestActivity.class)));
    }
    
    protected void bindService() {
    	
    	
    }
    
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