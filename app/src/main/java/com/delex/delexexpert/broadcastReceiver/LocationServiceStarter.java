package com.delex.delexexpert.broadcastReceiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.util.Log;

import com.delex.delexexpert.firebase.DataBase;
import com.delex.delexexpert.userSession.ExpertSessionManager;
import com.delex.delexexpert.util.Commonlib;

public class LocationServiceStarter extends BroadcastReceiver {

    public static final String TAG = LocationServiceStarter.class.getSimpleName();

    @Override
    public void onReceive(final Context context, Intent intent) {
        ExpertSessionManager expertSessionManager = new ExpertSessionManager(context);
        String action = intent.getAction();
        DataBase dataBase = new DataBase(context);

        if (Intent.ACTION_MY_PACKAGE_REPLACED.equals(action)) {
            Log.d(TAG, "onReceive: ACTION_MY_PACKAGE_REPLACED");
            dataBase.writeStateData(false, false, false, "업데이트 완료", "","");
            Commonlib.serviceCheckAndStart(context);

        } else if (Intent.ACTION_BOOT_COMPLETED.equals(action)) {
            dataBase.writeStateData(false, false, false, "재부팅 완료", "","");
            Commonlib.serviceCheckAndStart(context);
        }
    }
}