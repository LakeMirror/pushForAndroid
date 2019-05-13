package learn.hugy.com.pushsocket;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import java.lang.ref.WeakReference;

import learn.hugy.com.pushsocket.model.IdBean;
import learn.hugy.com.pushsocket.receiver.PushReceiver;
import learn.hugy.com.pushsocket.service.ConnectionService;
import learn.hugy.com.pushsocket.util.RandomUtil;

public class PushSocket {
    private static Intent serviceIntent;
    private static BroadcastReceiver receiver;
    private static WeakReference<Context> context;
    private static IdBean idBean;

    /**
     * 注册 开启推送服务
     * @param mContext
     */
    public static void register(Context mContext) {
        if (idBean == null) {
            throw new IllegalStateException("YOU NEED TO INVOKE METHOD id FIRST");
        } else {
            context = new WeakReference<>(mContext);
            serviceIntent = new Intent(mContext, ConnectionService.class);
            mContext.startService(serviceIntent);
            IntentFilter receiverFilter = new IntentFilter("cn.win.notification.push");
            receiver = new PushReceiver();
            mContext.registerReceiver(receiver, receiverFilter);
        }
    }

    /**
     * 注销 关闭推送服务
     */
    public static void unRegister() {
        if (serviceIntent == null || receiver == null) {
            throw new IllegalStateException("YOU MAY NOT REGISTER");
        } else {
            context.get().stopService(serviceIntent);
            context.get().unregisterReceiver(receiver);
        }
    }
    /**
     * 设置用户的分组
     */
    public static void id(Context mContext, String account, String group) {
        context = new WeakReference<>(mContext);
        initIdBean(account);
        if (!"".equals(group) && group != null) {
            idBean.setGroup(group);
        }
    }

    public static void phone(String phone) {
        if (idBean == null) {
            throw new IllegalStateException("NEED TO INVOKE METHOD id FIRST");
        } else {
            idBean.setPhone(phone);
        }
    }
    private static void initIdBean(String account) {
        if ("".equals(account) || null == account) {
            throw new IllegalArgumentException("Account Can not be null");
        } else {
            idBean = new IdBean(RandomUtil.greater("a".charAt(0), context.get(), account), "all", "", "");
        }
    }

    public static String getIdBean() {
        return idBean.toString();
    }
}
