package com.delex.delexexpert.application;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatDialog;
import android.util.Log;

public class BaseApplication extends Application {

    private AppStatus mAppStatus = AppStatus.FOREGROUND;
    //private MqttInterface mqttInterface;
    private boolean runningDriverMainActivity = false;

    public static boolean DEBUG = false;  // Dlog 셋팅

    ////////카카오톡 sdk 설정 변수////////////
    private static volatile BaseApplication obj = null;
    //    private static volatile Activity currentActivity = null;
    ////////////////////////////////
    private static BaseApplication baseApplication;
    AppCompatDialog progressDialog;
    public static final String TAG = BaseApplication.class.getSimpleName();

    public static BaseApplication getInstance() {
        return baseApplication;
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
//        MultiDex.install(this);
    }

    @Override
    public void onCreate() {
        super.onCreate();
//        Stetho.initializeWithDefaults(this);

        obj = this;
        registerActivityLifecycleCallbacks(new MyActivityLifecycleCallbacks());
        Log.d(TAG, "onCreate: why");
    }

    public static BaseApplication getGlobalApplicationContext() {
        return obj;
    }

    // Get app is foreground
    public AppStatus getAppStatus() {
        return mAppStatus;
    }

    // check if app is return foreground
    public boolean isReturnedForeground() {
        return mAppStatus.ordinal() == AppStatus.RETURNED_TO_FOREGROUND.ordinal();
    }

    public boolean runningDriverMainActivity() {
        return runningDriverMainActivity;
    }

    public enum AppStatus {
        BACKGROUND, // app is background
        RETURNED_TO_FOREGROUND, // app returned to foreground(or first launch)
        FOREGROUND; // app is foreground
    }

    public class MyActivityLifecycleCallbacks implements ActivityLifecycleCallbacks {

        // running activity count
        private int running = 0;

        @Override
        public void onActivityCreated(Activity activity, Bundle bundle) {

        }

        @Override
        public void onActivityStarted(Activity activity) {

            if (++running == 1) {
// running activity is 1,
// app must be returned from background just now (or first launch)
                mAppStatus = AppStatus.RETURNED_TO_FOREGROUND;
            } else if (running > 1) {
// 2 or more running activities,
// should be foreground already.
                mAppStatus = AppStatus.FOREGROUND;
            }
        }

        @Override
        public void onActivityResumed(Activity activity) {
        }

        @Override
        public void onActivityPaused(Activity activity) {
        }

        @Override
        public void onActivityStopped(Activity activity) {

            if (--running == 0) {
// no active activity
// app goes to background
                mAppStatus = AppStatus.BACKGROUND;
            }
        }

        @Override
        public void onActivitySaveInstanceState(Activity activity, Bundle bundle) {
        }

        @Override
        public void onActivityDestroyed(Activity activity) {
        }
    }

}
