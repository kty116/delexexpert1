package com.delex.delexexpert.service;

import android.app.job.JobParameters;
import android.app.job.JobService;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.util.Log;

import com.delex.delexexpert.util.Commonlib;

@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public class LocationJobService extends JobService {

    public static final String TAG = LocationJobService.class.getSimpleName();
    @Override
    public boolean onStartJob(JobParameters params) {
        Commonlib.serviceCheckAndStart(this, true);
        return false;
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        return false;
    }
}
