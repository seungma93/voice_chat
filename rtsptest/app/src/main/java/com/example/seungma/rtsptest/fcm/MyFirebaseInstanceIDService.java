package com.example.seungma.rtsptest.fcm;

import android.util.Log;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Map;

public class MyFirebaseInstanceIDService extends FirebaseInstanceIdService {

    private static final String TAG = "MyFirebaseIIDservice";

    @Override
    public void onTokenRefresh(){
        String refreshedToken = FirebaseInstanceId.getInstance().getToken();
        Log.d(TAG,"Refreshed token" + refreshedToken);

        sendRegistrationToServer(refreshedToken);
    }

    private void sendRegistrationToServer(String token){

        //여기서 저장
    }
}
