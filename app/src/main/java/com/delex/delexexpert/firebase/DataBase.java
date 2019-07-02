package com.delex.delexexpert.firebase;

import android.content.Context;
import android.location.Location;
import android.os.Build;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Toast;

import com.delex.delexexpert.userSession.ExpertSessionManager;
import com.delex.delexexpert.util.Commonlib;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;

import java.io.BufferedOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.UUID;

public class DataBase {
    public static final String TAG = DataBase.class.getSimpleName();
    private String mAndroidVersion;
    private String mUserId;
    private String mModelName;
    private int mApiVersion;
    //    private String mPhoneNumber;
    private String mUUID;
    private String mAppVersion;
    private String mCarNumber;

    private FirebaseDatabase database;
    private DatabaseReference myRef;
    private Gson mGson;
    private Context mContext;
    private StateModel mStateModels;
    private boolean isFirstLoad = true;
    private ExpertSessionManager mExpertSessionManager;
    private SimpleDateFormat dayTime;

    public DataBase(Context context) {
        mGson = new Gson();
        mContext = context;
        database = FirebaseDatabase.getInstance();
        myRef = database.getReference("tbl_driver");
        mExpertSessionManager = new ExpertSessionManager(context);
        mCarNumber = mExpertSessionManager.getCarNum();
        mUserId = mExpertSessionManager.getUserId();
        mAppVersion = Commonlib.getVersionValue(context);
        mAndroidVersion = Build.VERSION.RELEASE;
        mModelName = Build.MODEL;
        mApiVersion = Build.VERSION.SDK_INT;

        dayTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        mUUID = mExpertSessionManager.getUuid();

        if (mUUID.isEmpty()) {
            String uuid = UUID.randomUUID().toString();
            mExpertSessionManager.setUuid(uuid);
            mUUID = uuid;
        }

//        mPhoneNumber = Commonlib.getPhoneNumber(context);
    }

//    public void writeContent(ChatContentModel chatContentModel) {
//        myRef.child("채팅방").child("content").push().setValue(chatContentModel);
//    }

    public void writeStateData(boolean isWork, boolean isMqttConnection, boolean serviceState, Location location, String error, String text) {
        String currentTime = dayTime.format(new Date(System.currentTimeMillis()));

        double lat = 0;
        double lon = 0;

        if (location != null) {
            lat = location.getLatitude();
            lon = location.getLongitude();
        }

        if (!mCarNumber.isEmpty()) {  //mCarNumber 빈값이 아닐때만 전송
            if (error.isEmpty()) {
                mStateModels = new StateModel(mModelName, mApiVersion, mAndroidVersion, mCarNumber, mUserId, currentTime, mAppVersion, lat, lon, Commonlib.locationOnOffCheck(mContext), isWork, isMqttConnection, serviceState, error, "", text);
            } else {
                mStateModels = new StateModel(mModelName, mApiVersion, mAndroidVersion, mCarNumber, mUserId, currentTime, mAppVersion, lat, lon, Commonlib.locationOnOffCheck(mContext), isWork, isMqttConnection, serviceState, error, currentTime, text);
            }
            myRef.child(mCarNumber).setValue(mStateModels);

        } else {
            if (error.isEmpty()) {
                mStateModels = new StateModel(mModelName, mApiVersion, mAndroidVersion, mUUID, "", currentTime, mAppVersion, lat, lon, Commonlib.locationOnOffCheck(mContext), isWork, isMqttConnection, serviceState, error, "", text);
            } else {
                mStateModels = new StateModel(mModelName, mApiVersion, mAndroidVersion, mUUID, "", currentTime, mAppVersion, lat, lon, Commonlib.locationOnOffCheck(mContext), isWork, isMqttConnection, serviceState, error, currentTime, text);
            }
            myRef.child(mUUID).setValue(mStateModels);
        }
    }


}
