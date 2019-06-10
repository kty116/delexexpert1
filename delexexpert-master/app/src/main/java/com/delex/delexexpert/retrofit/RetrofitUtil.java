package com.delex.delexexpert.retrofit;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import com.delex.delexexpert.R;
import com.delex.delexexpert.retrofit.model.ClientAuthenticationModel;
import com.delex.delexexpert.userSession.SessionManager;
import com.delex.delexexpert.userSession.SessionManagerPojo;
import com.delex.delexexpert.util.Commonlib;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.EOFException;
import java.io.IOException;
import java.util.Date;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RetrofitUtil {

    public static final String TAG = RetrofitUtil.class.getSimpleName();

    public static <T> void retrofitService(Context context, Call<T> call, final RetrofitResponseImpl<T> retrofitResponse) {
        if (Commonlib.isNetworkAvailable(context)) {

            call.enqueue(new Callback<T>() {
                @Override
                public void onResponse(Call<T> call, Response<T> response) {
                    if (response.isSuccessful()) {
                        T result = response.body();

                        Log.d(TAG, "onResponse: "+response);

                        retrofitResponse.successful(result);

                    } else {
                        JSONObject object = null;
                        try {
                            object = new JSONObject(response.errorBody().string());
                        } catch (JSONException e) {
                            e.printStackTrace();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        Log.d(TAG, "onResponse: JSONObject object = null");
                        if (object == null) {
                            retrofitResponse.failure("null");
                        }
                        retrofitResponse.failure(object.toString());
                    }
                }

                @Override
                public void onFailure(Call<T> call, Throwable t) {
                    if (t instanceof EOFException) {
                    } else {
                        retrofitResponse.failure(t.getMessage());
                    }
                }
            });

        } else {
            Toast.makeText(context, context.getString(R.string.no_network), Toast.LENGTH_LONG).show();
        }
    }


    public interface RetrofitResponseImpl<T> {
        void successful(T result);

        void failure(String t);
    }


    private interface AccessTokenResponseImpl {

        void successful(boolean isNewToken);

        void failure(String t);

    }
}

