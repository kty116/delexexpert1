package com.delex.delexexpert.util;

import android.Manifest;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.Dialog;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.graphics.drawable.VectorDrawableCompat;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.telephony.TelephonyManager;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.Toast;

import com.delex.delexexpert.activity.MainActivity;
import com.delex.delexexpert.application.BaseApplication;
import com.delex.delexexpert.firebase.DataBase;
import com.delex.delexexpert.retrofit.RetrofitLib;
import com.delex.delexexpert.retrofit.RetrofitUtil;
import com.delex.delexexpert.service.LocationService;
import com.delex.delexexpert.userSession.ExpertSessionManager;
import com.gun0912.tedpermission.PermissionListener;
import com.gun0912.tedpermission.TedPermission;

import org.greenrobot.eventbus.EventBus;

import java.io.BufferedInputStream;
import java.net.URL;
import java.net.URLConnection;
import java.security.MessageDigest;
import java.text.DecimalFormat;
import java.util.List;

import retrofit2.Call;


public class Commonlib {

    public static final String SHIPPING_DATA = "shipping_data";
    public static final String RECENT_ADDRESS_DATA = "recent_address_data";
    public static final String FAVORITE_ADDRESS_DATA = "favorite_address_data";


    public static String TOOLBAR_TITLE = "toolbar_title";
    public static String RESULT_ADDRESS = "result_address";

    public static final int GET_START_ADDRESS = 1000;
    public static final int GET_STOP_ADDRESS = 1001;
    public static final int FAVORITE_DIALOG = 1002;
    public static final int MIC_BUTTON_CLICK = 1003;


    public static final String TAG = Commonlib.class.getSimpleName();
    private static Dialog progressDialog;
    private static Dialog AletDialog;


    public static void focusOutView(Activity activity) {
        if (activity.getCurrentFocus() != null) {
            activity.getCurrentFocus().clearFocus();

            InputMethodManager inputMethodManager = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
            inputMethodManager.hideSoftInputFromWindow(new View(activity).getWindowToken(), 0);
        }
    }

    /**
     * url을 bitmap으로 변환
     *
     * @param strImageURL
     * @return
     */
    public static Bitmap getImageFromURL(String strImageURL) {
        Bitmap imgBitmap = null;

        try {
            URL url = new URL(strImageURL);
            URLConnection conn = url.openConnection();
            conn.connect();

            int nSize = conn.getContentLength();
            BufferedInputStream bis = new BufferedInputStream(conn.getInputStream(), nSize);
            imgBitmap = BitmapFactory.decodeStream(bis);

            bis.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return imgBitmap;
    }

    /**
     * 서버로 푸시 토큰 보냄
     *
     * @param userId
     * @param pushToken
     */
    public static void sendPushToken(String userId, String pushToken) {
        Log.d(TAG, "sendPushToken: ");

        if (pushToken != null) {

            Call<Object> call1 = RetrofitLib.getRetrofit().getDeviceToken(userId, pushToken);

            RetrofitUtil.retrofitService(call1, new RetrofitUtil.RetrofitResponseImpl<Object>() {
                @Override
                public void successful(Object result) {
                    Log.d(TAG, "successful: " + result);
                }

                @Override
                public void failure(String t) {
                    Log.d(TAG, "failure: " + t);
                }
            });
        }
    }

    /**
     * GPS 체크
     *
     * @return
     */
    public static boolean locationOnOffCheck(Context context) {
        boolean gpsEnable = false;
        LocationManager manager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        if (manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            gpsEnable = true;
        }

        return gpsEnable;
    }

    /**
     * 실행중인 서비스 체크
     *
     * @return
     */
    public static boolean isServiceRunning(Context context) {
        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);

        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (LocationService.class.getName().equals(service.service.getClassName()))
                return true;
        }
        return false;
    }


    public static void serviceCheckAndStart(Context context, boolean isAction) {

        if (isAction) {
            DataBase dataBase = new DataBase(context);
            ExpertSessionManager expertSessionManager = new ExpertSessionManager(context);

            if (!isServiceRunning(context)) {  //서비스 실행중 아님

                String userId = expertSessionManager.getUserId();
                String carNum = expertSessionManager.getCarNum();

                if (userId != null && !userId.isEmpty() && carNum != null && !carNum.isEmpty()) {

                    startService(context, expertSessionManager);
                    dataBase.writeStateData(false, false, false, null, "", "유저 정보 있음");

                } else {
                    dataBase.writeStateData(false, false, false, null, "", "유저 정보 없음 로그아웃 상태");
                    openApp(context);
                    Toast.makeText(context, "로그인을 해주세요!", Toast.LENGTH_LONG).show();
                }
            }
        }
    }

