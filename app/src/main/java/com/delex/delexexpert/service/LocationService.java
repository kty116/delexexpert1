package com.delex.delexexpert.service;

import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.delex.delexexpert.activity.MainActivity3;
import com.delex.delexexpert.R;
import com.delex.delexexpert.application.BaseApplication;
import com.delex.delexexpert.commonLib.TinyDB;
import com.delex.delexexpert.event.CurrentLocationExpertEvent;
import com.delex.delexexpert.event.ExpertEvent;
import com.delex.delexexpert.event.LastLocationExpertEvent;
import com.delex.delexexpert.event.LocationServiceFinishExpertEvent;
import com.delex.delexexpert.model.MqttLocationModel;
import com.delex.delexexpert.model.MqttWorkModel;
import com.delex.delexexpert.userSession.ExpertSessionManager;
import com.delex.delexexpert.util.Commonlib;
import com.delex.delexexpert.util.LocationUtil;
import com.delex.delexexpert.util.MqttHelper;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.gson.Gson;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.Timer;
import java.util.TimerTask;


public class LocationService extends Service {

    public final String TAG = LocationService.class.getSimpleName();

    public final static String ACTION_START_DATA = "action_start_data";
    public final static String ACTION_STOP_DATA = "action_stop_data";
    public final String ACTION_CLICK_NOTIBAR = "action_click_notibar";
    public static final String ACTION_BATTERY_CHECK_SERVICE = "action_battery_check_service";

    private NotificationCompat.Builder mBLEStateNoti;

    /**
     * 파싱 변수
     */

    private Location mCurrentLocation;  //현재위치

    //test데이터 변수
    private Gson mGson;

    private String mLocationJsonDate;
    private MqttHelper mMqttHelper;
    private boolean isFirstConnected = true;  //mqtt에 처음 연결
    private String mOnOffJsonString;
    private LocationUtil sLocationUtil = MainActivity3.sLocationUtil;
    public static final String BROADCAST_LOCATION_UPDATE = "broadcast_receiver.LocationReceiver";
    private TinyDB mTinyDB;
    private Timer mLocationTimer;
    private TimerTask mLocationTimerTask;
    private boolean isEqualsLocation = true;  //일단 같음으로 해놓고 바뀌면 보낼수 있게
    private LocationCallback mLocationCallback;
    private ExpertSessionManager mExpertSessionManager;
    private String mClientId;
    private boolean isFirstRequest = true;
    private String mAcceptOrderNumber;
    private boolean mConnectedOrderTopic;
    private BroadcastReceiver mInternetReceiver;
    private boolean isFirstEOFException = true;
    private boolean mIsWork;
    private String mCarNum;
    private boolean onWorked = true;
    private boolean offWorked = true;
    private String mIntentAction;
    private LocationRequest mRocationRequest;

    public LocationService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onCreate() {
        super.onCreate();

        Log.d(TAG, "onCreate: ");

        EventBus.getDefault().register(this);
        mGson = new Gson();

        mExpertSessionManager = new ExpertSessionManager(this);
        mClientId = mExpertSessionManager.getUserId();
        mCarNum = mExpertSessionManager.getCarNum();

        createNoti(true, "");

        if (mMqttHelper != null) {
            mMqttHelper.disConnect();
        }

        setLocationMqtt(mExpertSessionManager.getUserId());

        mInternetReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {

                String action = intent.getAction();

                if (LocationManager.PROVIDERS_CHANGED_ACTION.equals(action)) {  //위치데이터 허용하는지 체크

                    if (locationOnOffCheck()) {
                        //gps 켜짐
                        Log.d(TAG, "onReceive: gps 켜짐");



//                        if (mIsWork) {
                            sLocationUtil.startLocationUpdates(mRocationRequest);
//                        } else {
//                            sLocationUtil.startLocationUpdates(sLocationUtil.mOffWorkLocationRequest);
//                        }

                    } else {
                        Log.d(TAG, "onReceive: gps 안켜짐");
                        sLocationUtil.stopLocationUpdate();
                        //gps 안켜짐
                    }
//                    startLocationUtil();
//                    setBatteryCheckAlarm(true);
                }
            }
        };

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(LocationManager.PROVIDERS_CHANGED_ACTION);
        registerReceiver(mInternetReceiver, intentFilter);


//        setBatteryCheckAlarm(true);

    }

    public boolean locationOnOffCheck() {
        boolean gpsEnable = false;
        LocationManager manager = (LocationManager) getApplication().getSystemService(Context.LOCATION_SERVICE);
        if (manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            gpsEnable = true;
        }

        return gpsEnable;
    }

