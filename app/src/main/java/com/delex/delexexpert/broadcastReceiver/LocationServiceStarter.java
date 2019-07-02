package com.delex.delexexpert.broadcastReceiver;

import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.util.Log;

import com.delex.delexexpert.application.BaseApplication;
import com.delex.delexexpert.firebase.DataBase;
import com.delex.delexexpert.service.LocationJobService;
import com.delex.delexexpert.userSession.ExpertSessionManager;
import com.delex.delexexpert.util.Commonlib;

import java.util.concurrent.TimeUnit;

public class LocationServiceStarter extends BroadcastReceiver {

    public static final String TAG = LocationServiceStarter.class.getSimpleName();

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        DataBase dataBase = new DataBase(context);

        if (Intent.ACTION_MY_PACKAGE_REPLACED.equals(action)) {
            Log.d(TAG, "onReceive: ACTION_MY_PACKAGE_REPLACED");
            dataBase.writeStateData(false, false, false, null, "", "업데이트완료");
            receiverServiceStart(context, true);

        } else if (Intent.ACTION_BOOT_COMPLETED.equals(action)) {
            Log.d(TAG, "onReceive: ACTION_BOOT_COMPLETED");
            dataBase.writeStateData(false, false, false, null, "", "재부팅 완료");
            receiverServiceStart(context, true);
        }
    }

    public void receiverServiceStart(Context context, boolean isAction) {
        if (isAction) {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                ComponentName componentName = new ComponentName(context, LocationJobService.class);
                JobInfo builder = null;
                builder = new JobInfo.Builder(1000, componentName)
                        .setMinimumLatency(TimeUnit.MINUTES.toMillis(1))
//                        .setPersisted(true)
                        .setOverrideDeadline(TimeUnit.MINUTES.toMillis(10))
                        .build();

                JobScheduler jobScheduler = (JobScheduler) context.getSystemService(Context.JOB_SCHEDULER_SERVICE);
                jobScheduler.schedule(builder);
            } else {
                Commonlib.serviceCheckAndStart(context, true);
            }
        }
    }
}