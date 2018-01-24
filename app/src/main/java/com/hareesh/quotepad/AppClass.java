package com.hareesh.quotepad;

import android.app.Application;
import android.content.Context;
import com.firebase.client.Firebase;

import frenchtoast.FrenchToast;

/**
 * Created by Hareesh on 9/8/2016.
 */
public class AppClass extends Application {

    private static Context context;

    @Override
    public void onCreate() {
        super.onCreate();
        AppClass.context = getApplicationContext();
        Firebase.setAndroidContext(this);
        FrenchToast.install(this);
    }

    public static Context getAppContext() {
        return AppClass.context;
    }
}

