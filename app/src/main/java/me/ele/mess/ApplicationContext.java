package me.ele.mess;

import android.app.Application;
import android.util.Log;

public class ApplicationContext extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d("test", "haha");
    }
}
