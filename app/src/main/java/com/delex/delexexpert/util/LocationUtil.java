package com.delex.delexexpert.util;

import android.Manifest;
import android.app.Activity;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.util.Log;

import com.delex.delexexpert.broadcastReceiver.LocationReceiver;
import com.delex.delexexpert.event.GpsOnOffExpertEvent;
import com.delex.delexexpert.event.LastLocationExpertEvent;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;

/**
 * <h>LocationUtil</h>
 * <p>
 * 업데이트된 위치 제공 위치 서비스 Utill
 * </P>
 */
public class LocationUtil {
    public static final int REQUEST_CHECK_SETTINGS = 1123;
    public static final long TEN_UPDATE_INTERVAL_IN_MILLISECONDS = 10 * 1000;
    public static final long FIVE_UPDATE_INTERVAL_IN_MILLISECONDS = 5 * 1000;
    public static final long TWO_UPDATE_INTERVAL_IN_MILLISECONDS = 2 * 1000;

    public static final long OFF_INTERVAL_IN_MILLISECONDS = 10 * 60 * 1000;
    private final Context mContext;
    private FusedLocationProviderClient mFusedLocationClient;
    private PendingIntent mPendingIntent;
    protected LocationSettingsRequest mLocationSettingsRequest;

    protected Boolean mRequestingLocationUpdates = false;
    private Task<LocationSettingsResponse> mTask;
    private LocationSettingsRequest.Builder mBuilder;

    private KalmanLatLong mKalmanFilter;
    private float currentSpeed = 0.0f; // meters/second
    private long runStartTimeInMillis;

    public static final String TAG = LocationUtil.class.getSimpleName();
    private ArrayList<LocationRequest> mLocationRequests;
    public LocationRequest mHighLocationRequest;
    //    public LocationRequest mNoPowerLocationRequest;
    public LocationRequest mOffWorkLocationRequest;


    public LocationUtil(Context context) {
        mContext = context;
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(mContext);
        createLocationRequest();
        buildLocationSettingsRequest();
        mPendingIntent = getUpdateLocationPendingIntent();
        mKalmanFilter = new KalmanLatLong(3);
    }


