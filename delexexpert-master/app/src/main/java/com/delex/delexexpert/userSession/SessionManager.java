package com.delex.delexexpert.userSession;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;

import com.delex.delexexpert.commonLib.TinyDB;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;

public class SessionManager {

    public static final String TAG = SessionManager.class.getSimpleName();
    private TinyDB mTinyDB;
    private String result = "";
    public final String DATA = "data";

    public SessionManager(Context context) {

        mTinyDB = new TinyDB(context);

        SessionManagerPojo sessionManagerPojo = mTinyDB.getObject(DATA, SessionManagerPojo.class);
        if (sessionManagerPojo == null) {
            mTinyDB.putObject(DATA, new SessionManagerPojo());
        }
    }

    /**
     * 드라이버 운행 할 vehicle
     */
    public void setPubOrderNumberData(ArrayList<String> initData) {
        SessionManagerPojo sessionManagerPojo = mTinyDB.getObject(DATA, SessionManagerPojo.class);
        sessionManagerPojo.setPubOrderNumberData(initData);
        mTinyDB.putObject(DATA, sessionManagerPojo);
    }

    public ArrayList<String> getPubOrderNumberData() {
        return mTinyDB.getObject(DATA, SessionManagerPojo.class).getPubOrderNumberList();
    }


    /**
     * 저장될 데이터 넣기
     *
     * @param key
     * @param value
     */
    public void setStringData(@NonNull String key, @NonNull String value) {
        Log.d(TAG, "setStringData: " + key + " / " + value);

        SessionManagerPojo sessionManagerPojo = mTinyDB.getObject(DATA, SessionManagerPojo.class);

        Class userInfoPojoClass = sessionManagerPojo.getClass();
        Log.d(TAG, "setStringData: set" + key);

        try {
            try {
                userInfoPojoClass.getMethod("set" + key, String.class).invoke(sessionManagerPojo, value);

                Log.d(TAG, "setStringData: " + userInfoPojoClass.getMethods().length);
                mTinyDB.putObject(DATA, sessionManagerPojo);  //저장된 데이터 넣기

            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            }
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
    }

    /**
     * 값이 없을경우 빈값이 리턴됨
     *
     * @param key
     * @return
     */
    public String getStringData(@NonNull String key) {

        SessionManagerPojo sessionManagerPojo = mTinyDB.getObject(DATA, SessionManagerPojo.class);

        Log.d(TAG, "setStringData: " + key);
        Log.d(TAG, "getStringData: " + sessionManagerPojo.toString());
        Class userInfoPojoClass = sessionManagerPojo.getClass();

        try {

            String resultString = String.valueOf(userInfoPojoClass.getMethod("get" + key).invoke(sessionManagerPojo));

            if (!resultString.equals("null")) {
                result = resultString;
            }

            Log.d(TAG, "getStringData: " + result);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }

        return result;

    }
}
