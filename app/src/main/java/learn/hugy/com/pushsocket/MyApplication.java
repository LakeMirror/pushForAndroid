package learn.hugy.com.pushsocket;

import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;

import learn.hugy.com.pushsocket.receiver.PushReceiver;
import learn.hugy.com.pushsocket.service.ConnectionService;

public class MyApplication extends Application {

   

    @Override
    public void onCreate() {
        super.onCreate();
        PushSocket.id(this, "zhangsan", "all");
        PushSocket.register(this);
    }

    @Override
    public void onTerminate() {
        Log.i("Push", "OnTerminate");
        super.onTerminate();

    }

    @Override
    public void onTrimMemory(int level) {
        super.onTrimMemory(level);
        Log.i("Push", "onTrimMemory");
        PushSocket.unRegister();
    }
}
