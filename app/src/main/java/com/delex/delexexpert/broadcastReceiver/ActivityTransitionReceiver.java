package com.delex.delexexpert.broadcastReceiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.location.ActivityTransitionEvent;
import com.google.android.gms.location.ActivityTransitionResult;

public class ActivityTransitionReceiver extends BroadcastReceiver {

    public static final String TAG = ActivityTransitionReceiver.class.getSimpleName();

    @Override
    public void onReceive(Context context, Intent intent) {

        if (ActivityTransitionResult.hasResult(intent)) {
            ActivityTransitionResult result = ActivityTransitionResult.extractResult(intent);
            for (ActivityTransitionEvent event : result.getTransitionEvents()) {

                event.getTransitionType();


                Log.d(TAG, "onReceive:"+event);
                Toast.makeText(context, ""+event, Toast.LENGTH_LONG).show();
                // chronological sequence of events....
            }
        }
    }
}
