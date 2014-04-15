package com.example.proxalert;

import android.app.Service;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

public class ProximityAlertService extends Service implements LocationListener {

	public static final String LATITUDE_INTENT_KEY = "LATITUDE_INTENT_KEY";
	public static final String LONGITUDE_INTENT_KEY = "LONGITUDE_INTENT_KEY";
	public static final String RADIUS_INTENT_KEY = "RADIUS_INTENT_KEY";
	private static final String TAG = "ProximityAlertService";

	private double latitude;
	private double longitude;
	private float radius;
	private LocationManager locationManager;
	private boolean inProximity;

	@Override
	public void onCreate() {
		super.onCreate();
		locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {

		Location bestLocation = null;

		latitude = intent.getDoubleExtra(LATITUDE_INTENT_KEY, Double.MIN_VALUE);
		longitude = intent.getDoubleExtra(LONGITUDE_INTENT_KEY,
				Double.MIN_VALUE);
		radius = intent.getFloatExtra(RADIUS_INTENT_KEY, Float.MIN_VALUE);

		for (String provider : locationManager.getProviders(false)) {
			Location location = locationManager.getLastKnownLocation(provider);

			if (bestLocation == null) {
				bestLocation = location;
			} else {
				// getAccuracy() describes the deviation in meters. So, the
				// smaller the number, the better the accuracy.
				if (location.getAccuracy() < bestLocation.getAccuracy()) {
					bestLocation = location;
				}
			}
		}

		if (bestLocation != null) {
			if (getDistance(bestLocation) <= radius) {
				inProximity = true;
			} else {
				inProximity = false;
			}
		}

		locationManager.requestLocationUpdates(
				LocationManager.NETWORK_PROVIDER, 0, 0, this);

		// meaning that the service should be moved back into the
		// started state (as if onStartCommand() had been called), but do not
		// re-deliver
		// the Intent to onStartCommand()
		return START_STICKY;
	}

	@Override
	public void onLocationChanged(Location location) {
		// TODO Auto-generated method stub
		float distance = getDistance(location);
		if (distance <= radius && !inProximity) {
			inProximity = true;
			Log.i(TAG, "Entering Proximity");
			Toast.makeText(getBaseContext(), "Entering Proximity by service",
					Toast.LENGTH_LONG).show();
			Intent intent = new Intent(
					ProximityPendingIntentFactory.PROX_ALERT_INTENT);
			intent.putExtra(LocationManager.KEY_PROXIMITY_ENTERING, true);
			sendBroadcast(intent);

		} else if (distance > radius && inProximity) {
			inProximity = false;
			Log.i(TAG, "Exiting Proximity");
			Toast.makeText(getBaseContext(), "Exiting Proximity",
					Toast.LENGTH_LONG).show();
			Intent intent = new Intent(
					ProximityPendingIntentFactory.PROX_ALERT_INTENT);
			intent.putExtra(LocationManager.KEY_PROXIMITY_ENTERING, false);
			sendBroadcast(intent);
		} else {
			
			// roaming state - if distance > radius && !inProximity or if distance < radius && inProximity
			
			float distanceFromRadius = Math.abs(distance - radius);

			// Calculate the distance to the edge of the user-defined radius
			// around the target location
			float locationEvaluationDistance = (distanceFromRadius - location
					.getAccuracy()) / 2;

			Toast.makeText(
					getBaseContext(),
					"roaming state and make sure signal correct!" + " , (distanceFromRadius):"
							+ distanceFromRadius
							+ " (locationEvaluationDistance):"
							+ locationEvaluationDistance, Toast.LENGTH_LONG)
					.show();

			locationManager.removeUpdates(this);
			float updateDistance = Math.max(1, locationEvaluationDistance);
			
			Toast.makeText(getBaseContext(), "updateDistance: " + updateDistance,
					Toast.LENGTH_LONG).show();

			String provider;
			if (distanceFromRadius <= location.getAccuracy()
					|| LocationManager.GPS_PROVIDER.equals(location
							.getProvider())) {
				provider = LocationManager.GPS_PROVIDER;
			} else {
				provider = LocationManager.NETWORK_PROVIDER;
			}
			//LocationManager.requestLocationUpdates(String provider, long minTime, float minDistance, 
			//LocationListener listener)
			locationManager.requestLocationUpdates(provider, 0, updateDistance,
					this);
		}

	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		locationManager.removeUpdates(this);
		Toast.makeText(getBaseContext(), "Stoping the service!",
				Toast.LENGTH_LONG).show();
	}

	@Override
	public void onProviderDisabled(String provider) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onProviderEnabled(String provider) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
		// TODO Auto-generated method stub

	}

	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	private float getDistance(Location location) {
		float[] results = new float[1];

		Location.distanceBetween(latitude, longitude, location.getLatitude(),
				location.getLongitude(), results);

		return results[0];
	}

}