//    public static String getPhoneNumber(Context context) {
//        TelephonyManager telManager = (TelephonyManager) context.getSystemService(context.TELEPHONY_SERVICE);
//        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.READ_SMS) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(context, Manifest.permission.READ_PHONE_NUMBERS) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(context, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
//        }
//        String phoneNum = telManager.getLine1Number();
//        if (phoneNum.startsWith("+82")) {
//            phoneNum = phoneNum.replace("+82", "0");
//        }
//        return phoneNum;
//    }

    public static void openApp(Context context) {
        Intent intent = new Intent(context, MainActivity.class);
        context.startActivity(intent);
//        PackageManager manager = context.getPackageManager();
//        try {
//            Intent i = manager.getLaunchIntentForPackage(packageName);
//            if (i == null) {
//                throw new PackageManager.NameNotFoundException();
//            }
//            i.addCategory(Intent.CATEGORY_LAUNCHER);
//            context.startActivity(i);
//            return true;
//        } catch (PackageManager.NameNotFoundException e) {
//            e.printStackTrace();
//            return false;
//        } catch (Exception e) {
//            e.printStackTrace();
//            return false;
//        }
    }

    public static boolean isAppRunning(Context context) {
        ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> procInfos = activityManager.getRunningAppProcesses();
        for (int i = 0; i < procInfos.size(); i++) {
            if (procInfos.get(i).processName.equals(context.getPackageName())) {
                return true;
            }
        }
        return false;
    }

    public static void startService(Context context, ExpertSessionManager expertSessionManager) {
        Intent intent = new Intent(context, LocationService.class);
        String locationSetting = expertSessionManager.getLocationSetting();

        if (locationSetting != null && !locationSetting.isEmpty()) {
            if (locationSetting.equals("출근")) {
                intent.setAction(LocationService.ACTION_START_DATA);
            } else {
                intent.setAction(LocationService.ACTION_STOP_DATA);
            }

        } else {
            expertSessionManager.setLocationSetting("출근");
            intent.setAction(LocationService.ACTION_START_DATA);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {  //API 26버전 이상
            context.startForegroundService(intent);
        } else {
            context.startService(intent);
        }
    }


    /**
     * <h2>validateTime</h2>
     * <p>
     * method to validate that whether the selected time
     * is greater than the current time or not
     * </p>
     *
     * @param current:  device current time
     * @param selected: selected time
     * @return boolean: true is selected time is greater than the current time
     */
    public static boolean validateTime(long current, long selected) {
        Log.d(TAG, "validateTime: " + "current:" + current + "selected:" + selected);
        return selected > current;
    }

//    public static void focusOutView(Activity activity) {
//        if (activity.getCurrentFocus() == null) {
//            activity.getCurrentFocus().clearFocus();
//        }
//        InputMethodManager inputMethodManager = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
//        inputMethodManager.hideSoftInputFromWindow(activity.getCurrentFocus().getWindowToken(), 0);
//    }

    public static void editTextFocusOnView(Activity activity, EditText editText) {
        editText.setFocusableInTouchMode(true);
        editText.requestFocus();

        InputMethodManager inputMethodManager = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
        inputMethodManager.showSoftInput(editText, 0);
    }

    /**
     * <h2>isNetworkAvailable</h2>
     * <p>
     * This method is used for checking internet connection
     * </P>
     *
     * @param context current context.
     * @return boolean value.
     */
    public static boolean isNetworkAvailable(Context context) {

        ConnectivityManager connectivity;
        boolean isNetworkAvail = false;
        try {
            connectivity = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            if (connectivity != null) {
                NetworkInfo[] info = connectivity.getAllNetworkInfo();
                if (info != null) {
                    for (NetworkInfo anInfo : info)
                        if (anInfo.getState() == NetworkInfo.State.CONNECTED) {
                            return true;
                        }
                }
            }
            return false;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return isNetworkAvail;
    }


    public static void gpsServiceCheck(Context context) {
        LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);

        //GPS가 켜져있는지 체크
        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            //GPS 설정화면으로 이동
            Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            intent.addCategory(Intent.CATEGORY_DEFAULT);
            context.startActivity(intent);
        }
    }


    /**
     * 퍼미션 체크 메소드
     *
     * @param permissionCheckList     //     * @param descriptionMessage      "핸드폰번호와 인증번호를 자동으로 가져오려면 이 권한이 필요합니다."
     *                                //     * @param deniedMessage           "해당 권한을 거부하면 이 서비스를 이용할 수 없습니다.\n- 권한 승인 변경 방법\n[설정] > [애플리케이션] > [담너머] \n> [권한] > 모두 허용"
     * @param permissionCheckResponse
     */

    public static void permissionCheck(final Context context,
                                       final String[] permissionCheckList, boolean isDeniedMessage,
                                       final PermissionCheckResponseImpl permissionCheckResponse) {
        boolean isPermission = true;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) { //마시멜로우 이상인지 체크


            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) { //마시멜로우 이상인지 체크

                int[] permissionChecks = new int[permissionCheckList.length];

                for (int i = 0; i < permissionChecks.length; i++) {
                    permissionChecks[i] = ContextCompat.checkSelfPermission(context, permissionCheckList[i]);
                    if (permissionChecks[i] == PackageManager.PERMISSION_DENIED) {
                        isPermission = false;
                    }
                }

                if (!isPermission) {

                    PermissionListener permissionlistener = new PermissionListener() {
                        @Override
                        public void onPermissionGranted() {
                            permissionCheckResponse.granted();
                        }

                        @Override
                        public void onPermissionDenied(List<String> deniedPermissions) {
                            permissionCheckResponse.denied();
                        }
                    };

                    if (isDeniedMessage) {

                        TedPermission.with(context)
                                .setPermissionListener(permissionlistener)
//                            .setRationaleMessage(descriptionMessage)
                                .setDeniedMessage("If you reject permission,you can not use this service\n\nPlease turn on permissions at [Setting] > [Permission]")
                                .setPermissions(permissionCheckList)
                                .check();
                    } else {
                        TedPermission.with(context)
                                .setPermissionListener(permissionlistener)
//                            .setRationaleMessage(descriptionMessage)
                                .setPermissions(permissionCheckList)
                                .check();
                    }
                } else {
                    permissionCheckResponse.granted();
                }
            }

        } else {
            permissionCheckResponse.granted();
        }
    }

    public static String getHashKey(Context context) {
        final String TAG = "KeyHash";
        String keyHash = null;
        try {
            PackageInfo info = context.getPackageManager().getPackageInfo(context.getPackageName(), PackageManager.GET_SIGNATURES);
            for (Signature signature : info.signatures) {
                MessageDigest md;
                md = MessageDigest.getInstance("SHA");
                md.update(signature.toByteArray());
                keyHash = new String(Base64.encode(md.digest(), 0));
                Log.d(TAG, keyHash);
            }
        } catch (Exception e) {
            Log.e("name not found", e.toString());
        }

        if (keyHash != null) {
            return keyHash;
        } else {
            return null;
        }
    }

    public interface PermissionCheckResponseImpl {
        void granted();

        void denied();
    }

    //region Helper method for PreLollipop TextView & Buttons Vector Images
    public static Drawable setVectorForPreLollipop(int resourceId, Context activity) {
        Drawable icon;
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            icon = VectorDrawableCompat.create(activity.getResources(), resourceId, activity.getTheme());
        } else {
            icon = activity.getResources().getDrawable(resourceId, activity.getTheme());
        }

        return icon;
    }

    /**
     * 버전코드 가져오기
     *
     * @param context
     * @return
     */
    public static String getVersionValue(Context context) {
        PackageInfo pi = null;
        try {
            pi = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

//        int versionCode = pi.versionCode;
        String versionName = pi.versionName;

        return versionName;
    }

    /**
     * URI를 Filepath로 변환
     */
    public static String uriToFilePath(Context context, Uri uri) {
        Cursor cursor = context.getContentResolver().query(uri, null, null, null, null);
        cursor.moveToNext();
        String path = cursor.getString(cursor.getColumnIndex("_data"));
        cursor.close();
        return path;
    }


    public static String getPathFromUri(Context context, Uri uri) {

        Cursor cursor = context.getContentResolver().query(uri, null, null, null, null);

        cursor.moveToNext();

        String path = cursor.getString(cursor.getColumnIndex("_data"));

        cursor.close();
        return path;

    }

    public static Uri getUriFromPath(Context context, String path) {

        String fileName = "file://" + path;

        Uri fileUri = Uri.parse(fileName);

        String filePath = fileUri.getPath();

        Cursor cursor = context.getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,

                null, "_data = '" + filePath + "'", null, null);

        cursor.moveToNext();

        int id = cursor.getInt(cursor.getColumnIndex("_id"));

        Uri uri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id);


        return uri;

    }

    public static String formattedStringPrice(long price) {
        DecimalFormat myFormatter = new DecimalFormat("###,###,###");
        String formattedStringPrice = myFormatter.format(price);

        return formattedStringPrice;
    }

    public static String getTime(int second) {
        int hour = second / 3600;
        int min = (second % 3600 / 60);
        if (hour == 0) {
            return min + "분";
        } else {
            return hour + "시간 " + min + "분";
        }


    }

    public static String getKm(double m) {
        double km = (m / 1000);
        return String.format("%.1f", km) + "km";

    }

    public static Fragment getNowFragment(FragmentManager fragmentManager) {
        Fragment rtnfragment = null;
        for (Fragment fragment : fragmentManager.getFragments()) {
            if (fragment.isVisible()) {
                rtnfragment = fragment;
            }
            break;
        }
        return rtnfragment;
    }

    public static void quitFragment(FragmentActivity activity, int stack) {
        List<Fragment> fragments = activity.getSupportFragmentManager().getFragments();
        activity.getSupportFragmentManager().beginTransaction().remove(fragments.get(fragments.size() - 1 - stack)).commit();

    }
}
