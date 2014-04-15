package com.example.proxalert;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

public class ProximityPendingIntentFactory {
	
	public static final String PROX_ALERT_INTENT = "com.example.proxalert.ProximityAlert";

//	public static final String PROXIMITY_ACTION = "com.example.proximityalert.PROXIMITY";
	private static final int REQUEST_CODE = 0;

	public static PendingIntent createPendingIntent(Context context) {
		return PendingIntent.getBroadcast(context, REQUEST_CODE, new Intent(
				PROX_ALERT_INTENT), PendingIntent.FLAG_UPDATE_CURRENT);
	}

}
