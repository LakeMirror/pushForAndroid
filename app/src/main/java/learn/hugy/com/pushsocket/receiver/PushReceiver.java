package learn.hugy.com.pushsocket.receiver;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import learn.hugy.com.pushsocket.R;

import static android.content.Context.NOTIFICATION_SERVICE;

public class PushReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i("PUSH", "接收到广播");
        String msg = intent.getStringExtra("msg");
        if (msg != null) {
            String[] splitMsg = msg.split("☆");
            if (splitMsg.length < 3) {
                return;
            }
            // APP 用 app 进行通知 否则用通知栏通知
            if ("APP".equals(splitMsg[0])) {
            } else {
                int iconId = 0;
                if ("normal".equals(splitMsg[1])) {
                    iconId = R.drawable.notification;
                } else if ("warn".equals(splitMsg[1])) {
                    iconId = R.drawable.warning;
                }
                NotificationManager notificationManager = (NotificationManager) context.getSystemService
                        (NOTIFICATION_SERVICE);

                /**
                 *  实例化通知栏构造器
                 */

                NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context);

                /**
                 *  设置Builder
                 */
                //设置标题
                mBuilder.setContentTitle("通知")
                        //设置内容
                        .setContentText(splitMsg[2])
                        //设置小图标
                        .setSmallIcon(iconId)
                        //设置通知时间
                        .setWhen(System.currentTimeMillis())
                        //首次进入时显示效果
                        .setTicker(msg)
                        //设置通知方式，声音，震动，呼吸灯等效果，这里通知方式为声音
                        .setDefaults(Notification.DEFAULT_SOUND);
                //发送通知请求
                notificationManager.notify(10, mBuilder.build());
            }
        }
    }
}
