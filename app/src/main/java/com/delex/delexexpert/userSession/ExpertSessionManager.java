package com.delex.delexexpert.userSession;

import android.content.Context;

import com.delex.delexexpert.commonLib.TinyDB;


public class ExpertSessionManager {
    private TinyDB tinyDB;
    private String userId;
    private String carNum;
    private String password;
    private boolean isLogin;
    private String loginSession;
    private double lat;
    private double lon;

    public double getLat() {
        return tinyDB.getDouble("lat", 0.0);
    }

    public void setLat(double lat) {
        tinyDB.putDouble("lat", lat);
    }

    public double getLon() {
        return tinyDB.getDouble("lon", 0.0);
    }

    public void setLon(double lon) {
        tinyDB.putDouble("lon", lon);
    }

    public ExpertSessionManager(Context context) {
        TinyDB tinyDB = new TinyDB(context);
        this.tinyDB = tinyDB;
    }

    public String getCarNum() {
        return tinyDB.getString("car_num");
    }

    public void setCarNum(String carNum) {
        tinyDB.putString("car_num", carNum);
    }

    public boolean isLogin() {
        return tinyDB.getBoolean("is_login");
    }

    public void setLogin(boolean login) {
        tinyDB.putBoolean("is_login", login);
    }

    public String getUserId() {

        return tinyDB.getString("user_id");
    }

    public void setUserId(String userId) {
        tinyDB.putString("user_id", userId);
    }

    public String getPassword() {
        return tinyDB.getString("password");
    }

    public void setPassword(String password) {
        tinyDB.putString("password", password);
    }

    public String getLoginSession() {
        return tinyDB.getString("login_session");
    }

    public void setLoginSession(String loginSession) {
        tinyDB.putString("login_session", loginSession);
    }
}
