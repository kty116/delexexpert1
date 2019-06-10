package com.delex.delexexpert.userSession;


import com.delex.delexexpert.commonLib.TinyDB;

import java.util.ArrayList;

/**
 * TinyDB에 저장되있는 쉐어드프리퍼런스들의 고유 키값
 */
public class SessionManagerPojo {

    private TinyDB tinyDB;

    public static final String LOGIN_ID = "LoginId";
    public static final String LOGIN_PASSWORD = "LoginPassword";
    public static final String IS_LOGIN = "IsLogin";
    public static final String LANGUAGE = "Language";
    public static final String PUSH_TOKEN = "PushToken";
    public static final String PHONE_NUBER = "PhoneNumber";
    public static final String ACCESS_TOKEN = "AccessToken";
    public static final String REFRESH_TOKEN = "RefreshToken";
    public static final String CURRENT_LAT = "CurrentLat";
    public static final String CURRENT_LON = "CurrentLon";
    public static final String USER_TYPE = "UserType";
    public static final String USER_NAME = "UserName";
    public static final String USER_IMAGE_URL = "UserImageUrl";
    public static final String PAYMENT_TYPE = "PaymentType";
    public static final String USER_INDEX = "UserIndex";
    public static final String DEFAULT_VEHICLE_TYPE_ID = "DefaultVehicleTypeId";
    public static final String DEFAULT_VEHICLE_NAME = "DefaultVehicleName";


    private ArrayList<String> driverOrderList;

    private String userIndex = "";
    private String userId;
    private String userPassword;
    private String isLogin;
    private String language = "en";
    private String pushToken;
    private String phoneNumber = "";
    private String accessToken;
    private String refreshToken;
    private String currentLat;
    private String currentLon;
    private String userType;

    private String userName = "";
    private String userImageUrl;

    private String defaultVehicleTypeId;
    private String defaultVehicleName;

    private String loginId;
    private String loginPassword;


    //================================================driver======================================================

    public static final String DRIVER_ON_AND_OFF_STATE = "DriverOnAndOffState";
    public static final String DRIVER_IS_SHIPPING = "DriverIsShipping";

    private String driverOnAndOffState;
    private String driverIsShipping = "false";


//    private ArrayList<RecentAddressModel> recentAddressList = new ArrayList<>();
//    private ArrayList<FavoriteAddressModel> favoriteAddressList = new ArrayList<>();
//    private ArrayList<ShippingGoodsUnitModel> shippingGoodsList = new ArrayList<>();

    //    private ArrayList<>


    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    public String getLoginId() {
        return loginId;
    }

    public void setLoginId(String loginId) {
        this.loginId = loginId;
    }

    public String getLoginPassword() {
        return loginPassword;
    }

    public void setLoginPassword(String loginPassword) {
        this.loginPassword = loginPassword;
    }

    public String getDriverIsShipping() {
        return driverIsShipping;
    }

    public void setDriverIsShipping(String driverIsShipping) {
        this.driverIsShipping = driverIsShipping;
    }

    public String getDriverOnAndOffState() {
        return driverOnAndOffState;
    }

    public void setDriverOnAndOffState(String driverOnAndOffState) {
        this.driverOnAndOffState = driverOnAndOffState;
    }

    public ArrayList<String> getPubOrderNumberList() {
        return driverOrderList;
    }

    public void setPubOrderNumberData(ArrayList<String> driverOrderList) {
        this.driverOrderList = driverOrderList;
    }

    public String getDefaultVehicleTypeId() {
        return defaultVehicleTypeId;
    }

    public void setDefaultVehicleTypeId(String defaultVehicleTypeId) {
        this.defaultVehicleTypeId = defaultVehicleTypeId;
    }

    public String getDefaultVehicleName() {
        return defaultVehicleName;
    }

    public void setDefaultVehicleName(String defaultVehicleName) {
        this.defaultVehicleName = defaultVehicleName;
    }

    public String getUserIndex() {
        return userIndex;
    }

    public void setUserIndex(String userIndex) {
        this.userIndex = userIndex;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserImageUrl() {
        return userImageUrl;
    }

    public void setUserImageUrl(String userImageUrl) {
        this.userImageUrl = userImageUrl;
    }

    public String getUserType() {
        return userType;
    }

    public void setUserType(String userType) {
        this.userType = userType;
    }

    public String getCurrentLat() {
        return currentLat;
    }

    public void setCurrentLat(String currentLat) {
        this.currentLat = currentLat;
    }

    public String getCurrentLon() {
        return currentLon;
    }

    public void setCurrentLon(String currentLon) {
        this.currentLon = currentLon;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUserPassword() {
        return userPassword;
    }

    public void setUserPassword(String userPassword) {
        this.userPassword = userPassword;
    }

    public String getIsLogin() {
        return isLogin;
    }

    public void setIsLogin(String isLogin) {
        this.isLogin = isLogin;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public String getPushToken() {
        return pushToken;
    }

    public void setPushToken(String pushToken) {
        this.pushToken = pushToken;
    }

}
