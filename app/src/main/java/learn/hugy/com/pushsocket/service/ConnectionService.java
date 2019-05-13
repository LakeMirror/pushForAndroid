package learn.hugy.com.pushsocket.service;

import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.Socket;
import java.net.URL;
import java.net.URLConnection;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

import learn.hugy.com.pushsocket.PushSocket;
import learn.hugy.com.pushsocket.util.RandomUtil;

public class ConnectionService extends Service {

    private static final String TAG = "BackService";
    //心跳包频率
    private static final long HEART_BEAT_RATE = 30 * 1000;

    public static final String HOST = "192.168.1.7";
    public static final int PORT = 9998;

    public static final String MESSAGE_ACTION = "cn.win.notification.push";
    public static final String HEART_BEAT_ACTION = "com.splxtech.powermanagor.engine.socket.heart";
    //心跳包内容
    public static final String HEART_BEAT_STRING = "00";

    private ReadThread mReadThread;

    private LocalBroadcastManager mLocalBroadcastManager;

    private WeakReference<Socket> mSocket;

    // For heart Beat
    private Handler mHandler = new Handler();
    private Runnable heartBeatRunnable = new Runnable() {

        @Override
        public void run() {
            if (System.currentTimeMillis() - sendTime >= HEART_BEAT_RATE) {
                //就发送一个HEART_BEAT_STRING过去 如果发送失败，就重新初始化一个socket
                boolean isSuccess = sendMsg(HEART_BEAT_STRING);
                if (!isSuccess) {
                    mHandler.removeCallbacks(heartBeatRunnable);
                    mReadThread.release();
                    releaseLastSocket(mSocket);
                    new InitSocketThread().start();
                }
            }
            mHandler.postDelayed(this, HEART_BEAT_RATE);
        }
    };

    private long sendTime = 0L;
    private String uid;

    private ExecutorService executor = Executors.newFixedThreadPool(3);

    @Override
    public void onCreate() {
        super.onCreate();
        new InitSocketThread().start();
        mLocalBroadcastManager = LocalBroadcastManager.getInstance(this);

    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public boolean sendMsg(final String msg) {
        if (null == mSocket || null == mSocket.get()) {
            return false;
        }
        final Socket soc = mSocket.get();

        if (!soc.isClosed() && !soc.isOutputShutdown()) {
            executor.execute(new Runnable() {
                @Override
                public void run() {
                    try {
                        OutputStream os = soc.getOutputStream();
                        String message = msg;
                        os.write(message.getBytes());
                        os.flush();
                        //每次发送成数据，就改一下最后成功发送的时间，节省心跳间隔时间
                        sendTime = System.currentTimeMillis();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });
        } else {
            return false;
        }

        return true;
    }

    private void initSocket() {//初始化Socket
        try {
            Socket so = new Socket(HOST, PORT);
            // 像服务端表明身份
            OutputStream os = so.getOutputStream();
            //we make this look like a valid IMEI
            uid = PushSocket.getIdBean();
            os.write(uid.getBytes());
            os.flush();
            mSocket = new WeakReference<Socket>(so);
            mReadThread = new ReadThread(so);
            mReadThread.start();
            //初始化成功后，就准备发送心跳包
            mHandler.postDelayed(heartBeatRunnable, HEART_BEAT_RATE);
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void releaseLastSocket(WeakReference<Socket> mSocket) {
        try {
            if (null != mSocket) {
                Socket sk = mSocket.get();
                if (!sk.isClosed()) {
                    sk.close();
                }
                sk = null;
                mSocket = null;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    class InitSocketThread extends Thread {
        @Override
        public void run() {
            super.run();
            initSocket();
        }
    }

    // Thread to read content from Socket
    class ReadThread extends Thread {
        private WeakReference<Socket> mWeakSocket;
        private boolean isStart = true;

        public ReadThread(Socket socket) {
            mWeakSocket = new WeakReference<Socket>(socket);
        }

        public void release() {
            isStart = false;
            releaseLastSocket(mWeakSocket);
        }

        @Override
        public void run() {
            super.run();
            Socket socket = mWeakSocket.get();
            if (null != socket) {
                try {
                    InputStream is = socket.getInputStream();
                    byte[] buffer = new byte[1024 * 4];
                    int length = 0;
                    while (!socket.isClosed() && !socket.isInputShutdown()
                            && isStart && ((length = is.read(buffer)) != -1)) {
                        if (length > 0) {
                            String message = new String(Arrays.copyOf(buffer,
                                    length)).trim();
                            Log.e(TAG, message);
                            //收到服务器过来的消息，就通过Broadcast发送出去
                            //处理心跳回复
                            if (message.equals(HEART_BEAT_STRING)) {
                                Intent intent = new Intent(HEART_BEAT_ACTION);
                                mLocalBroadcastManager.sendBroadcast(intent);
                            } else {
                                //其他消息回复
                                Intent intent = new Intent(MESSAGE_ACTION);
                                intent.putExtra("msg", message);
                                sendBroadcast(intent);
                            }
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // 通知后台此 socket 已离线
        Log.i("Hugy", "onDestroy");
        executor.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    URL url = new URL("http://10.0.2.2:8888/test/offline/" + uid);
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    connection.setConnectTimeout(10000);
                    connection.setRequestMethod("GET");
                    Log.i("Push", connection.getURL().toString());
                    connection.connect();
                    Log.i("Push", connection.getResponseCode() + "");
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }


}