    private PendingIntent getUpdateLocationPendingIntent() {

        Intent locationIntent = new Intent(mContext, LocationReceiver.class);
        PendingIntent pending = PendingIntent.getBroadcast(mContext, 10, locationIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        return pending;
    }

    protected void createLocationRequest() {

        //3초 안에 업데이트 되면 nopower로 변경 & 7초 안에 업데이트 안되고 배터리 상태 체크 후 balance or high로 변경

        mLocationRequests = new ArrayList<>();

        mOffWorkLocationRequest = LocationRequest.create();
        mOffWorkLocationRequest.setInterval(OFF_INTERVAL_IN_MILLISECONDS);
        mOffWorkLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        mHighLocationRequest = LocationRequest.create();
        mHighLocationRequest.setSmallestDisplacement(10);
        mHighLocationRequest.setFastestInterval(FIVE_UPDATE_INTERVAL_IN_MILLISECONDS);
        mHighLocationRequest.setInterval(TEN_UPDATE_INTERVAL_IN_MILLISECONDS);
        mHighLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        mLocationRequests.add(mHighLocationRequest);
        mLocationRequests.add(mOffWorkLocationRequest);

//        mNoPowerLocationRequest = LocationRequest.create();
//        mNoPowerLocationRequest.setInterval(TEN_UPDATE_INTERVAL_IN_MILLISECONDS);
//        mNoPowerLocationRequest.setFastestInterval(FIVE_UPDATE_INTERVAL_IN_MILLISECONDS);
//        mNoPowerLocationRequest.setPriority(LocationRequest.PRIORITY_NO_POWER);

//        mBalanceLocationRequest = LocationRequest.create();
//        mBalanceLocationRequest.setInterval(FIVE_UPDATE_INTERVAL_IN_MILLISECONDS);
//        mBalanceLocationRequest.setFastestInterval(FIVE_UPDATE_INTERVAL_IN_MILLISECONDS);
//        mBalanceLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);



        //        mHighLocationRequest.setSmallestDisplacement(10);
//        mHighLocationRequest.setMaxWaitTime(UPDATE_INTERVAL_IN_MILLISECONDS * 2);

    }

    /**
     * <h2>buildLocationSettingsRequest</h2>
     * <p>
     * 장치에 필요한 위치 설정이 있는 경우 위치서비스가 켜진후애
     * if a device has the needed location settings when location service is off.
     * </P>
     */
    protected void buildLocationSettingsRequest() {
        mBuilder = new LocationSettingsRequest.Builder();
        mBuilder.addAllLocationRequests(mLocationRequests);
        mBuilder.setAlwaysShow(true);
        mLocationSettingsRequest = mBuilder.build();
    }

    /**
     * <h2>checkLocationSettings</h2>
     * <p>
     * 기기의 위치 설정이 앱의 요구에 적합한 지 확인합니다.
     * Check if the device's location settings are adequate for the app's needs.
     * </P>
     */
    public void checkLocationSettings(final Activity activity) {
        SettingsClient client = LocationServices.getSettingsClient(mContext);
        mTask = client.checkLocationSettings(mBuilder.build());

        mTask.addOnSuccessListener(activity, new OnSuccessListener<LocationSettingsResponse>() {
            @Override
            public void onSuccess(LocationSettingsResponse locationSettingsResponse) {
//                startLocationUpdates(mHighLocationRequest);
                EventBus.getDefault().post(new GpsOnOffExpertEvent(true));

            }
        });

        mTask.addOnFailureListener(activity, new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                if (e instanceof ResolvableApiException) {
//                    EventBus.getDefault().post(new GpsOnOffExpertEvent(false));
                    ResolvableApiException resolvable = (ResolvableApiException) e;
                    try {
                        resolvable.startResolutionForResult(activity,
                                REQUEST_CHECK_SETTINGS);
                    } catch (IntentSender.SendIntentException e1) {
                        e1.printStackTrace();
                    }
                }
            }
        });

    }

    /**
     * 서버에서 출석 요청했을때 혹은 처음 출근 버튼 눌렀을때의 출석 말하기
     */
    public void requestLastLocation() {
        if (mFusedLocationClient != null) {
            if (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            mFusedLocationClient.getLastLocation().addOnSuccessListener(new OnSuccessListener<Location>() {
                @Override
                public void onSuccess(Location location) {
                    if (location != null) {
                        EventBus.getDefault().post(new LastLocationExpertEvent(location));
                    }
                }
            });
        }
    }

    /**
     * <h2>startLocationUpdates</h2>
     * <p>
     * Requests location updates from the FusedLocationApi.
     * </p>
     */
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
    public void startLocationUpdates(LocationRequest locationRequest) {

        if (!mRequestingLocationUpdates) {
            runStartTimeInMillis = (long) (SystemClock.elapsedRealtimeNanos() / 1000000);
            if (mFusedLocationClient != null) {
                if (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    return;
                }

                mFusedLocationClient.requestLocationUpdates(locationRequest, mPendingIntent);
                mRequestingLocationUpdates = true;
            }
        }
    }

    /**
     * <h2>stopLocationUpdate</h2>
     * <p>
     * Removes location updates from the FusedLocationApi.
     * </P>
     */
    public void stopLocationUpdate() {
        if (mRequestingLocationUpdates) {
            if (mFusedLocationClient != null) {
                mFusedLocationClient.removeLocationUpdates(mPendingIntent);
            }
            mRequestingLocationUpdates = false;
        }

    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
    public boolean filterAndAddLocation(Location location, float accuracy, int radiusPassed) {

        long age = getLocationAge(location);

        if (age > 10 * 1000) { //more than 5 seconds
            Log.d(TAG, "Location is old");
            return false;
        }

        if (location.getAccuracy() <= 0) {
            Log.d(TAG, "Latitidue and longitude values are invalid.");
            return false;
        }

        //setAccuracy(newLocation.getAccuracy());
        float horizontalAccuracy = location.getAccuracy();
        if (horizontalAccuracy > accuracy) { //10meter filter
            Log.d(TAG, "Accuracy is too low.");
            return false;
        }


        /* Kalman Filter */
        float Qvalue;

        long locationTimeInMillis = (long) (location.getElapsedRealtimeNanos() / 1000000);
        long elapsedTimeInMillis = locationTimeInMillis - runStartTimeInMillis;

        if (currentSpeed == 0.0f) {
            Qvalue = 3.0f; //3 meters per second
        } else {
            Qvalue = currentSpeed; // meters per second
        }

        mKalmanFilter.Process(location.getLatitude(), location.getLongitude(), location.getAccuracy(), elapsedTimeInMillis, Qvalue);
        double predictedLat = mKalmanFilter.get_lat();
        double predictedLng = mKalmanFilter.get_lng();

        Location predictedLocation = new Location("");//provider name is unecessary
        predictedLocation.setLatitude(predictedLat);//your coords of course
        predictedLocation.setLongitude(predictedLng);
        float predictedDeltaInMeters = predictedLocation.distanceTo(location);

        if (predictedDeltaInMeters > radiusPassed) {
            Log.d(TAG, "Kalman Filter detects mal GPS, we should probably remove this from track");
            mKalmanFilter.consecutiveRejectCount += 1;

            if (mKalmanFilter.consecutiveRejectCount > 3) {
                mKalmanFilter = new KalmanLatLong(3); //reset Kalman Filter if it rejects more than 3 times in raw.
            }

            return false;
        } else {
            mKalmanFilter.consecutiveRejectCount = 0;
        }

        Log.d(TAG, "Location quality is good enough.");
        currentSpeed = location.getSpeed();

        return true;
    }

    private long getLocationAge(Location newLocation) {
        long locationAge;
        if (Build.VERSION.SDK_INT >= 17) {
            long currentTimeInMilli = (long) (SystemClock.elapsedRealtimeNanos() / 1000000);
            long locationTimeInMilli = (long) (newLocation.getElapsedRealtimeNanos() / 1000000);
            locationAge = currentTimeInMilli - locationTimeInMilli;
        } else {
            locationAge = System.currentTimeMillis() - newLocation.getTime();
        }
        return locationAge;
    }
}