//    /**
//     * 연결 상태 리시버
//     */
//    private final BroadcastReceiver mInternetReceiver = new BroadcastReceiver() {
//        @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
//        @Override
//        public void onReceive(Context context, Intent intent) {
//
//            String action = intent.getAction();
//
//            if (ConnectivityManager.CONNECTIVITY_ACTION.equals(action)) {  //인터넷 연결 됐을때 오는 콜백 , 인터넷 연결 됐을때 쌓인 데이터 보내기
//
//            } else if (action.equals(BROADCAST_LOCATION_UPDATE)) {
//                Location location = (Location) intent.getExtras().getParcelable("com.google.android.location.LOCATION");
//
//                if (location == null) {
//                    return;
//                } else {
//
//
////                    Log.d(TAG, "onReceive: " + location.getLatitude() + " / " + location.getLongitude() + " / " + location.getAccuracy());
//                }
//            }
//        }
//    };

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        if (intent != null) {
            if (intent.getAction() != null) {

                mIntentAction = intent.getAction();

                switch (mIntentAction) {

                    case ACTION_START_DATA:
                        Log.d(TAG, "onStartCommand: 출근");
                        mRocationRequest = sLocationUtil.mHighLocationRequest;
                        sLocationUtil.stopLocationUpdate();
                        sLocationUtil.startLocationUpdates(mRocationRequest);
                        onWorked = false;
                        createNoti(false, "출근");
                        break;

                    case ACTION_STOP_DATA:
                        Log.d(TAG, "onStartCommand: 퇴근");
                        mRocationRequest = sLocationUtil.mOffWorkLocationRequest;
                        sLocationUtil.stopLocationUpdate();
                        sLocationUtil.startLocationUpdates(mRocationRequest);
                        offWorked = false;
                        createNoti(false, "퇴근");
                        break;


                    case ACTION_CLICK_NOTIBAR: //노티 클릭
                        if (getApplication() instanceof BaseApplication) {
                            if (!((BaseApplication) getApplication()).isReturnedForeground()) {  //화면에 보이지 않을때
                                //메인 액티비티가 보이지 않을때만 화면 새로 띄우기
                                Intent intent1 = new Intent(getApplicationContext(), MainActivity3.class);
                                intent1.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                startActivity(intent1.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
                            }
                        }
                }
            }
        }
        return START_STICKY;
    }

    public void setLocationMqtt(String clientId) {


        mMqttHelper = new MqttHelper(this, clientId, new MqttCallbackExtended() {
            @Override
            public void connectComplete(boolean reconnect, String serverURI) {
                Log.d(TAG, "connectComplete: " + reconnect);

                switch (mIntentAction){
                    case ACTION_START_DATA:
                        onWorked = false;
                        break;

                    case ACTION_STOP_DATA:
                        offWorked = false;
                        break;
                }

                if (reconnect) {
                    sLocationUtil.startLocationUpdates(mRocationRequest);
                }
//                if (mCurrentLocation == null) {
//                    sLocationUtil.requestLastLocation();
//                } else {
//                    sendWorkToMqtt(true, mCurrentLocation);
//                }

            }

            @Override
            public void connectionLost(Throwable cause) {
//                Log.d(TAG, "connectionLost: " + cause.getCause());
//                Log.d(TAG, "connectionLost: " + cause.getMessage());
//                Log.d(TAG, "connectionLost: " + cause.getLocalizedMessage());
//                Log.d(TAG, "connectionLost: " + cause.getStackTrace());
                sLocationUtil.stopLocationUpdate();
//                if (cause instanceof java.net.SocketException) {
//
//                }
//
//                if (cause.getCause() instanceof java.io.EOFException) {
//
//                    if (isFirstEOFException) {
//                        isFirstEOFException = false;
//                        EventBus.getDefault().post(new FinishExpertEvent());
//                        stopSelf();
//                    }
//                }
            }

            @Override
            public void messageArrived(String topic, MqttMessage message) throws Exception {

            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken token) {

            }
        });
    }

    public void createNoti(boolean firstNoti, @Nullable String message) {
        if (firstNoti) {

            String channelId = "new_delex_channel_id";
            String channelName = "new_delex_channel_name";

            NotificationManager notifiManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

                int importance = NotificationManager.IMPORTANCE_HIGH;
                NotificationChannel mChannel = new NotificationChannel(channelId, channelName, importance);
                notifiManager.createNotificationChannel(mChannel);
            }


            mBLEStateNoti = new NotificationCompat.Builder(this, channelId)
                    .setContentTitle(message)
                    .setSmallIcon(R.mipmap.ic_launcher_expert_round)
                    .setContentIntent(clickNotiPendingIntent());  //노티 클릭설정
        } else {
            mBLEStateNoti.setContentTitle(message);
        }


        startForeground(1, mBLEStateNoti.build());

    }

    private PendingIntent clickNotiPendingIntent() {

        Intent clickNotiIntent = new Intent(this, this.getClass());
        clickNotiIntent.setAction(ACTION_CLICK_NOTIBAR);
        PendingIntent pending = PendingIntent.getService(this, 1, clickNotiIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        return pending;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

//        setBatteryCheckAlarm(false);
        sendWorkToMqtt(false, mCurrentLocation);
        unregisterReceiver(mInternetReceiver);
        sLocationUtil.stopLocationUpdate();
        EventBus.getDefault().unregister(this);
        if (mMqttHelper != null) {
            mMqttHelper.disConnect();
        }
        stopForeground(true);  //노티피케이션 지우기
    }


    //    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(ExpertEvent expertEvent) {

//        if (expertEvent instanceof OffWorkEvent) {  //퇴근 버튼 클릭시
//            sendWorkToMqtt(false, mCurrentLocation);
//            createNoti(false, "퇴근");
////            setBatteryCheckAlarm(false);
//            sLocationUtil.stopLocationUpdate();
//            sLocationUtil.startLocationUpdates(sLocationUtil.mOffWorkLocationRequest);

        if (expertEvent instanceof LocationServiceFinishExpertEvent) {  //로그아웃 버튼 클릭시
            stopSelf();

        } else if (expertEvent instanceof CurrentLocationExpertEvent) {  //현재 위치 값 업데이트

            if (mMqttHelper != null) {
                CurrentLocationExpertEvent currentLocationEvent = (CurrentLocationExpertEvent) expertEvent;
                Location currentLocation = currentLocationEvent.getLocation();

                if (!onWorked) {
                    //출근하기
                    onWorked = true;
                    sendWorkToMqtt(true, currentLocation);
                    Log.d(TAG, "connectComplete: 출근 데이터 보냄");
                }

                if (!offWorked) {
                    //퇴근하기
                    offWorked = true;
                    sendWorkToMqtt(false, currentLocation);
                    Log.d(TAG, "connectComplete: 퇴근 데이터 보냄");
                }
//                if (mCurrentLocation != null) {


//                    double oldLat = mCurrentLocation.getLatitude();
//                    double oldLon = mCurrentLocation.getLongitude();
//
//                    double newLat = currentLocation.getLatitude();
//                    double newLon = currentLocation.getLongitude();
//                    if (oldLat == newLat && oldLon == newLon) {  //위치 데이터가 같을때
//
//                    } else {  //위치 데이터가 같지 않을 때
                mCurrentLocation = currentLocation;
                sendCurrentLocationToMqtt();

//                } else {
//                    mCurrentLocation = currentLocation;
//                    sendCurrentLocationToMqtt();
//                }
            }
        } else if (expertEvent instanceof LastLocationExpertEvent) {
            LastLocationExpertEvent lastLocationEvent = (LastLocationExpertEvent) expertEvent;
            Location location = lastLocationEvent.getLocation();
            mCurrentLocation = location;
            sendWorkToMqtt(true, location);
        }

    }

    public void setBatteryCheckAlarm(boolean start) {
        Intent alarmIntent = new Intent(ACTION_BATTERY_CHECK_SERVICE);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 2, alarmIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        long delay = 30 * 60 * 1000;  //설정안 변수로 가져오기

        if (start) {  //true면 알람 시작
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + delay, pendingIntent);
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                alarmManager.setExact(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + delay, pendingIntent);
            } else {
                alarmManager.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + delay, pendingIntent);
            }
        } else {  //false면 알람 취소
            alarmManager.cancel(pendingIntent);
        }
    }


