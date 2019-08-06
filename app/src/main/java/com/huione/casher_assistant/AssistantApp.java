package com.huione.casher_assistant;

import android.app.Application;

import com.iflytek.cloud.SpeechUtility;

public class AssistantApp extends Application {
    @Override
    public void onCreate() {
        SpeechUtility.createUtility(AssistantApp.this, "appid=" + getString(R.string.iflytek_app_id));  // set your own app id here
        super.onCreate();
    }
}