package com.delex.delexexpert.util;

import android.content.Context;
import android.util.Log;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.DisconnectedBufferOptions;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;


public class MqttHelper {

    private final String mClientId;
    public final String mWorkTopic;
    public final String mLocationTopic;
    private MqttAndroidClient mMqttAndroidClient;
    private Context context;
    public static final String TAG = MqttHelper.class.getSimpleName();
    private final String mServerUri = "tcp://15.164.150.169:1883"; // 브로커 주소

    public MqttHelper(Context context, String clientId, MqttCallbackExtended callback) {

        this.context = context;
        this.mClientId = clientId;

        mLocationTopic = "driver/location";
        mWorkTopic = "driver/work";
        Log.d(TAG, "MqttHelper: " + mLocationTopic);
        Log.d(TAG, "MqttHelper: " + mWorkTopic);

        mMqttAndroidClient = new MqttAndroidClient(context, mServerUri, mClientId);
        mMqttAndroidClient.setCallback(callback);

        connect();
    }

    private void connect() {
        MqttConnectOptions mqttConnectOptions = new MqttConnectOptions();
        mqttConnectOptions.setAutomaticReconnect(true);
        mqttConnectOptions.setCleanSession(false);
        mqttConnectOptions.setKeepAliveInterval(10 * 60 * 1000);
        mqttConnectOptions.setConnectionTimeout(20 * 60 * 1000);


        try {
            mMqttAndroidClient.connect(mqttConnectOptions, null, new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    Log.d(TAG, "connectonSuccess: 연결 성공");
                    DisconnectedBufferOptions disconnectedBufferOptions = new DisconnectedBufferOptions();
                    disconnectedBufferOptions.setBufferEnabled(true);
                    disconnectedBufferOptions.setBufferSize(100);
                    disconnectedBufferOptions.setPersistBuffer(false);
                    disconnectedBufferOptions.setDeleteOldestMessages(false);
                    mMqttAndroidClient.setBufferOpts(disconnectedBufferOptions);
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    Log.d(TAG, "connectonFailure: " + exception);
                }
            });

        } catch (MqttException ex) {
            ex.printStackTrace();
        }

    }

//    /**
//     * 토픽 구독
//     */
//    public void subscribeToWaitingTopic() {
//        try {
//            mMqttAndroidClient.subscribe(mCallCatchTopic, 2, null, new IMqttActionListener() {
//                @Override
//                public void onSuccess(IMqttToken asyncActionToken) {
//                }
//
//                @Override
//                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
//                }
//            });
//
//        } catch (MqttException ex) {
//            ex.printStackTrace();
//        }
//
//    }
//
//
//    public void unsubscribeToWaitingTopic() {
//        try {
//            mMqttAndroidClient.unsubscribe(mCallCatchTopic);
//        } catch (MqttException e) {
//            e.printStackTrace();
//        }
//    }
//

    /**
     * 메세지 보내기
     *
     * @param topicName
     * @param data
     */
    public void publishMessage(String topicName, String data) {

        if (mMqttAndroidClient != null && mMqttAndroidClient.isConnected()) {
            try {
                MqttMessage message = new MqttMessage();
                message.setPayload(data.getBytes());
                mMqttAndroidClient.publish(topicName, message);
            } catch (MqttException e) {
                System.err.println("Error Publishing: " + e.getMessage());
            }
        }
    }

    /**
     * mqtt 연결 상태 확인
     *
     * @return
     */
    public boolean isConnected() {
        if (mMqttAndroidClient != null) {
            return mMqttAndroidClient.isConnected();
        } else {
            return false;
        }
    }

    /**
     * mqtt 연결 해제
     */
    public void disConnect() {
        try {
            mMqttAndroidClient.unregisterResources();
            mMqttAndroidClient.close();
            mMqttAndroidClient.disconnect();
            mMqttAndroidClient = null;
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }
}
