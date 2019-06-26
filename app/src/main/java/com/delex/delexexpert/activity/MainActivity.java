package com.delex.delexexpert.activity;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.webkit.GeolocationPermissions;
import android.webkit.JavascriptInterface;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.delex.delexexpert.R;
import com.delex.delexexpert.event.CurrentLocationExpertEvent;
import com.delex.delexexpert.event.ExpertEvent;
import com.delex.delexexpert.event.FinishExpertEvent;
import com.delex.delexexpert.event.GpsOnOffExpertEvent;
import com.delex.delexexpert.event.LocationServiceFinishExpertEvent;
import com.delex.delexexpert.service.LocationService;
import com.delex.delexexpert.userSession.ExpertSessionManager;
import com.delex.delexexpert.userSession.SessionManager;
import com.delex.delexexpert.userSession.SessionManagerPojo;
import com.delex.delexexpert.util.Commonlib;
import com.delex.delexexpert.util.EditImageUtil;
import com.delex.delexexpert.util.LocationUtil;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.IOException;
import java.net.HttpCookie;
import java.net.URL;
import java.util.List;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;

public class MainActivity extends AppCompatActivity {

    public final String TAG = MainActivity.class.getSimpleName();
    public final int POST = 1;
    public final int DELETE = 2;
    public final int PUT = 3;
    private final long FINISH_INTERVAL_TIME = 2000;
    private long backPressedTime = 0;
    public static boolean sVisibleActivity;  //화면 보이면 노티 눌렀을때 다시 액티비티 켜지지 않게 설정하는 변수

    private String mUrl;  //현재 페이지 url
    private LocationUtil sLocationUtil;
    private String mCM;
    private ValueCallback<Uri> mUM;
    private ValueCallback<Uri[]> mUMA;

    private EditImageUtil editImageUtil;
//    private RetrofitLib mRetrofitLib;

    private SharedPreferences mPref;

    private static final int IMAGE_MAX_DIMENSION = 1280;

    private boolean firstPageLoadingCompleted = false;
    private ConnectivityManager mManager;
    //    private WaveLoadingView mWaveLoadingView;
    private NetworkInfo mMobile;
    private NetworkInfo mWifi;
    private Handler mHandler;

    private boolean canEnd = false;
    private int count = 0;
    public boolean splashLoadingComplite = false;
    private boolean loadingConfirm = false;
    private String mPushToken;
    private SessionManager mSessionManager;
    private ExpertSessionManager mExpertSessionManager;
    private Intent mServiceIntent;
    public static Location sCurrentLocation;
    private WebView mWebView;
    private ProgressBar mProgressBar;
    private FrameLayout mSplashLayout;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(R.layout.activity_main);

        mWebView = (WebView) findViewById(R.id.webView);
        mProgressBar = (ProgressBar) findViewById(R.id.progressBar);
        mSplashLayout = (FrameLayout) findViewById(R.id.splash_layout);

        mPref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        mSessionManager = new SessionManager(this);

        mManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        editImageUtil = new EditImageUtil();
        mExpertSessionManager = new ExpertSessionManager(this);
        sLocationUtil = new LocationUtil(this);

