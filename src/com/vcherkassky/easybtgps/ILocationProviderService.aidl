package com.vcherkassky.easybtgps;

interface ILocationProviderService {

	int getServiceStatus();
	
	void setBluetoothDevice(String address);
	
	void setLocationProviderName(String name);
	
	String getLocationProviderName();
	
	void startLogging();
	
	void stopLogging();
	
}