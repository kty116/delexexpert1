package com.delex.delexexpert.broadcastReceiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.util.Log;
import android.widget.Toast;

import com.delex.delexexpert.event.CurrentLocationExpertEvent;
import com.delex.delexexpert.userSession.ExpertSessionManager;
import com.delex.delexexpert.util.Commonlib;

import org.greenrobot.eventbus.EventBus;

public class LocationReceiver extends BroadcastReceiver {

    public static final String TAG = LocationReceiver.class.getSimpleName();
    private String mCarNum;

    @Override
    public void onReceive(Context context, Intent intent) {

        Location location = (Location) intent.getExtras().getParcelable("com.google.android.location.LOCATION");


        if (location != null) {
//            Toast.makeText(context, ""+location.toString(), Toast.LENGTH_SHORT).show();
//            double lat = location.getLatitude();
//            double lon = location.getLongitude();
//
//            if (lat >= 33 && lat < 44 && lon >= 124 && lon < 133) {
//
//                if (location.getAccuracy() <= 0) {
//                    return;
//                }
//
//            if (location.getAccuracy() < 15) {
//                    Log.d(TAG, "onReceive: " + location.getLatitude() + " / " + location.getLongitude());
                EventBus.getDefault().post(new CurrentLocationExpertEvent(location));
//            }
//            }
        } else {
            return;
        }
    }
}
