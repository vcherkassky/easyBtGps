# Easy Bluetooth GPS

Simple Android application, which allows one to use external Bluetooth GPS module. 
Android lacks API (at least before 5th API version) for creating a custom GPS provider, so the app works around this issue using Mock GPS provider and thus requires location simulation to be turned on to work on the device.

## Current status:

* UI is ugly
* Application consists of 2 parts: Location Provider Service and an Activity, which controls the Service
* Location Provider Service does not properly provide satellites and current speed info