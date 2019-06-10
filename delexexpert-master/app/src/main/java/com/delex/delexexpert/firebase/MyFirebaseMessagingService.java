package com.delex.delexexpert.firebase;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.delex.delexexpert.MainActivity3;
import com.delex.delexexpert.R;
import com.delex.delexexpert.firebase.model.PushModel;
import com.delex.delexexpert.userSession.SessionManager;
import com.delex.delexexpert.userSession.SessionManagerPojo;
import com.delex.delexexpert.util.Commonlib;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.Map;

/**
 * <h1>MyFirebaseMessagingService</h1>
 * This class is for handling the messages those were came from FCM server.
 *
 * @author 3embed
 * @since 6 Apr 2017.
 */
public class MyFirebaseMessagingService extends FirebaseMessagingService {
    PendingIntent intent = null;
    NotificationManager notificationManager;
    private static final String TAG = "FireBase_Message";
    private Bundle mbundle;
    private Bundle mbundle1;
    private int count = 0;
    private NotificationCompat.Builder mNoti;
    private Gson mGson = new Gson();

    @Override
    public void onNewToken(String token) {
        super.onNewToken(token);
        SessionManager sessionManager = new SessionManager(this);
        sessionManager.setStringData(SessionManagerPojo.PUSH_TOKEN, token);
    }

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {

        Log.d(TAG, "onMessageReceived: ");
        Log.d(TAG, "onMessageReceived: onMessageReceiveddd" + remoteMessage.getData());

        Map<String, String> data = remoteMessage.getData();

        String message = data.get("message");

        PushModel pushData = mGson.fromJson(message, PushModel.class);

        String title = pushData.getData().getTitle();
        String body = pushData.getData().getBody();
        ArrayList<PushModel.PushImageModel> image = pushData.getData().getImage();

        createNoti(true, title, body, image);
    }

    public void createNoti(boolean firstNoti, @Nullable String title, @Nullable String message, @Nullable ArrayList<PushModel.PushImageModel> image) {
        if (firstNoti) {

            String channelId = "delex_channel_id";
            String channelName = "delex_channel_name";

            notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

                int importance = NotificationManager.IMPORTANCE_HIGH;
                NotificationChannel mChannel = new NotificationChannel(channelId, channelName, importance);
                notificationManager.createNotificationChannel(mChannel);
            }

            mNoti = new NotificationCompat.Builder(this, channelId)
                    .setContentTitle(title)
                    .setContentText(message)
                    .setAutoCancel(true)
                    .setContentIntent(clickNotiPendingIntent())
                    .setSmallIcon(R.mipmap.ic_launcher_expert);

            if (image != null) {
//                Bitmap bitmap = getImageFromURL("https://img.insight.co.kr/static/2019/01/21/700/8wps1552c4j3o4q91jn7.jpg");
                Bitmap bitmap = Commonlib.getImageFromURL(getString(R.string.main_url) + image.get(0).getPath());
                mNoti.setStyle(new NotificationCompat.BigPictureStyle().bigPicture(bitmap));
            }
        }

        notificationManager.notify(0, mNoti.build());

    }

    private PendingIntent clickNotiPendingIntent() {

        Intent clickNotiIntent = new Intent(this, MainActivity3.class);
        PendingIntent pending = PendingIntent.getService(this, 0, clickNotiIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        return pending;
    }

}