//    /**
//     * 위치 정보 서버로 보내주는 타이머 등록
//     */
//    public void startLocationUploadTask() {
//
//        if (mLocationTimer != null) {
//            return;
//        }
//        mLocationTimer = new Timer();
//
//        mLocationTimerTask = new TimerTask() {
//            @Override
//            public void run() {
//                sendLocationUploadToMqttWaitTopic();
//            }
//        }
//
//        ;
//        mLocationTimer.schedule(mLocationTimerTask, 0, 5000);
//    }
//
//    /**
//     * 위치 정보 서버로 보내주는 타이머 해제
//     */
//    public void stopLocationUploadTask() {
//        if (mLocationTimerTask != null) {
//            mLocationTimerTask.cancel();
//            mLocationTimer.cancel();
//        }
//    }


//    /**
//     * 서버에 출근 메세지 보내기
//     *
//     * @param location
//     */
//    public void requestConnectToMqtt(Location location) {
//
//        if (mClientId != null) {
//            String data = mGson.toJson(new LocationDataModel(mClientId, new Date().getTime(), location.getLatitude(), location.getLongitude()));
//            mMqttHelper.publishMessage(MqttHelper.sResponseConnect, data);
//        }
//    }
//
//
//    /**
//     * 서버에 퇴근 메세지 보내기
//     */
//    public void requestDisconnectToMqtt() {
//        if (mMqttHelper != null && mMqttHelper.isConnected()) {
//            if (mOnOffJsonString != null) {
//                mMqttHelper.publishMessage(MqttHelper.mCallCatchTopic, mOnOffJsonString);
//            }
//            mMqttHelper.disConnect();
//        }
//    }

    /**
     * 출퇴근 메세지 보내기
     */
    public void sendWorkToMqtt(boolean isWork, Location location) {
        mIsWork = isWork;

        if (Commonlib.isNetworkAvailable(getApplicationContext())) {
            if (mMqttHelper.isConnected()) {

                MqttWorkModel mqttWorkModel = new MqttWorkModel(mClientId, mCarNum, isWork, location.getLatitude(), location.getLongitude());

                mMqttHelper.publishMessage(mMqttHelper.mWorkTopic, mGson.toJson(mqttWorkModel));

            }
        }
    }

    /**
     * 위치데이터 메세지 보내기
     */
    public void sendCurrentLocationToMqtt() {
        Log.d(TAG, "sendCurrentLocationToMqtt: 위치 데이터 보냄");
        if (Commonlib.isNetworkAvailable(getApplicationContext())) {
            if (mMqttHelper.isConnected()) {

                MqttLocationModel mqttLocationModel = new MqttLocationModel(mClientId, mCarNum, mIsWork, mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude());

                mMqttHelper.publishMessage(mMqttHelper.mLocationTopic, mGson.toJson(mqttLocationModel));

            }
        }
    }
}