        networkCheck();

    }

    public void setWebview() {
        WebSettings webSettings = mWebView.getSettings();

        webSettings.setSaveFormData(true);
        webSettings.setSupportZoom(true);
        webSettings.setBuiltInZoomControls(true);
        webSettings.setDisplayZoomControls(false);
        webSettings.setDomStorageEnabled(true);
        webSettings.setGeolocationEnabled(true);

        if (Build.VERSION.SDK_INT >= 16) {
            webSettings.setAllowFileAccessFromFileURLs(true);
            webSettings.setAllowUniversalAccessFromFileURLs(true);
        }
        if (Build.VERSION.SDK_INT >= 21) {
            webSettings.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
        }

        webSettings.setUserAgentString(webSettings.getUserAgentString() + "|APP/1.2.3");

        mWebView.getSettings().setJavaScriptEnabled(true);
        // JavaScript의 window.open 허용
        mWebView.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);

        mWebView.addJavascriptInterface(new MyJavascriptInterface(), "Android");
        mWebView.setWebChromeClient(new WebChromeClient() {

            @Override
            public void onGeolocationPermissionsShowPrompt(String origin, GeolocationPermissions.Callback callback) {
                super.onGeolocationPermissionsShowPrompt(origin, callback);
                callback.invoke(origin, true, false);
            }

            //For Android 3.0+
            public void openFileChooser(ValueCallback<Uri> uploadMsg) {
                mUM = uploadMsg;
            }

            // For Android 3.0+, above method not supported in some android 3+ versions, in such case we use this
            public void openFileChooser(ValueCallback uploadMsg, String acceptType) {
                mUM = uploadMsg;
            }

            //For Android 4.1+
            public void openFileChooser(ValueCallback<Uri> uploadMsg, String acceptType, String capture) {
                mUM = uploadMsg;

            }

            //For Android 5.0+
            public boolean onShowFileChooser(WebView webView, ValueCallback<Uri[]> filePathCallback, FileChooserParams fileChooserParams) {
                Log.d(TAG, "onShowFileChooser: ");

                if (mUMA != null) {
                    mUMA.onReceiveValue(null);
                }
                mUMA = filePathCallback;

                mCM = "file:";
                return true;
            }
        });

        mWebView.setWebViewClient(new WebViewClient() {

            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                mProgressBar.setVisibility(View.VISIBLE);
                mUrl = url;

            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);

                mUrl = url;

                mProgressBar.setVisibility(View.INVISIBLE);

                if (!firstPageLoadingCompleted) {  //처음 로딩할때 페이지 로딩 완료를 알려주는 변수
                    //로딩 끝
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            while (!splashLoadingComplite) {
                                try {
                                    Thread.sleep(2000);
                                    if (loadingConfirm) {
                                        mHandler.post(new Runnable() {
                                            @Override
                                            public void run() {
                                                mSplashLayout.setVisibility(View.GONE);
                                            }
                                        });
                                        canEnd = true;
                                        splashLoadingComplite = true;
                                    }

                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    }).start();

                    firstPageLoadingCompleted = true;
                }
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                Log.d(TAG, "shouldOverrideUrlLoading: " + url);
                mUrl = url;
                view.loadUrl(url);
                return true;
            }
        });

        webSettings.setJavaScriptEnabled(true);
    }


    @Override
    protected void onResume() {
        super.onResume();

        sVisibleActivity = true;

//        SharedPreferences.Editor editor = mPref.edit();
//        editor.putInt(MyFirebaseMessagingService.PUSH_COUNT, 0);
//        editor.commit();
//
//        int pushCount = mPref.getInt(MyFirebaseMessagingService.PUSH_COUNT, 0);
//        Log.d("dd", "onResume: " + pushCount);
//
//        PushConnectService.setBadge(this, pushCount);
//        Log.d(TAG, "onResume: ");

    }

    @Override
    protected void onPause() {
        super.onPause();
        sVisibleActivity = false;

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onBackPressed() {

        if (canEnd) {

            long tempTime = System.currentTimeMillis();
            long intervalTime = tempTime - backPressedTime;

            if (0 <= intervalTime && FINISH_INTERVAL_TIME >= intervalTime) {
                super.onBackPressed();
                Log.d(TAG, "onBackPressed: ");
            } else {
                backPressedTime = tempTime;
                Toast.makeText(getApplicationContext(), "'뒤로' 버튼을 한번 더 누르시면 종료됩니다.", Toast.LENGTH_SHORT).show();
                Log.d(TAG, "onBackPressed: ");
            }
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        String url = mWebView.getUrl();
        String loginPageUrl = getString(R.string.login_url);
        String mainPageUrl = getString(R.string.main_url);
        String loginCheckPageUrl = getString(R.string.login_check_url);
        String logoutPageUrl = getString(R.string.logout_url);
        Log.d(TAG, "onKeyDown: login" + loginPageUrl);

        if (url != null) {

            if ((keyCode == KeyEvent.KEYCODE_BACK) && (url.equals(loginPageUrl) || url.equals(mainPageUrl) || url.equals(loginCheckPageUrl) || url.equals(logoutPageUrl))) {

                Log.d(TAG, "onKeyDown: main_url" + url);

            } else if ((keyCode == KeyEvent.KEYCODE_BACK) && mWebView.canGoBack()) {  //메인인데 뒤로 갈 수 있으면
                mWebView.goBack();
                Log.d(TAG, "onKeyDown dddd: " + mUrl);

                return true;
            }
        }
        return super.onKeyDown(keyCode, event);
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        editImageUtil.fileDelete(this);
        sVisibleActivity = false;
        count = 111;
        loadingConfirm = true;
        splashLoadingComplite = true;
    }

    public class MyJavascriptInterface {

        @JavascriptInterface
        public void login(String loginId, String loginCarNum) {

            mExpertSessionManager.setLogin(true);

            if (Commonlib.isServiceRunning(MainActivity.this)) {
                EventBus.getDefault().post(new LocationServiceFinishExpertEvent());
            }

            Log.d(TAG, "login: ");

            mExpertSessionManager.setUserId(loginId);
            mExpertSessionManager.setCarNum(loginCarNum);
            mExpertSessionManager.setLocationSetting("출근");
            String userId = mExpertSessionManager.getUserId();
            String carNum = mExpertSessionManager.getCarNum();

            String pushToken = mSessionManager.getStringData(SessionManagerPojo.PUSH_TOKEN);

            Commonlib.sendPushToken(userId, pushToken);

            if (userId != null && !userId.isEmpty() && carNum != null && !carNum.isEmpty()) {
                Commonlib.startService(getApplicationContext(), mExpertSessionManager);
            } else {
                Toast.makeText(MainActivity.this, "로그인을 재시도 해주세요.", Toast.LENGTH_SHORT).show();
            }
        }

        @JavascriptInterface
        public void logout() {
            Log.d(TAG, "logout: ");

            if (mExpertSessionManager.isLogin()) {

                if (Commonlib.isServiceRunning(MainActivity.this)) {
                    String userId = mExpertSessionManager.getUserId();
                    String carNum = mExpertSessionManager.getCarNum();
                    mExpertSessionManager.setLocationSetting("퇴근");

                    if (userId != null && !userId.isEmpty() && carNum != null && !carNum.isEmpty()) {
                        stopDataService();
                    }
                }
            }

            mExpertSessionManager.setLogin(false);
        }

        @JavascriptInterface
        public void onWork(String string1, String string2) {
            Log.d(TAG, "onWork: ");
            String userId = mExpertSessionManager.getUserId();
            String carNum = mExpertSessionManager.getCarNum();

            mExpertSessionManager.setLocationSetting("출근");

            if (userId != null && !userId.isEmpty() && carNum != null && !carNum.isEmpty()) {
                startDataService();
            } else {
                Toast.makeText(MainActivity.this, "로그인을 재시도 해주세요.", Toast.LENGTH_SHORT).show();
            }
        }

        @JavascriptInterface
        public void offWork(String string1, String string2) {
            Log.d(TAG, "offWork: ");
            String userId = mExpertSessionManager.getUserId();
            String carNum = mExpertSessionManager.getCarNum();
            mExpertSessionManager.setLocationSetting("퇴근");

            if (userId != null && !userId.isEmpty() && carNum != null && !carNum.isEmpty()) {
                stopDataService();
            } else {
                Toast.makeText(MainActivity.this, "로그인을 재시도 해주세요.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    public void startDataService() {

        mServiceIntent = new Intent(this, LocationService.class);
        mServiceIntent.setAction(LocationService.ACTION_START_DATA);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {  //API 26버전 이상
            this.startForegroundService(mServiceIntent);
        } else {
            this.startService(mServiceIntent);
        }
    }

    public void stopDataService() {

        mServiceIntent = new Intent(this, LocationService.class);
        mServiceIntent.setAction(LocationService.ACTION_STOP_DATA);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {  //API 26버전 이상
            this.startForegroundService(mServiceIntent);
        } else {
            this.startService(mServiceIntent);
        }
    }


//    /**
//     * 푸시토큰변수값 초기화
//     */
//    public void initPushToken() {
//        mPushToken = mSessionManager.getStringData(SessionManagerPojo.PUSH_TOKEN);
//
//        if (mPushToken.isEmpty() || mPushToken == null) {
//            FirebaseInstanceId.getInstance().getInstanceId().addOnSuccessListener(this, new OnSuccessListener<InstanceIdResult>() {
//                @Override
//                public void onSuccess(InstanceIdResult instanceIdResult) {
//                    String newToken = instanceIdResult.getToken();
//                    mSessionManager.setStringData(SessionManagerPojo.PUSH_TOKEN, newToken);
//                    mPushToken = newToken;
//                    Log.d(TAG, "initData: " + mPushToken);
//
//
//                }
//            });
//
//        } else {
//            mPushToken = mSessionManager.getStringData(SessionManagerPojo.PUSH_TOKEN);
//            Log.d(TAG, "initData: " + mPushToken);
//        }
//    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case LocationUtil.REQUEST_CHECK_SETTINGS:
                if (resultCode == Activity.RESULT_OK) {
                    sLocationUtil.checkLocationSettings(this);
                } else if (resultCode == RESULT_CANCELED) {
                    sLocationUtil.checkLocationSettings(this);
                }
                break;
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
    }

    @Override
    protected void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(ExpertEvent expertEvent) {
        if (expertEvent instanceof GpsOnOffExpertEvent) {
            GpsOnOffExpertEvent gpsOnOffEvent = (GpsOnOffExpertEvent) expertEvent;
            if (gpsOnOffEvent.isGpsOnOff()) {

                splashThread();

                if (sLocationUtil != null) {  //locationUtil 널이 아닐때만 널 처리
                    sLocationUtil.stopLocationUpdate();
                    sLocationUtil = null;
                }

//                startLocationUtil();
            }
        } else if (expertEvent instanceof FinishExpertEvent) {
            android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
            builder.setTitle("동일한 아이디 재접속")
                    .setMessage("다른기기에서 이미 현재 아이디를 사용중입니다. 먼저 다른기기에서 로그아웃 후 재접속 해주세요.")
                    .setPositiveButton("확인", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            finish();
                        }
                    });
            builder.show();
        } else if (expertEvent instanceof CurrentLocationExpertEvent) {  //현재 위치 값 업데이트

//            CurrentLocationExpertEvent currentLocationEvent = (CurrentLocationExpertEvent) expertEvent;
//            Location currentLocation = currentLocationEvent.getLocation();
//            sCurrentLocation = currentLocation;


        }
    }

    public static void cookieMaker(String url) {
        //롤리팝 이하 버전 cookiesyncmanager로 사용

        String COOKIES_HEADER = "Set-Cookie";
        try {

            URL url1 = new URL(url);
            HttpsURLConnection con = (HttpsURLConnection) url1.openConnection();

            con.connect();

            Map<String, List<String>> headerFields = con.getHeaderFields();
            List<String> cookiesHeader = headerFields.get(COOKIES_HEADER);

            if (cookiesHeader != null) {
                for (String cookie : cookiesHeader) {
                    String cookieName = HttpCookie.parse(cookie).get(0).getName();
                    String cookieValue = HttpCookie.parse(cookie).get(0).getValue();

                    String cookieString = cookieName + "=" + cookieValue;
                    Log.d("d", "cookieMaker: " + cookieString);

//                    CookieManager.getInstance().setCookie("https://example.co.kr", cookieString);

                }
            }

        } catch (IOException e) {
            e.printStackTrace();

        }
    }

    private void noNetwork() {
        AlertDialog.Builder alert_confirm = new AlertDialog.Builder(MainActivity.this);
        alert_confirm.setMessage("인터넷 연결 확인 후 다시 시도해주세요.").setCancelable(false).setPositiveButton("재접속",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        networkCheck();
                    }
                }).setNegativeButton("취소", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                finish();
            }
        });
        AlertDialog alert = alert_confirm.create();
        alert.show();
    }

    public void networkCheck() {
        mMobile = mManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
        mWifi = mManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        if (mWifi.isConnected() || mMobile.isConnected()) {  //인터넷 연결 됐을때
//            initPushToken();
            permissionCheck();

        } else {
            //인터넷 연결 안됐을때
            noNetwork();
        }
    }

    /**
     * 퍼미션 체크
     */
    public void permissionCheck() {

        Commonlib.permissionCheck(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                false, new Commonlib.PermissionCheckResponseImpl() {
                    @Override
                    public void granted() {
                        sLocationUtil.checkLocationSettings(MainActivity.this);

                    }

                    @Override
                    public void denied() {
                    }
                });
    }


    public void splashThread() {
        Log.d(TAG, "splashThread: ");

        setWebview();

        mWebView.loadUrl(getString(R.string.main_url));

//        if (mExpertSessionManager.isLogin()) {
//            Commonlib.serviceCheckAndStart(this);
//        }
//        } else {
//            binding.webView.loadUrl(getString(R.string.logout_url));
//        }
        mHandler = new Handler();

        final Thread thread = new Thread(new Runnable() {

            @Override
            public void run() {

//                try {
//                    Thread.sleep(1000);
                loadingConfirm = true;
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                }

            }
        });
        thread.start();
    }
}
