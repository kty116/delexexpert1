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

import com.delex.delexexpert.activity.MainActivity;
import com.delex.delexexpert.R;
import com.delex.delexexpert.commonLib.TinyDB;
import com.delex.delexexpert.event.CurrentLocationExpertEvent;
import com.delex.delexexpert.event.ExpertEvent;
import com.delex.delexexpert.event.LastLocationExpertEvent;
import com.delex.delexexpert.event.LocationServiceFinishExpertEvent;
import com.delex.delexexpert.firebase.DataBase;
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

    private Location mCurrentLocation;//현재위치

    //test데이터 변수
    private Gson mGson;

    private String mLocationJsonDate;
    private MqttHelper mMqttHelper;
    private boolean isFirstConnected = true;  //mqtt에 처음 연결
    private String mOnOffJsonString;
    private LocationUtil sLocationUtil;
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
    public static LocationRequest sLocationRequest;
    private DataBase mDataBase;

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

        Log.d(TAG, "onCreate: ddddddd");

        createNoti(true, "");
        EventBus.getDefault().register(this);
        mGson = new Gson();

        mDataBase = new DataBase(this);
        mDataBase.writeStateData(false, false, true, "서비스 실행", "", "");

        sLocationUtil = new LocationUtil(this);

        mExpertSessionManager = new ExpertSessionManager(this);
        mClientId = mExpertSessionManager.getUserId();
        mCarNum = mExpertSessionManager.getCarNum();

//        if (mExpertSessionManager.getLocationSetting().equals("출근")) {
//            mIntentAction = ACTION_START_DATA;
//            sLocationRequest = sLocationUtil.mHighLocationRequest;

//        } else {
//            mIntentAction = ACTION_STOP_DATA;
//            sLocationRequest = sLocationUtil.mOffWorkLocationRequest;
//            createNoti(true, "퇴근");
//        }

        if (mMqttHelper != null) {
            mMqttHelper.disConnect();
        }

        setLocationMqtt(mExpertSessionManager.getUserId());

        mInternetReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {

                String action = intent.getAction();

                if (LocationManager.PROVIDERS_CHANGED_ACTION.equals(action)) {  //위치데이터 허용하는지 체크

                    if (Commonlib.locationOnOffCheck(getApplicationContext())) {
                        //gps 켜짐
                        if (mCurrentLocation != null) {
                            mDataBase.writeStateData(true, true, true, mCurrentLocation.getLatitude() + " / " + mCurrentLocation.getLongitude(), "", "gps켜짐");
                        } else {
                            mDataBase.writeStateData(true, true, true, "", "", "gps 켜짐");
                        }

                        sLocationUtil.startLocationUpdates(sLocationRequest);

                    } else {
                        if (mCurrentLocation != null) {
                            mDataBase.writeStateData(true, true, true, mCurrentLocation.getLatitude() + " / " + mCurrentLocation.getLongitude(), "", "gps 꺼짐");
                        } else {
                            mDataBase.writeStateData(true, true, true, "", "", "gps 꺼짐");
                        }
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

        Log.d(TAG, "onStartCommand: ddddddd");

        if (intent != null) {
            if (intent.getAction() != null) {
                mIntentAction = intent.getAction();
                if (mIntentAction != null) {
                    switch (mIntentAction) {

                        case ACTION_START_DATA:
                            sLocationRequest = sLocationUtil.mHighLocationRequest;
                            sLocationUtil.stopLocationUpdate();
                            sLocationUtil.startLocationUpdates(sLocationRequest);
                            onWorked = false;
                            offWorked = true;
                            if (mCurrentLocation != null) {
                                mDataBase.writeStateData(true, true, true, mCurrentLocation.getLatitude() + " / " + mCurrentLocation.getLongitude(), "", "출근");
                            } else {
                                mDataBase.writeStateData(true, true, true, "", "", "출근");

                            }
                            createNoti(false, "출근");
                            break;

                        case ACTION_STOP_DATA:
                            sLocationRequest = sLocationUtil.mOffWorkLocationRequest;
                            sLocationUtil.stopLocationUpdate();
                            sLocationUtil.startLocationUpdates(sLocationRequest);
                            offWorked = false;
                            onWorked = true;
                            if (mCurrentLocation != null) {
                                mDataBase.writeStateData(false, true, true, mCurrentLocation.getLatitude() + " / " + mCurrentLocation.getLongitude(), "", "퇴근");
                            } else {
                                mDataBase.writeStateData(false, true, true, "", "", "퇴근");

                            }
                            createNoti(false, "퇴근");
                            break;


//                    case ACTION_CLICK_NOTIBAR: //노티 클릭
//                        if (getApplication() instanceof BaseApplication) {
//                            if (!((BaseApplication) getApplication()).isReturnedForeground()) {  //화면에 보이지 않을때
//                                //메인 액티비티가 보이지 않을때만 화면 새로 띄우기
//                                Intent intent1 = new Intent(getApplicationContext(), MainActivity.class);
//                                intent1.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
//                                startActivity(intent1.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
//                            }else {
//                                Log.d(TAG, "onStartCommand: !((BaseApplication) getApplication()).isReturnedForeground()");
//                            }
//                        }else {
//                            Log.d(TAG, "onStartCommand: getApplication() instanceof BaseApplication");
//                        }
                    }
                }
            }
        } else {
            onDestroy();
        }
        return START_REDELIVER_INTENT;
    }

    public void setLocationMqtt(String clientId) {


        mMqttHelper = new MqttHelper(this, clientId, new MqttCallbackExtended() {
            @Override
            public void connectComplete(boolean reconnect, String serverURI) {
//                Log.d(TAG, "connectComplete: " + reconnect);

                switch (mIntentAction) {
                    case ACTION_START_DATA:
                        offWorked = true;
                        onWorked = false;
                        if (mCurrentLocation != null) {
                            mDataBase.writeStateData(true, true, true, mCurrentLocation.getLatitude() + " / " + mCurrentLocation.getLongitude(), "", "mMqttHelper 연결");
                        } else {
                            mDataBase.writeStateData(true, true, true, "", "", "mMqttHelper 연결");
                        }
                        break;

                    case ACTION_STOP_DATA:
                        offWorked = false;
                        onWorked = true;
                        if (mCurrentLocation != null) {
                            mDataBase.writeStateData(false, true, true, mCurrentLocation.getLatitude() + " / " + mCurrentLocation.getLongitude(), "", "mMqttHelper 연결");
                        } else {
                            mDataBase.writeStateData(false, true, true, "", "", "mMqttHelper 연결");
                        }
                        break;
                }


                if (reconnect) {
                    sLocationUtil.startLocationUpdates(sLocationRequest);
                }
//                if (mCurrentLocation == null) {
//                    sLocationUtil.requestLastLocation();
//                } else {
//                    sendWorkToMqtt(true, mCurrentLocation);
//                }

            }

            @Override
            public void connectionLost(Throwable cause) {
                Log.d(TAG, "connectionLost: " + cause);
                if (mCurrentLocation != null) {
                    mDataBase.writeStateData(false, false, true, mCurrentLocation.getLatitude() + " / " + mCurrentLocation.getLongitude(), "mqtt connectionLost : " + cause, "");
                } else {
                    mDataBase.writeStateData(false, false, true, "", "mqtt connectionLost : " + cause, "");
                }
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
                    .setSmallIcon(R.mipmap.ic_launcher_round)
                    .setContentIntent(clickNotiPendingIntent());  //노티 클릭설정
        } else {
            mBLEStateNoti.setContentTitle(message);
        }
        startForeground(1, mBLEStateNoti.build());
    }

    private PendingIntent clickNotiPendingIntent() {

        Intent clickNotiIntent = new Intent(this, MainActivity.class);
        PendingIntent pending = PendingIntent.getActivity(this, 1, clickNotiIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        return pending;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

//        setBatteryCheckAlarm(false);
        if (mCurrentLocation != null) {
            sendWorkToMqtt(false, mCurrentLocation);
        }
        unregisterReceiver(mInternetReceiver);
        sLocationUtil.stopLocationUpdate();
        EventBus.getDefault().unregister(this);
        if (mMqttHelper != null) {
            mMqttHelper.disConnect();
        }
        stopForeground(true);  //노티피케이션 지우기

        if (mCurrentLocation != null) {
            mDataBase.writeStateData(false, mMqttHelper.isConnected(), false, mCurrentLocation.getLatitude() + " / " + mCurrentLocation.getLongitude(), "", "위치데이터 서비스 죽음");
        } else {
            mDataBase.writeStateData(false, mMqttHelper.isConnected(), false, "", "", "위치데이터 서비스 죽음");
        }

//        new Thread(new Runnable() {
//            @Override
//            public void run() {
//                try {
//                    Thread.sleep(10000);
//                    startDataService();
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                }
//            }
//        }).start();


    }

    public void startDataService() {

        Intent serviceIntent = new Intent(this, LocationService.class);
        serviceIntent.setAction(LocationService.ACTION_STOP_DATA);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {  //API 26버전 이상
            this.startForegroundService(serviceIntent);
        } else {
            this.startService(serviceIntent);
        }
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

        if (expertEvent instanceof LocationServiceFinishExpertEvent) {  //서비스 종료 호출
            stopSelf();

        } else if (expertEvent instanceof CurrentLocationExpertEvent) {  //현재 위치 값 업데이트

            if (mMqttHelper != null) {

                CurrentLocationExpertEvent currentLocationEvent = (CurrentLocationExpertEvent) expertEvent;
                Location currentLocation = currentLocationEvent.getLocation();
                mCurrentLocation = currentLocation;

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

                float accuracy = currentLocation.getAccuracy();
                if (accuracy > 0 && accuracy < 20) { // 1 ~ 20사이값만 가져온다
                    sendCurrentLocationToMqtt();
                }
                if (mCurrentLocation != null) {
                    mDataBase.writeStateData(mIsWork, mMqttHelper.isConnected(), true, mCurrentLocation.getLatitude() + " / " + mCurrentLocation.getLongitude(), "", "위치데이터 보냄");
                }

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
        long delay = 10 * 1000;  //설정안 변수로 가져오기

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

        if (mExpertSessionManager.getLocationSetting().equals("출근")) {
            mIsWork = true;
        } else {
            mIsWork = false;
        }

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
        if (Commonlib.isNetworkAvailable(getApplicationContext())) {
            if (mMqttHelper.isConnected()) {

                MqttLocationModel mqttLocationModel = new MqttLocationModel(mClientId, mCarNum, mIsWork, mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude());
                mMqttHelper.publishMessage(mMqttHelper.mLocationTopic, mGson.toJson(mqttLocationModel));
                Log.d(TAG, "sendCurrentLocationToMqtt: " + mGson.toJson(mqttLocationModel));
            }
        }
    }
}